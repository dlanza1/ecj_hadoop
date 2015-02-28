package ec.hadoop.reader;

import java.io.IOException;

import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

import ec.hadoop.writables.IndividualWritable;
import ec.hadoop.writables.IndividualIndexWritable;

public class IndividualsFileInputFormat extends
		SequenceFileInputFormat<IndividualIndexWritable, IndividualWritable> {

	@Override
	public RecordReader<IndividualIndexWritable, IndividualWritable> createRecordReader(
			InputSplit split, TaskAttemptContext context) throws IOException {

		return new IndividualsFileRecordReader();
	}
}
