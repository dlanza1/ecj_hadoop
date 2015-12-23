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
		
		Runtime runtime = Runtime.getRuntime();
		
	}

	
	@Override
	public void run(
			Mapper<NullWritable, ImageWritable, NullWritable, MatEWithIDWritable>.Context context
			) throws IOException, InterruptedException {
		setup(context);
		try {
			long startTime = System.currentTimeMillis();
			
			int i = 0;
			while (context.nextKeyValue()) {
				map(context.getCurrentKey(), context.getCurrentValue(), context);
				i++;
				
				if(i % 50 == 0){
					long gcTime = System.currentTimeMillis();
					System.gc();
					System.out.println((System.currentTimeMillis() - gcTime) + " ms to GC");
				}
			}
			
			System.out.println((System.currentTimeMillis() - startTime) + " ms to run the loop");
		} finally {
			cleanup(context);
		}
	}
	
	@Override
	protected void map(NullWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {
		
		long startTime = System.currentTimeMillis();
		
		MatE parameters = image.getParameters(windows_size);
		
		System.out.println((System.currentTimeMillis() - startTime) + " ms");
		
		//System.out.println("Results for image " + image.getId());
		//System.out.println(parameters.dump());
		
		context.write(NullWritable.get(), new MatEWithIDWritable(image.getId(), parameters));
	}

}