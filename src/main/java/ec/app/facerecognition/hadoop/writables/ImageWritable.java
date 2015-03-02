package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.MatE;
import ec.app.facerecognition.catalog.Image;

public class ImageWritable extends Image implements WritableComparable<ImageWritable> {
	
	public ImageWritable() {
	}

	public ImageWritable(MatE value) {
		this.value = value;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		value = MatE.read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		value.write(out);
	}

	@Override
	public int compareTo(ImageWritable o) {
		return 0;
	}

}
