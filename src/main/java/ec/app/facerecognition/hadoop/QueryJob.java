package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class QueryJob {
	
	Job job;

	public QueryJob(Configuration conf, int generation, int individual) throws IOException {
		this.job = Job.getInstance(conf, "ECJ-FaceRecognition Generation = "+generation
				+", Individual = "+individual+", Query phase");
		
		SequenceFileOutputFormat.setOutputPath(job, getOutputPath());
	}

	public float run() throws Exception { 
		
		job.setJarByClass(ComputeParamsMapper.class);
		job.addFileToClassPath(new Path(EvaluateIndividual.OPENCV_JAR));

		ImageInputFormat.setInputPaths(job, EvaluateIndividual.IMAGES_DIR);
		job.setInputFormatClass(ImageInputFormat.class);
		
		job.setMapperClass(QueryVectorMapper.class);
		job.setMapOutputKeyClass(MatEWritable.class);
		job.setMapOutputValueClass(MatEWithIDWritable.class);

		job.setReducerClass(QueryReducer.class);
		job.setNumReduceTasks(1);
		
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(FloatWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		job.waitForCompletion(true);
		
		//Get result
		@SuppressWarnings("resource")
		SequenceFile.Reader reader = new SequenceFile.Reader(job.getConfiguration(), 
				Reader.file(new Path(job.getConfiguration().get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM) + "query/part-r-00000")));
	    NullWritable key = NullWritable.get();
	    FloatWritable val = new FloatWritable();
		while(reader.next(key, val));
		
		return val.get();
	}

	private Path getOutputPath() {
		return new Path(job.getConfiguration().get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("query/"));
	}
}
