package ec.app.facerecognition.hadoop;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class ReferenceMatrixReducer extends
		Reducer<IntWritable, ParametersWritable, IntWritable, ParametersWritable> {

}
