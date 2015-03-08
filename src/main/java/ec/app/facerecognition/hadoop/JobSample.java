package ec.app.facerecognition.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class JobSample extends Configured implements Tool{

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		int res = ToolRunner.run(conf, new JobSample(), args);
		System.exit(res);
	}

	@Override
	public int run(String[] arg0) throws Exception { 
		
		Job job = Job.getInstance(new Configuration(), "Face Recognition");
		job.setJarByClass(ComputeImagenMapper.class);

		job.setInputFormatClass(ImageInputFormat.class);
		job.setMapperClass(ComputeImagenMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(MatEWritable.class);

		job.setReducerClass(NormalizeMatrixReducer.class);
		job.setOutputKeyClass(MatEWritable.class);
		job.setOutputValueClass(MatEWritable.class);

		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		job.addFileToClassPath(new Path("/user/hdfs/ecj_hadoop/lib/opencv-2410.jar"));

		ImageInputFormat.setInputPaths(job, "/user/hdfs/ecj_hadoop/img/");
		SequenceFileOutputFormat.setOutputPath(job, new Path("/user/hdfs/ecj_hadoop/out/"));

		Configuration conf = job.getConfiguration();
		conf.setInt(ImageRecordReader.NUM_OF_SPLITS_PARAM, 30);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "/user/hdfs/ecj_hadoop/files.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "/user/hdfs/ecj_hadoop/poi.csv");
		conf.set(ImageRecordReader.FILTER_POI_PARAM, "00000000" + "00000000" // 0 - 15
				+ "11111111" + "11111111" // 16 - 31
				+ "11111111" + "11111111" // 32 - 47
				+ "11111111" + "11111111" // 48 - 63
				+ "11111111" + "1111" // 64 - 75
		);

		job.waitForCompletion(true);
		
		return 0;
	}

	
}
