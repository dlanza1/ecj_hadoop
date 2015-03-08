package ec.app.facerecognition.catalog;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class RGB {

	/**
	 * Red channel
	 */
	MatE r;
	
	/**
	 * Green channel
	 */
	MatE g;
	
	/**
	 * Blue channel
	 */
	MatE b;
	
	public RGB(MatE value) {
		//To RGB type Double
		MatE target = new MatE();
		value.convertTo(target, CvType.CV_64FC(3), 1.0 / 255.0);
		
		List<Mat> channels = target.split();
		
		r = new MatE(channels.get(2));
		g = new MatE(channels.get(1));
		b = new MatE(channels.get(0));
	}
	
	public void release(){
		r.release();
		g.release();
		b.release();
	}

}
