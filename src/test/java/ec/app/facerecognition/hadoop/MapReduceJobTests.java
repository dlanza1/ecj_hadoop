package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;

import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.ParametersWritable;

public class MapReduceJobTests {

	MapDriver<IntWritable, ImageWritable, IntWritable, ParametersWritable> mapDriver;
	ReduceDriver<IntWritable, ParametersWritable, IntWritable, ParametersWritable> reduceDriver;
	MapReduceDriver<IntWritable, ImageWritable, IntWritable, ParametersWritable, IntWritable, ParametersWritable> mapReduceDriver;

	@Before
	public void setUp() {
		ComputeImagenMapper mapper = new ComputeImagenMapper();
		NormalizeMatrixReducer reducer = new NormalizeMatrixReducer();
		mapDriver = MapDriver.newMapDriver(mapper);
		reduceDriver = ReduceDriver.newReduceDriver(reducer);
		mapReduceDriver = MapReduceDriver.newMapReduceDriver(mapper, reducer);
	}

	@Test
	public void testMapper() {
//		mapDriver.withInput(new LongWritable(), new Text(
//				"655209;1;796764372490213;804422938115889;6"));
//		mapDriver.withOutput(new Text("6"), new IntWritable(1));
//		mapDriver.runTest();
	}

	@Test
	public void testReducer() throws IOException {
		List<IntWritable> values = new ArrayList<IntWritable>();
		values.add(new IntWritable(1));
		values.add(new IntWritable(1));
//		reduceDriver.withInput(new Text("6"), values);
//		reduceDriver.withOutput(new Text("6"), new IntWritable(2));
		reduceDriver.runTest();
	}

	@Test
	public void testMapReduce() throws IOException {
//		mapReduceDriver.withInput(new LongWritable(), new Text(
//				"655209;1;796764372490213;804422938115889;6"));
		List<IntWritable> values = new ArrayList<IntWritable>();
		values.add(new IntWritable(1));
		values.add(new IntWritable(1));
//		mapReduceDriver.withOutput(new Text("6"), new IntWritable(2));
		mapReduceDriver.runTest();
	}

}
