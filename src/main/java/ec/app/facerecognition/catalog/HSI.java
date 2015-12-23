package ec.app.facerecognition.catalog;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class HSI {
	
	/**
	 * Hue
	 */
	MatE h;
	
	/**
	 * Saturation
	 */
	MatE s;
	
	/**
	 * Intensity
	 */
	MatE i;

	public HSI(RGB rgb) {
		
		long sstartTime = System.currentTimeMillis();
		
		long startTime = System.currentTimeMillis();
		
		h = new MatE();
		s = new MatE();
		i = new MatE();
		
		MatE parcial = new MatE();
		MatE parcial1 = new MatE();   
		MatE parcial2 = new MatE();
		
		Core.subtract(rgb.r, rgb.g, parcial);
		Core.subtract(rgb.r, rgb.b, parcial1);
		Core.add(parcial, parcial1, h);
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (1)");
		startTime = System.currentTimeMillis();
		
		Core.pow(parcial, 2, parcial2); 
		Core.subtract(rgb.g, rgb.b, parcial);
		Core.multiply(parcial1, parcial, parcial);
		Core.add(parcial2, parcial, parcial);
		Core.sqrt(parcial, parcial1);
		Core.add(parcial1, MatE.EP, parcial1);
		Core.multiply(h, new Scalar(0.5), parcial);
		Core.divide(parcial, parcial1, parcial);
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (2)");
		startTime = System.currentTimeMillis();
		
		parcial1 = parcial.getAngle(rgb.b, rgb.g);
		parcial1.convertTo(parcial1, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(parcial1, h);
		h.convertTo(h, CvType.CV_64F, 1.0 / 255.0);
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (3)");
		startTime = System.currentTimeMillis();
		
		Core.divide(h, new Scalar(2 * Math.PI), h);
		Core.min(rgb.r, rgb.g, parcial1);
		Core.min(parcial1, rgb.b, parcial1);
		Core.add(rgb.r, rgb.g, parcial);
		Core.add(parcial, rgb.b, parcial);
		Core.add(parcial, MatE.EP, parcial);
		Core.divide(parcial1, parcial, s);
		Scalar valorS = new Scalar(-3.0);
		Scalar valorS1 = new Scalar(-1.0);
		Core.multiply(s, valorS, s);
		Core.subtract(s, valorS1, s);
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (4)");
		startTime = System.currentTimeMillis();

		Core.add(rgb.r, rgb.g, parcial);
		Core.add(rgb.b, parcial, parcial);
		Core.divide(parcial, new Scalar(3.0), i);
		i.convertTo(i, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(i, i);
		i.convertTo(i, CvType.CV_64F, 1.0 / 255.0);
		
		s.convertTo(s, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(s, s);
		s.convertTo(s, CvType.CV_64F, 1.0 / 255.0);
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (5)");
		startTime = System.currentTimeMillis();
		
		rgb.release();
		parcial.release();
		parcial1.release();
		parcial2.release();
		
		System.out.println("      " + (System.currentTimeMillis() - startTime) + " ms (6)");
		startTime = System.currentTimeMillis();
		
		System.out.println("    " + (System.currentTimeMillis() - sstartTime) + " ms (hsi)");
	}
	
	public HSI(RGB rgb, boolean old) {
		h = new MatE();
		s = new MatE();
		i = new MatE();
		
		MatE parcial = new MatE(), 
				parcial1 = new MatE(), 
				parcial2 = new MatE(), 
				parcial3 = new MatE(), 
				num = new MatE(), 
				rg = new MatE(),
				mulRGB = new MatE(), 
				teta = new MatE(), 
				den = new MatE(), 
				minimo = new MatE(), 
				suma = new MatE();
		
		Core.subtract(rgb.r, rgb.g, parcial);
		Core.subtract(rgb.r, rgb.b, parcial1);
		Core.add(parcial, parcial1, h);
		Core.multiply(h, new Scalar(0.5), num);
		Core.pow(parcial, 2, rg); 
		Core.subtract(rgb.g, rgb.b, parcial2);
		Core.multiply(parcial1, parcial2, mulRGB);
		Core.add(rg, mulRGB, parcial3);
		Core.sqrt(parcial3, den);
		Core.add(den, MatE.EP, den);
		Core.divide(num, den, teta);
		
		MatE angle = teta.getAngle(rgb.b, rgb.g);
			
		angle.convertTo(angle, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(angle, h);
		h.convertTo(h, CvType.CV_64F, 1.0 / 255.0);
		Scalar valorPi = new Scalar(2 * Math.PI);
		Core.divide(h, valorPi, h);
		Core.min(rgb.r, rgb.g, minimo);
		Core.min(minimo, rgb.b, minimo);
		Core.add(rgb.r, rgb.g, suma);
		Core.add(suma, rgb.b, suma);
		Core.add(suma, MatE.EP, suma);
		Core.divide(minimo, suma, s);
		Scalar valorS = new Scalar(-3.0);
		Scalar valorS1 = new Scalar(-1.0);
		Core.multiply(s, valorS, s);
		Core.subtract(s, valorS1, s);

		Mat pI = new Mat();
		Core.add(rgb.r, rgb.g, pI);
		Core.add(rgb.b, pI, pI);
		Core.divide(pI, new Scalar(3.0), i);
		i.convertTo(i, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(i, i);
		i.convertTo(i, CvType.CV_64F, 1.0 / 255.0);
		
		s.convertTo(s, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(s, s);
		s.convertTo(s, CvType.CV_64F, 1.0 / 255.0);
	}

	public MatE getH() {
		return h;
	}
	
	public MatE getS() {
		return s;
	}
	
	public MatE getI() {
		return i;
	}

	public void release() {
		h.release();
		s.release();
		i.release();
	}

}
