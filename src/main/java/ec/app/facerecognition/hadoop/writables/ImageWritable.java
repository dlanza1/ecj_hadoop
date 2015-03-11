package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.catalog.Image;
import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.catalog.POI;

public class ImageWritable extends Image implements WritableComparable<ImageWritable> {

	public ImageWritable() {
	}

	public ImageWritable(Integer id, String file_name, MatE value, LinkedList<POI> poi) {
		super(id, file_name, value, poi);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		file_name = in.readUTF();
		value = MatE.read(in);
		
		int poi_length = in.readInt();
		poi = new LinkedList<POI>();
		for (int i = 0; i < poi_length; i++) {
			POI poi_ = new POI();
			poi_.read(in);
			poi.add(poi_);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeUTF(file_name);
		value.write(out);
		value.release();
		
		out.writeInt(poi.size());
		for (POI poi_ : poi)
			poi_.write(out);
	}

	@Override
	public int compareTo(ImageWritable other) {
		return id.compareTo(other.id);
	}

}
