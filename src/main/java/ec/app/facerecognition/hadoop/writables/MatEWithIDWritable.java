package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.catalog.MatE;

public class MatEWithIDWritable extends MatE implements WritableComparable<MatEWithIDWritable> {
	
	Integer id;
	
	public MatEWithIDWritable() {
		super();
	}

	public MatEWithIDWritable(Integer image_id, MatE value) {
		this.id = image_id;
		value.copyTo(this);
		value.release();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		id = in.readInt();
		read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(id);
		super.write(out);
	}

	@Override
	public int compareTo(MatEWithIDWritable o) {
		return id.compareTo(o.id);
	}
	
	public Integer getID() {
		return id;
	}

	public void copyTo(MatEWithIDWritable m) {
		assignTo(m);
		
		m.id = id;
	}
}
