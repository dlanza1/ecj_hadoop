package ec.app.facerecognition.hadoop.input;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import ec.app.facerecognition.hadoop.ImageWritable;

public class ImageInputFormat extends MultiFileInputFormat<IntWritable, ImageWritable> {

	@Override
	public RecordReader<IntWritable, ImageWritable> createRecordReader(
			InputSplit split, TaskAttemptContext taskAttemptContext) throws IOException,
			InterruptedException {
		return new ImageRecordReader();
	}

}
