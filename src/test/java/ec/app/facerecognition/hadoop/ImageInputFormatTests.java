package ec.app.facerecognition.hadoop;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;
import ec.app.facerecognition.hadoop.writables.ImageWritable;

public class ImageInputFormatTests {
	
	@Before
	public void setU() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Test
	public void readFiles() throws IOException, InterruptedException{
		Configuration conf = new Configuration(false);
		conf.set("fs.default.name", "file:///");
		conf.setInt("mapreduce.input.multifileinputformat.splits", 10);
		Job job = Job.getInstance(conf);
		
		ImageInputFormat.setInputPaths(job, "src/main/java/ec/app/facerecognition/img/");
		InputFormat<IntWritable, ImageWritable> inputFormat = ReflectionUtils.newInstance(ImageInputFormat.class, conf);
		List<InputSplit> splits = inputFormat.getSplits(job);
		
		int counter = 0;
		
		for (InputSplit split : splits) {
			TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
			RecordReader<IntWritable, ImageWritable> reader = inputFormat.createRecordReader(split, context);

			reader.initialize(split, context);
			while(reader.nextKeyValue()){
				ImageWritable image = (ImageWritable) reader.getCurrentValue();
				
				assertTrue(image.getValue().hasContent());
				counter++;
			}
		}
		
		//Number of files in the folder is 3755
		System.out.println("The number of images readed was " + counter 
				+ " divided in " + splits.size() + " splits.");
	}
	
	
	
}
