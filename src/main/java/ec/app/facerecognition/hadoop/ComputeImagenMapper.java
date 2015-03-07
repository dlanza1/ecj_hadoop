package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.opencv.core.Core;

import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class ComputeImagenMapper extends Mapper<IntWritable, ImageWritable, IntWritable, MatEWritable> {

	private int roi_radius;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		roi_radius = context.getConfiguration().getInt("ec.app.facerecognition.roi.radius", 5);
	}
	
	@Override
	protected void map(IntWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {
		context.write(key, new MatEWritable(image.getParameters(roi_radius)));
	}

}