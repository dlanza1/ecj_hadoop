package ec.app.facerecognition.hadoop;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.server.MiniYARNCluster;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.ResourceScheduler;
import org.apache.hadoop.yarn.server.resourcemanager.scheduler.fifo.FifoScheduler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.app.facerecognition.hadoop.writables.MatEWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class MapReduceJobTests {

	private MiniYARNCluster miniCluster;
	private MiniDFSCluster hdfsCluster;
	private String hdfsURI;

	@Before
	public void setUp() throws IOException {

		
		File baseDir = new File("./target/hdfs/").getAbsoluteFile();
		FileUtil.fullyDelete(baseDir);
		Configuration conf = new Configuration();
		conf.set(MiniDFSCluster.HDFS_MINIDFS_BASEDIR, baseDir.getAbsolutePath());
		MiniDFSCluster.Builder builder = new MiniDFSCluster.Builder(conf);
		hdfsCluster = builder.build();
		hdfsURI = "hdfs://localhost:" + hdfsCluster.getNameNodePort() + "/";
		
		FileSystem hdfs = hdfsCluster.getFileSystem();
		hdfs.copyFromLocalFile(new Path("res/img/test/"), new Path(hdfsURI + "img/in"));
//		hdfs.copyFromLocalFile(new Path("res/img/"), new Path(hdfsURI + "img/in"));
		
		YarnConfiguration clusterConf = new YarnConfiguration();
		clusterConf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 64);
		clusterConf.setClass(YarnConfiguration.RM_SCHEDULER, FifoScheduler.class, ResourceScheduler.class);
		miniCluster = new MiniYARNCluster("miniYarnCluster", 1, 1, 1);
		miniCluster.init(clusterConf);
		miniCluster.start();
	}

	@Test
	public void testMapReduce() throws Exception {

		Job job = Job.getInstance(miniCluster.getConfig(), "Face Recognition");
	    job.setJarByClass(ComputeImagenMapper.class);
	    
	    job.setInputFormatClass(ImageInputFormat.class);
	    job.setMapperClass(ComputeImagenMapper.class);
	    job.setMapOutputKeyClass(NullWritable.class);
	    job.setMapOutputValueClass(MatEWritable.class);
	    
	    job.setReducerClass(TrainingReducer.class);
	    job.setOutputKeyClass(NullWritable.class);
	    job.setOutputValueClass(TrainingResultsWritable.class);
	    
	    job.setOutputFormatClass(SequenceFileOutputFormat.class);
	    
	    job.setNumReduceTasks(1);
	    
	    ImageInputFormat.setInputPaths(job, hdfsURI + "img/in");
	    SequenceFileOutputFormat.setOutputPath(job, new Path(hdfsURI + "img/out/"));
	    
	    Configuration conf = job.getConfiguration();
		conf.setInt(ImageRecordReader.NUM_OF_SPLITS_PARAM, 10);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/test/nombres.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/test/datos.csv");
//		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/nombres.csv");
//		conf.set(ImageRecordReader.POI_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/datos.csv");
		conf.set(ImageRecordReader.FILTER_POI_PARAM,
				  "00000000" + "00000000" // 0 - 15
				+ "11111111" + "11111111" //16 - 31
				+ "11111111" + "11111111" //32 - 47
				+ "11111111" + "11111111" //48 - 63
				+ "11111111" + "1111"     //64 - 75  
				);
	    
		job.waitForCompletion(true);
//		job.submit();
//		while(!job.isComplete())
//			System.out.println("waiting for job");
		
		Assert.assertTrue(job.isSuccessful());
		
		FileSystem hdfs = hdfsCluster.getFileSystem();

	    FileUtil.fullyDelete(new File("./target/job/out"));
	    hdfs.copyToLocalFile(new Path(hdfsURI + "img/out"), new Path("./target/job"));
	    
	    SequenceFile.Reader reader = new SequenceFile.Reader(conf, Reader.file(new Path(hdfsURI + "img/out/part-r-00000")));
		
	    NullWritable key = NullWritable.get();
	    TrainingResultsWritable val = new TrainingResultsWritable();
		while(reader.next(key, val)){
			System.out.println(val);
		}
	}
	
	@After
	public void destroy(){
		miniCluster.stop();
		hdfsCluster.shutdown();
	}

}
