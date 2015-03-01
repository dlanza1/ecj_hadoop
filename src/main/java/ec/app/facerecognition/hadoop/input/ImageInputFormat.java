package ec.app.facerecognition.hadoop.input;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import ec.app.facerecognition.hadoop.ImageWritable;

public class ImageInputFormat extends FileInputFormat<IntWritable, ImageWritable> {

	@Override
	public RecordReader<IntWritable, ImageWritable> createRecordReader(
			InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		return new ImageRecordReader();
	}
	
}
