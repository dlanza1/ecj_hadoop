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

	private Path[] paths;
	private int index_path;

	@Override
	public void close() throws IOException {
	}

	@Override
	public IntWritable getCurrentKey() throws IOException, InterruptedException {
		return new IntWritable(index_path);
	}

	@Override
	public ImageWritable getCurrentValue() throws IOException,
			InterruptedException {
//		FileSystem fs = path.getFileSystem(context.getConfiguration());
//		value = new ImageWritable(MatE.fromFile(fs.open(path)));
		
		ImageWritable image = new ImageWritable(new MatE(
				Highgui.imread(Path.getPathWithoutSchemeAndAuthority(paths[index_path]).toString())));
		
		if(image.getValue() == null || !image.getValue().hasContent())
			throw new IOException("the image " + this.paths +" couldn't be loaded");
		
		return image;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return (float) index_path / (float) paths.length;
	}

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.paths = ((MultiFileSplit) inputSplit).getPaths();
		this.index_path = -1;
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		return ++index_path < paths.length;
	}

}
