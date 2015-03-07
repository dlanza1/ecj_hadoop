package ec.app.facerecognition.hadoop;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
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
		FileSystem hdfs=hdfsCluster.getFileSystem();
		hdfs.copyFromLocalFile(new Path("src/main/java/ec/app/facerecognition/img/test/"), new Path(hdfsURI + "img/in"));
		
		YarnConfiguration clusterConf = new YarnConfiguration();
		clusterConf.setInt(YarnConfiguration.RM_SCHEDULER_MINIMUM_ALLOCATION_MB, 64);
		clusterConf.setClass(YarnConfiguration.RM_SCHEDULER, FifoScheduler.class, ResourceScheduler.class);
		miniCluster = new MiniYARNCluster("miniYarnCLuster", 2, 1, 1);
		miniCluster.init(clusterConf);
		miniCluster.start();
	}

	@Test
	public void testMapReduce() throws Exception {

		Job job = Job.getInstance(miniCluster.getConfig(), "Face Recognition");
	    job.setJarByClass(ComputeImagenMapper.class);
	    
	    job.setInputFormatClass(ImageInputFormat.class);
	    job.setMapperClass(ComputeImagenMapper.class);
	    
	    job.setReducerClass(NormalizeMatrixReducer.class);
	    job.setOutputKeyClass(IntWritable.class);
	    job.setOutputValueClass(MatEWritable.class);
	    
	    ImageInputFormat.setInputPaths(job, hdfsURI + "img/in");
	    FileOutputFormat.setOutputPath(job, new Path(hdfsURI + "img/out/"));
	    
	    Configuration conf = job.getConfiguration();
		conf.setInt("mapreduce.input.multifileinputformat.splits", 10);
		conf.set(ImageRecordReader.NAMES_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/nombres.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/datos.csv");
		conf.set(ImageRecordReader.FILTER_POI_PARAM,
				  "00000000" + "00000000" // 0 - 15
				+ "11111111" + "11111111" //16 - 31
				+ "11111111" + "11111111" //32 - 47
				+ "11111111" + "11111111" //48 - 63
				+ "11111111" + "1111"     //64 - 75  
				);
	    
	    Assert.assertTrue(job.waitForCompletion(true));
	}
	
	@After
	public void destroy(){
		miniCluster.stop();
		hdfsCluster.shutdown();
	}

}
