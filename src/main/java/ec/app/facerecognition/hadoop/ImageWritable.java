package ec.app.facerecognition.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;
import org.opencv.core.Mat;

public class ImageWritable implements WritableComparable<ImageWritable> {
	
	private String file_name;
	private Mat image;
	private int number;

	@Override
	public void readFields(DataInput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(DataOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int compareTo(ImageWritable o) {
		// TODO Auto-generated method stub
		return 0;
	}

}
