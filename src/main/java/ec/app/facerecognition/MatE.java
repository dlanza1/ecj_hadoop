package ec.app.facerecognition;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.hadoop.fs.FSDataInputStream;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;

public class MatE extends Mat {

	/**
	 * Copy of input matrix 
	 * 
	 * @param in
	 */
	public MatE(Mat in) {
		in.copyTo(this);
	}

	public MatE(int rows, int cols, int type) {
		super(rows, cols, type);
	}

	/**
	 * Print the first dimension
	 */
	public void print() {
		for (int r = 0; r < rows(); r++){
			for (int c = 0; c < cols(); c++)
				System.out.print(get(r,c)[0] + " ");
			System.out.println();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		
		for (int r = 0; r < rows(); r++){
			for (int c = 0; c < cols(); c++)
				b.append(get(r,c)[0]);
			b.append("\n");
		}
		
		return b.toString();
	}

	public static MatE read(DataInput in) throws IOException {
		int rows = in.readInt();
		int cols = in.readInt();
		
		byte[] buff = new byte[rows * cols];
		in.readFully(buff);
		
		MatE loaded = new MatE(rows, cols, CvType.CV_8U);
		loaded.put(0, 0, buff);
		
		return loaded;
	}

	public void write(DataOutput out) throws IOException {
		byte[] buff = new byte[(int) (total() * channels())];
		get(0, 0, buff);
		
		out.writeInt(rows());
		out.writeInt(cols());
		out.write(buff);
	}

	/**
	 * Load image from file
	 * 
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public static MatE fromFile(FSDataInputStream inputStream) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    byte[] data = new byte[16384];
	    int nRead;
	    while ((nRead = inputStream.read(data, 0, data.length)) != -1)
	        buffer.write(data, 0, nRead);
	    buffer.flush();
	    buffer.close();
	    inputStream.close();

		return new MatE(Highgui.imdecode(new MatOfByte(buffer.toByteArray()), Highgui.IMREAD_UNCHANGED));		
	}

	/**
	 * Show image
	 */
	public void show() {
		// source: http://answers.opencv.org/question/10344/opencv-java-load-image-to-gui/

		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = channels() * cols() * rows();
		byte[] b = new byte[bufferSize];
		get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(cols(), rows(), type);
		DataBufferByte dbb = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] targetPixels = dbb.getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		
		ImageIcon icon = new ImageIcon(image);
		JFrame frame = new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(image.getWidth(null) + 50, image.getHeight(null) + 50);
		JLabel lbl = new JLabel();
		lbl.setIcon(icon);
		frame.add(lbl);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public boolean hasContent() {
		return rows() > 0 || cols() > 0;
	}

}
