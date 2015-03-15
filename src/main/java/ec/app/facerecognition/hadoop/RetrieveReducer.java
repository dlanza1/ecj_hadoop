package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;

public class RetrieveReducer extends Reducer<NullWritable, MatEWithIDWritable, NullWritable, IntWritable> {

	@Override
	protected void reduce(NullWritable key, Iterable<MatEWithIDWritable> queryVectors, Context arg2)
			throws IOException, InterruptedException {
		
		MatE query_mat = new MatE();

		//Join all vectors
		List<MatEWithIDWritable> mats = new LinkedList<MatEWithIDWritable>();
		for (MatEWithIDWritable mat : queryVectors) {
			MatEWithIDWritable tmp = new MatEWithIDWritable();
			mat.copyTo(tmp);
			mat.release();
			mats.add(tmp);
		}
		Collections.sort(mats);
		Core.vconcat((List<Mat>)(List<?>) mats, query_mat);
	}
}
