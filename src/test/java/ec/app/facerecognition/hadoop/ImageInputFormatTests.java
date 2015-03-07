package ec.app.facerecognition.hadoop;

import static org.junit.Assert.*;

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
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

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
		conf.set(ImageRecordReader.NAMES_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/nombres.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/datos.csv");
		conf.set(ImageRecordReader.FILTER_POI_PARAM,
				  "00000000" + "00000000" // 0 - 15
				+ "11111111" + "11111111" //16 - 31
				+ "11111111" + "11111111" //32 - 47
				+ "11111111" + "11111111" //48 - 63
				+ "11111111" + "1111"     //64 - 75  
				);
		Job job = Job.getInstance(conf);
		
		ImageInputFormat.setInputPaths(job, "src/main/java/ec/app/facerecognition/img/test/");
		InputFormat<IntWritable, ImageWritable> inputFormat = ReflectionUtils.newInstance(ImageInputFormat.class, conf);
		List<InputSplit> splits = inputFormat.getSplits(job);
		
		int counter = 0;
		
		for (InputSplit split : splits) {
			TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
			RecordReader<IntWritable, ImageWritable> reader = inputFormat.createRecordReader(split, context);

			reader.initialize(split, context);
			while(reader.nextKeyValue()){
				IntWritable key = reader.getCurrentKey();
				ImageWritable image = (ImageWritable) reader.getCurrentValue();
				
				//Mapper
				long time = System.currentTimeMillis();
				int roi_radius = 5;
				MatEWritable params = new MatEWritable(image.getParameters(roi_radius));
				
				System.out.println("Analized: key=" + key + " Image=" + image.getFileName());
//				System.out.println("Params: " + params);
				System.out.println("Time: " + (System.currentTimeMillis() - time) + " ms");
				System.out.println();
				//End mapper
				
				assertTrue(image.getValue().hasContent());
				assertEquals(image.getPOI().size(), 60);
				counter++;
			}
		}
		
		//The number of files in the normal folder is 3755 (filtered is 1368)
		//The number of files in the test folder is 7 (filtered is 3)
		System.out.println("The number of images read was " + counter 
				+ " divided in " + splits.size() + " splits.");
	}
	
	
	
}
