package ec.app.facerecognition.hadoop.input;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.opencv.highgui.Highgui;

import ec.app.facerecognition.MatE;
import ec.app.facerecognition.hadoop.ImageWritable;

public class ImageRecordReader extends RecordReader<IntWritable, ImageWritable> {

	private ImageWritable value;
	private boolean next;
	private Path path;

	@Override
	public void close() throws IOException {
	}

	@Override
	public IntWritable getCurrentKey() throws IOException, InterruptedException {
		return new IntWritable();
	}

	@Override
	public ImageWritable getCurrentValue() throws IOException,
			InterruptedException {
		return value;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return 0;
	}

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
//		FileSplit fileSplit = (FileSplit) inputSplit;
//		this.path = fileSplit.getPath();
//		this.next = false;
//
//		FileSystem fs = path.getFileSystem(context.getConfiguration());
//		value = new ImageWritable(MatE.fromFile(fs.open(path)));
		
		value = new ImageWritable(new MatE(
				Highgui.imread(Path.getPathWithoutSchemeAndAuthority(path).toString())));
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {		
		next = false;
		if(!next) return false;
		return true;
	}

}
