package main;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Recuperacion {

	Mat centers;
	double[] valRef;
	Mat MIndex;

	public Recuperacion(double[] valRef, Mat centers, Mat MIndex) {

		this.centers = centers;
		this.valRef = valRef;
		this.MIndex = MIndex;

	}

	/* Funcion que genera la matriz de referencia para la imagen analizada */
	public static void generaMatRef(Mat matRef, Mat image) {
		// TODO Auto-generated method stub
		Mat h = Mat.zeros(image.rows(), image.cols(), CvType.CV_64F);
		Mat s = Mat.zeros(image.rows(), image.cols(), CvType.CV_64F);
		Mat i = Mat.zeros(image.rows(), image.cols(), CvType.CV_64F);
		rgb2hsi(image, h, s, i);

	}

	// ++++++++++funcion que convierte una imagen RGH a HSI
	private static void rgb2hsi(Mat image, Mat h, Mat s, Mat i) {
		// TODO Auto-generated method stub
		Mat salida = new Mat();
		List<Mat> mRgb = new ArrayList<Mat>(3);
		Core.split(image, mRgb);// separa la la imagen por canales
		Mat mR = mRgb.get(0);// obtiene el canalR
		Mat mG = mRgb.get(1);// obtiene el canalG
		Core.add(mR, mG, salida);

	}

	public static void norMatRef(Mat matRef, double val[]) {
	}

}
