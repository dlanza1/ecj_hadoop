package ec.app.facerecognition.catalog;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.hadoop.fs.FSDataInputStream;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

public class MatE extends Mat {
	
	static{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	/**
	 * Epsilon
	 */
	public static Scalar EP = new Scalar(Double.MIN_VALUE);

	public MatE() {
		super();
	}
	
	/**
	 * Copy of input matrix 
	 * 
	 * @param in
	 */
	public MatE(Mat in) {
		in.copyTo(this);
		in.release();
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
		int channels = in.readInt();
		
		double[] buff = new double[rows * cols * channels];
		for (int i = 0; i < buff.length; i++)
			buff[i] = in.readDouble();
		
		MatE loaded = new MatE(rows, cols, CvType.CV_64F);
		loaded.put(0, 0, buff);
		
		return loaded;
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(rows());
		out.writeInt(cols());
		out.writeInt(channels());
		
		double[] buff = new double[rows() * cols() * channels()];
		get(0, 0, buff);
		
		for (double d : buff)
			out.writeDouble(d);
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
	

	/**
	 * Calculate de acos of each element
	 * 
	 * Absolute error: 6.8e-5
	 * 
	 * From Nvidia CUDA implementation: http://http.developer.nvidia.com/Cg/acos.html
	 * Autors: M. Abramowitz and I.A. Stegun, Ed.
	 * 
	 * @return MatE
	 */
	public MatE acos() {
//		double negate = x <= 0 ? 1 : 0;
		Mat negate = new Mat();
		Core.inRange(this, new Scalar(-1), new Scalar(0), negate);
		Core.divide(negate, new Scalar(255), negate);
		negate.convertTo(negate, CvType.CV_64F);
//		x = Math.abs(x);
		Mat in_abs = new Mat();
		Core.absdiff(this, new Scalar(0), in_abs);
//		double ret = -0.0187293;
//		ret = ret * x;
		MatE ret = new MatE();
		Core.multiply(in_abs, new Scalar(-0.0187293), ret);
//		ret = ret + 0.0742610;
		Core.add(ret, new Scalar(0.0742610), ret);
//		ret = ret * x;
		Core.multiply(ret, in_abs, ret);
//		ret = ret - 0.2121144;
		Core.add(ret, new Scalar(-0.2121144), ret);
//		ret = ret * x;
		Core.multiply(ret, in_abs, ret);
//		ret = ret + 1.5707288;
		Core.add(ret, new Scalar(1.5707288), ret);
//		ret = ret * Math.sqrt(1.0 - x);
//		Nota: 1 - x = - x + 1 = -1 * x + 1;
		Mat sqrt = new Mat();
		Core.multiply(in_abs, new Scalar(-1), sqrt);
		Core.add(sqrt, new Scalar(1), sqrt);
		Core.sqrt(sqrt, sqrt);
		Core.multiply(ret, sqrt, ret);
//		ret = ret - 2 * negate * ret;
		Mat tmp = new Mat();
		Core.multiply(ret, negate, tmp);
		Core.multiply(tmp, new Scalar(-2), tmp);
		Core.add(ret, tmp, ret);
//		return negate * 3.14159265358979 + ret;
		Core.multiply(negate, new Scalar(3.14159265358979), negate);
		Core.add(ret, negate, ret);
		
		negate.release();
		in_abs.release();
		sqrt.release();
		tmp.release();
		
		return ret;
	}

	public MatE getAngle(Mat mB, Mat mG) {
		Mat mat_two_times_pi = new Mat();
		
		Mat comp = Mat.zeros(mB.rows(), mB.cols(), CvType.CV_64F);
		Core.subtract(mB, mG, comp);	
		Core.inRange(comp, EP, new Scalar(Double.MAX_VALUE), mat_two_times_pi);
		mat_two_times_pi.convertTo(mat_two_times_pi, CvType.CV_64F);
		Core.divide(mat_two_times_pi, new Scalar(255), mat_two_times_pi);
		
		Mat sig_change = new Mat();
		Core.multiply(mat_two_times_pi, new Scalar(-2), sig_change);
		Core.add(sig_change, new Scalar(1), sig_change);
		Core.multiply(mat_two_times_pi, new Scalar(2 * Math.PI), mat_two_times_pi);

		MatE angle = acos();
		Core.multiply(angle, sig_change, angle);
		Core.add(mat_two_times_pi, angle, angle);
		
		mat_two_times_pi.release();
		comp.release();
		sig_change.release();
		
		return angle;
	}

	public List<Mat> split() {
		List<Mat> splits = new ArrayList<Mat>(3);
		Core.split(this, splits);
		return splits;
	}
	
	/**
	 * Calculate the homogeinity
	 * 
	 * @return Homogeinity
	 */
	public double homogeinity() {
		Mat out = new Mat();
		convertTo(out, CvType.CV_8U, 255, 0);
		
		int cont = out.rows() * (out.cols() - 1) * 2;
		double hom = 0;
		
		for (int i = 0; i < out.rows(); i++) {
			for (int j = 0; j < out.cols() - 1; j++) {
				int x = (int) out.get(i, j)[0];
				int y = (int) out.get(i, j + 1)[0];
				
				if(x >= 254 && y >= 254)
					continue;
				
				if(x != y)
					hom += 2d / (cont * (1 + Math.abs(x - y)));
				else
					hom += 1d / (cont * (1 + Math.abs(x - y)));
			}
		}
		
		out.release();

		return hom;
	}

	public MatE getWindows(int x, int y, int radius) {
		return new MatE(submat(y - radius, y + (radius + 1), x - radius, x + (radius + 1)));
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) 
			return true;
		
		if((obj == null) || obj.getClass() != this.getClass())
			return false;

		MatE other = (MatE) obj; 

		MatE dst = new MatE(rows(), cols(), type());
		Core.subtract(this, other, dst);
		
		return Core.countNonZero(dst) == 0;
	}

}
