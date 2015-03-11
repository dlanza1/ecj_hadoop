package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.catalog.MatE;

public class QueryVectorWritable implements WritableComparable<QueryVectorWritable> {
	
	Integer image_id;
	
	MatE vector;
	
	public QueryVectorWritable() {
	}

	public QueryVectorWritable(Integer image_id, MatE vectorConsulta) {
		this.image_id = image_id;
		this.vector = vectorConsulta;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		image_id = in.readInt();
		vector = MatE.read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(image_id);
		vector.write(out);
	}

	@Override
	public int compareTo(QueryVectorWritable o) {
		return image_id.compareTo(o.image_id);
	}

}
