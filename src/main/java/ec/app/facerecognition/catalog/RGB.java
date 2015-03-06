package ec.app.facerecognition.catalog;

import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import ec.app.facerecognition.MatE;

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
	
	public HSI toHSI(){
		Mat parcial = new Mat(), parcial1 = new Mat(), parcial2 = new Mat(), parcial3 = new Mat(), num = new Mat(), rg = new Mat();
		MatE mulRGB = new MatE(), teta = new MatE(), den = new MatE(), minimo = new MatE(), suma = new MatE();
		
		MatE h = new MatE(), s = new MatE(), i = new MatE();
		
		Core.subtract(r, g, parcial);
		Core.subtract(r, b, parcial1);
		Core.add(parcial, parcial1, h);
		Core.multiply(h, new Scalar(0.5), num);
		Core.pow(parcial, 2, rg);
		Core.subtract(g, b, parcial2);
		Core.multiply(parcial1, parcial2, mulRGB);
		Core.add(rg, mulRGB, parcial3);
		Core.sqrt(parcial3, den);
		Core.add(den, MatE.EP, den);
		Core.divide(num, den, teta);
		
		MatE angle = teta.getAngle(b, g);
			
		angle.convertTo(angle, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(angle, h);
		h.convertTo(h, CvType.CV_64F, 1.0 / 255.0);
		Scalar valorPi = new Scalar(2 * Math.PI);
		Core.divide(h, valorPi, h);
		Core.min(r, g, minimo);
		Core.min(minimo, b, minimo);
		Core.add(r, g, suma);
		Core.add(suma, b, suma);
		Core.add(suma, MatE.EP, suma);
		Core.divide(minimo, suma, s);
		Scalar valorS = new Scalar(-3.0);
		Scalar valorS1 = new Scalar(-1.0);
		Core.multiply(s, valorS, s);
		Core.subtract(s, valorS1, s);
		

		Mat pI = new Mat();
		Core.add(r, g, pI);
		Core.add(b, pI, pI);
		Core.divide(pI, new Scalar(3.0), i);
		i.convertTo(i, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(i, i);
		i.convertTo(i, CvType.CV_64F, 1.0 / 255.0);
		
		s.convertTo(s, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(s, s);
		s.convertTo(s, CvType.CV_64F, 1.0 / 255.0);
		
		return new HSI(h, s, i);
	}

}
