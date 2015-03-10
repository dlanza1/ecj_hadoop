package ec.app.facerecognition.hadoop.input;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import ec.app.facerecognition.hadoop.writables.ImageWritable;

public class ImageInputFormat extends MultiFileInputFormat<NullWritable, ImageWritable> {

	@Override
	public RecordReader<NullWritable, ImageWritable> createRecordReader(
			InputSplit split, TaskAttemptContext taskAttemptContext) throws IOException,
			InterruptedException {
		return new ImageRecordReader();
	}

}