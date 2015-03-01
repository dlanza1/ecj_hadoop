package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

public class ComputeImagenMapper extends Mapper<IntWritable, ImageWritable, IntWritable, ParametersWritable> {

	@Override
	protected void map(IntWritable key, ImageWritable value, Context context)
			throws IOException, InterruptedException {

	}
}
