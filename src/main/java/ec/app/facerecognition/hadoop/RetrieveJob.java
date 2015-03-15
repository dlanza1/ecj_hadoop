package ec.app.facerecognition.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;

public class RetrieveJob {
	
	Configuration conf;
	int generation;
	int individual;

	public RetrieveJob(Configuration conf, int generation, int individual) {
		this.conf = conf;
		this.generation = generation;
		this.individual = individual;
	}

	public boolean run() throws Exception { 
		
		Job job = Job.getInstance(conf, "ECJ-FaceRecognition Generation = "+generation
				+", Individual = "+individual+", Retrieve phase");
		job.setJarByClass(ComputeParamsMapper.class);
		job.addFileToClassPath(new Path(EvaluateIndividual.OPENCV_JAR));

		ImageInputFormat.setInputPaths(job, EvaluateIndividual.IMAGES_DIR);
		job.setInputFormatClass(ImageInputFormat.class);
		
		job.setMapperClass(QueryVectorMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(MatEWithIDWritable.class);

		job.setReducerClass(RetrieveReducer.class);
		job.setNumReduceTasks(1);
		
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(IntWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		SequenceFileOutputFormat.setOutputPath(job, getOutputPath());

		return job.waitForCompletion(true);
	}

	private Path getOutputPath() {
		return new Path(conf.get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("retrieve/"));
	}
}
