package ec.app.facerecognition.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.WritableComparable;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

import ec.app.facerecognition.MatE;

public class ImageWritable implements WritableComparable<ImageWritable> {
	
	private MatE value;
	
	public ImageWritable() {
	}

	public ImageWritable(MatE value) {
		this.value = value;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.value = MatE.read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.value.write(out);
	}

	@Override
	public int compareTo(ImageWritable o) {
		return 0;
	}
	
	public MatE getValue(){
		return value;
	}

}
