package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class NormalizeMatrixReducer extends
		Reducer<IntWritable, MatEWritable, IntWritable, MatEWritable> {

	@Override
	protected void reduce(
			IntWritable key,
			Iterable<MatEWritable> values,
			Context context)
			throws IOException, InterruptedException {

		for (MatEWritable matEWritable : values) {
			context.write(key, matEWritable);
		}
	}
}
