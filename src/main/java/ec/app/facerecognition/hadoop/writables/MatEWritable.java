package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.catalog.MatE;

public class MatEWritable extends MatE implements WritableComparable<MatEWritable> {

	public MatEWritable() {
	}
	
	public MatEWritable(MatE parameters) {
		super(parameters);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		super.read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		super.write(out);
	}

	@Override
	public int compareTo(MatEWritable o) {
		return 0;
	}

}
