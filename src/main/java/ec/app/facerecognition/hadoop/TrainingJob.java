package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class TrainingJob{
	
	Job job;

	public TrainingJob(Configuration conf, int generation, int individual) throws IOException {		
		this.job = Job.getInstance(conf, "ECJ-FaceRecognition Generation = "+generation
				+", Individual = "+individual+", Training phase");
		
		SequenceFileOutputFormat.setOutputPath(job, getOutputPath());
	}

	public boolean run() throws IOException, ClassNotFoundException, InterruptedException  { 
		
		job.setJarByClass(ComputeParamsMapper.class);
		job.addFileToClassPath(new Path(EvaluateIndividual.OPENCV_JAR));

		ImageInputFormat.setInputPaths(job, EvaluateIndividual.IMAGES_DIR);
		job.setInputFormatClass(ImageInputFormat.class);
		
		job.setMapperClass(ComputeParamsMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(MatEWithIDWritable.class);

		job.setReducerClass(TrainingReducer.class);
		job.setNumReduceTasks(1);
		
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(TrainingResultsWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);

		return job.waitForCompletion(false);
	}

	public Path getOutputPath() {
		return new Path(job.getConfiguration().get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("training/"));
	}

	
}
