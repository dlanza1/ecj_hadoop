package ec.app.facerecognition.hadoop;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Core;

import ec.app.facerecognition.hadoop.input.ImageInputFormat;

public class ImageInputFormatTests {
	
	@Before
	public void setU() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Test
	public void readFiles() throws IOException, InterruptedException{
		Configuration conf = new Configuration(false);
		conf.set("fs.default.name", "file:///");

		File testFile = new File("/Users/daniellanzagarcia/git/ecj/src/main/java/ec/app/facerecognition/img/i000qa-fn.jpg");
		Path path = new Path(testFile.getAbsoluteFile().toURI());
		FileSplit split = new FileSplit(path, 0, testFile.length(), null);
		
		InputFormat inputFormat = ReflectionUtils.newInstance(ImageInputFormat.class, conf);
		TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
		RecordReader reader = inputFormat.createRecordReader(split, context);

		reader.initialize(split, context);
		reader.nextKeyValue();
		
		ImageWritable image = (ImageWritable) reader.getCurrentValue();
		image.getValue().show();
	}
	
	
	
}
