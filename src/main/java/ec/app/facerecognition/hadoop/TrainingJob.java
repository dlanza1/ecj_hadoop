package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.writables.MatEWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class TrainingJob{
	
	Configuration conf;
	int generation;
	int individual;

	public TrainingJob(Configuration conf, int generation, int individual) {
		this.conf = conf;
		this.generation = generation;
		this.individual = individual;
	}

	public boolean run() throws IOException, ClassNotFoundException, InterruptedException  { 
		
		Job job = Job.getInstance(conf, "ECJ-FaceRecognition Generation = "+generation
				+", Individual = "+individual+", Training phase");
		job.setJarByClass(ComputeParamsMapper.class);
		job.addFileToClassPath(new Path(EvaluateIndividual.OPENCV_JAR));

		ImageInputFormat.setInputPaths(job, EvaluateIndividual.IMAGES_DIR);
		job.setInputFormatClass(ImageInputFormat.class);
		
		job.setMapperClass(ComputeParamsMapper.class);
		job.setMapOutputKeyClass(NullWritable.class);
		job.setMapOutputValueClass(MatEWritable.class);

		job.setReducerClass(TrainingReducer.class);
		job.setNumReduceTasks(1);
		
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(TrainingResultsWritable.class);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		
		SequenceFileOutputFormat.setOutputPath(job, getOutputPath());

		return job.waitForCompletion(true);
	}

	public Path getOutputPath() {
		return new Path(conf.get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("training/"));
	}

	
}
