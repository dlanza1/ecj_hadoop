package ec.app.facerecognition.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

import ec.app.facerecognition.hadoop.writables.ParametersWritable;

public class NormalizeMatrixReducer extends
		Reducer<IntWritable, ParametersWritable, IntWritable, ParametersWritable> {

}