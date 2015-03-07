package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class ComputeImagenMapper extends Mapper<NullWritable, ImageWritable, NullWritable, MatEWritable> {

	private int roi_radius;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		roi_radius = context.getConfiguration().getInt("ec.app.facerecognition.roi.radius", 5);
	}
	
	@Override
	protected void map(NullWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {
		context.write(NullWritable.get(), new MatEWritable(image.getParameters(roi_radius)));
	}

}