package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;

import ec.app.facerecognition.hadoop.writables.QueryVectorWritable;

public class RetrieveReducer extends Reducer<NullWritable, QueryVectorWritable, NullWritable, IntWritable> {

	@Override
	protected void reduce(NullWritable key, Iterable<QueryVectorWritable> queryVectors, Context arg2)
			throws IOException, InterruptedException {
	}
}
