package ec.app.facerecognition.hadoop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
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
import ec.app.facerecognition.hadoop.input.MultiFileInputFormat;
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
		conf.setInt(MultiFileInputFormat.NUM_SPLITS_PARAM, 10);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/nombres.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "src/main/java/ec/app/facerecognition/res/datos.csv");
		conf.set(ImageRecordReader.FILTER_POI_PARAM,
				  "00000000" + "00000000" // 0 - 15
				+ "11111111" + "11111111" //16 - 31
				+ "11111111" + "11111111" //32 - 47
				+ "11111111" + "11111111" //48 - 63
				+ "11111111" + "1111"     //64 - 75  
				);
		Job job = Job.getInstance(conf);
		
		ImageInputFormat.setInputPaths(job, "res/img");
		InputFormat<NullWritable, ImageWritable> inputFormat = ReflectionUtils.newInstance(ImageInputFormat.class, conf);
		List<InputSplit> splits = inputFormat.getSplits(job);
		
		int counter = 0;
		
		for (InputSplit split : splits) {
			TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
			RecordReader<NullWritable, ImageWritable> reader = inputFormat.createRecordReader(split, context);

			reader.initialize(split, context);
			while(reader.nextKeyValue()){
				ImageWritable image = (ImageWritable) reader.getCurrentValue();
				
				//Mapper
				long time = System.currentTimeMillis();
				
				image.getParameters(5);
				
				System.out.println("Image=" + image.getFileName());
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
