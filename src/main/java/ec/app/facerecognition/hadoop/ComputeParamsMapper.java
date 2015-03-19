package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;

public class ComputeParamsMapper extends Mapper<NullWritable, ImageWritable, NullWritable, MatEWithIDWritable> {

	private int windows_size;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		windows_size = context.getConfiguration().getInt(EvaluateIndividual.WINDOWS_SIZE_PARAM, 5);
	}
	
	@Override
	protected void map(NullWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {
		
		MatE parameters = image.getParameters(windows_size);
		
		System.out.println("Results for image " + image.getId());
		System.out.println(parameters.dump());
		
		context.write(NullWritable.get(), new MatEWithIDWritable(image.getId(), parameters));
	}

}