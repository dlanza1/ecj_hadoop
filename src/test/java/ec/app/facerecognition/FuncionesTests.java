package ec.app.facerecognition;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class FuncionesTests {

	public FuncionesTests() {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@Test
	public void acos() {
		for (int i = 0; i < 10000; i++) {
			double ang = Math.random() * 2 - 1;

			Mat m = Mat.zeros(1, 1, CvType.CV_64F);
			m.put(0, 0, ang);

			double exacto = Math.acos(m.get(0, 0)[0]);
			double aprox1 = acos_aprox(m).get(0, 0)[0];
			double aprox2 = Procesa.acos(m).get(0, 0)[0];

			// Comprobar aproximaciones
			assertEquals(exacto, aprox1, 0.18);
			assertEquals(exacto, aprox2, 0.000068);

			// Comprobar mismos rsultados con y sin Mat
			assertEquals(aprox1, acos_aprox(m.get(0, 0)[0]), 0.000000000000001);
			assertEquals(aprox2, acos(m.get(0, 0)[0]), 0.000000000000001);
		}
	}
	
	@Test
	public void getAnguloLoop(){
		double loops = 100000;
	
		double count = 0;
		for (int i = 0; i < loops; i++) {
			count += getAngulo();
		}
		System.out.println("Errors " + (count/loops*100) + "%");
		
		assertFalse( (count/loops*100) > 2);
	}
	
	public int getAngulo(){
		double rad1 = Math.random() * 2 - 1;
		double rad2 = Math.random() * 2 - 1;
		double rad3 = Math.random() * 2 - 1;

		Mat m1 = Mat.zeros(1, 1, CvType.CV_64F);
		m1.put(0, 0, rad1);
		Mat m2 = Mat.zeros(1, 1, CvType.CV_64F);
		m2.put(0, 0, rad2);
		Mat m3 = Mat.zeros(1, 1, CvType.CV_64F);
		m3.put(0, 0, rad3);
		
		try{																									   
			assertEquals(Procesa.getAngulo(m1, m2, m3).get(0, 0)[0], getAngulo_original(m1, m2, m3).get(0, 0)[0], 0.000068);
		}catch(AssertionError e){
			return 1;
		}
		return 0;
	}
	
	@Test
	public void acos_times(){
		int loops = 100000;
		
		long time = System.currentTimeMillis();
		
		for (int i = 0; i < loops; i++) {
			double ang = Math.random() * 2 - 1;

			Mat m = Mat.zeros(1, 1, CvType.CV_64F);
			m.put(0, 0, ang);

			double aprox1 = acos_aprox(m).get(0, 0)[0];
		}
		
		System.out.println((System.currentTimeMillis() - time)/(double)loops + " ms (acos_aprox1)");
		time = System.currentTimeMillis();
		
		for (int i = 0; i < loops; i++) {
			double ang = Math.random() * 2 - 1;

			Mat m = Mat.zeros(1, 1, CvType.CV_64F);
			m.put(0, 0, ang);

			double aprox1 = Procesa.acos(m).get(0, 0)[0];
		}
		
		System.out.println((System.currentTimeMillis() - time)/(double)loops + " ms (acos_aprox2)");
	}

	/**
	 * Funcion que calcula el acos
	 * 
	 * Muy rapida, con error absoluto de 6.8e-5
	 * 
	 * Extraida de su version en Nvidia CUDA: http://http.developer.nvidia.com/Cg/acos.html
	 * Autors: M. Abramowitz and I.A. Stegun, Ed.
	 * 
	 * @param x
	 * @return
	 */
	private double acos(double x) {
		double negate = x <= 0 ? 1 : 0;
		x = Math.abs(x);
		double ret = -0.0187293;
		ret = ret * x;
		ret = ret + 0.0742610;
		ret = ret * x;
		ret = ret - 0.2121144;
		ret = ret * x;
		ret = ret + 1.5707288;
		ret = ret * Math.sqrt(1.0 - x);
		ret = ret - 2 * negate * ret;
		return negate * 3.14159265358979 + ret;
	}

	@Test
	public void homogeinity() throws IOException {
		Mat rand = Mat.zeros(11, 11, CvType.CV_64F);
		Core.randu(rand, 0, 1);
		
		double ori = homogeinity(rand);
		double improved = Procesa.homogeinity(rand);
		
		assertEquals(ori, improved, 0.001);
	}

	/**
	 * Funcion original que calcula la homogeneidad de una ventana
	 * 
	 * @param ROI
	 * @return homogeneidad
	 */
	private double homogeinity(Mat ROI) {
		Mat ROI_conv = new Mat();
		ROI.convertTo(ROI_conv, CvType.CV_8U, 255, 0);
		
		Mat mC = Mat.zeros(256, 256, CvType.CV_64F);
		Mat mCP = Mat.zeros(256, 256, CvType.CV_64F);
		int cont = 0;
		// System.out.println("tamaño ROI:"+ROI.rows()+"x"+ROI.cols());
		for (int i = 0; i < ROI_conv.rows(); i++) {
			for (int j = 0; j < ROI_conv.cols() - 1; j++) {
				double[] dato1 = ROI_conv.get(i, j);
				double[] dato2 = ROI_conv.get(i, j + 1);
				double[] valor = mC.get((int) dato1[0], (int) dato2[0]);
				valor[0] = valor[0] + 1;
				double[] valor1 = mC.get((int) dato2[0], (int) dato1[0]);
				valor1[0] = valor1[0] + 1;
				mC.put((int) dato1[0], (int) dato2[0], valor[0]);
				mC.put((int) dato2[0], (int) dato1[0], valor1[0]);
				cont = cont + 2;
			}
		}

		for (int i = 0; i < 255; i++) {
			for (int j = 0; j < 255; j++) {
				double[] val = mC.get(i, j);
				val[0] = val[0] / cont;
				mCP.put(i, j, val);
			}
		}

		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double[] val = mCP.get(i, j);
				val[0] = val[0] / (1 + Math.abs(i - j));
				mCP.put(i, j, val);
			}
		}

		double hom = 0;

		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double[] val = mCP.get(i, j);
				hom = hom + val[0];
			}
		}

		return hom;
	}

	/**
	 * Funcion original que convierte una imagen RGB a HSI
	 * 
	 * @param image
	 * @param h
	 * @param s
	 * @param i
	 */
	private void rgb2hsi(Mat image, Mat h, Mat s, Mat i) {
		double EP = 0.00000001;
		Scalar epsilon = new Scalar(EP);

		List<Mat> mRgb = new ArrayList<Mat>(3);
		Core.split(image, mRgb);// separa la la imagen por canales
		Mat mR = mRgb.get(2);// obtiene el canalR
		Mat mG = mRgb.get(1);// obtiene el canalG
		Mat mB = mRgb.get(0);// obtiene el canalB

		Mat parcial = new Mat(), parcial1 = new Mat(), parcial2 = new Mat(), parcial3 = new Mat(), num = new Mat(), rg = new Mat();
		Mat mulRGB = new Mat(), teta = new Mat(), den = new Mat(), minimo = new Mat(), suma = new Mat();
		Mat angulo = Mat.zeros(mR.rows(), mR.cols(), CvType.CV_64F);
		Mat comparacion = Mat.zeros(mR.rows(), mR.cols(), CvType.CV_64F);
		Core.subtract(mR, mG, parcial);
		Core.subtract(mR, mB, parcial1);

		Core.add(parcial, parcial1, h);
		Scalar multiplo = new Scalar(0.5);
		Scalar tres = new Scalar(3.0);
		Core.multiply(h, multiplo, num);
		Core.pow(parcial, 2, rg);
		Core.subtract(mG, mB, parcial2);
		Core.multiply(parcial1, parcial2, mulRGB);
		Core.add(rg, mulRGB, parcial3);
		Core.sqrt(parcial3, den);
		Core.add(den, epsilon, den);
		Core.divide(num, den, teta);

		for (int k = 0; k < teta.rows(); k++) {
			for (int j = 0; j < teta.cols(); j++) {
				double[] pixel = teta.get(k, j);
				pixel[0] = Math.acos(pixel[0]);
				angulo.put(k, j, pixel);
			}
		}

		for (int k = 0; k < teta.rows(); k++) {
			for (int j = 0; j < teta.cols(); j++) {
				double[] pixelB = mB.get(k, j);
				double[] pixelG = mG.get(k, j);
				double[] pixelCom, pixelAng;
				if (pixelB[0] > pixelG[0]) {
					pixelCom = comparacion.get(k, j);
					pixelCom[0] = 1.0;
					comparacion.put(k, j, pixelCom);
					pixelAng = angulo.get(k, j);
					pixelAng[0] = (2 * Math.PI) - pixelAng[0];
					angulo.put(k, j, pixelAng);
				}
			}
		}

		angulo.convertTo(angulo, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(angulo, h);
		h.convertTo(h, CvType.CV_64F, 1.0 / 255.0);
		Scalar valorPi = new Scalar(2 * Math.PI);
		Core.divide(h, valorPi, h);
		Core.min(mR, mG, minimo);
		Core.min(minimo, mB, minimo);
		Core.add(mR, mG, suma);
		Core.add(suma, mB, suma);
		Core.add(suma, epsilon, suma);
		Core.divide(minimo, suma, s);
		Scalar valorS = new Scalar(-3.0);
		Scalar valorS1 = new Scalar(-1.0);
		Core.multiply(s, valorS, s);
		Core.subtract(s, valorS1, s);

		Mat pI = new Mat();
		Core.add(mR, mG, pI);
		Core.add(mB, pI, pI);
		Core.divide(pI, tres, i);
		i.convertTo(i, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(i, i);
		i.convertTo(i, CvType.CV_64F, 1.0 / 255.0);
		s.convertTo(s, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(s, s);
		s.convertTo(s, CvType.CV_64F, 1.0 / 255.0);
	}

	/**
	 * Calculo aproximado de acos
	 * 
	 * A simple cubic approximation, the Lagrange polynomial for x ∈ {-1,-½, 0, ½, 1}
	 * 
	 * Muy rapida pero con un error de 0.18 rad.
	 * 
	 * @param in Angulo de entrada
	 * @return Radianes de aplicar acos a la entrada
	 */
	public double acos_aprox(double in) {
		return (-0.69813170079773212 * in * in - 0.87266462599716477) * in + 1.5707963267948966;
	}

	/**
	 * Version para OpenCV de calculo aproximado de acos
	 * 
	 * A simple cubic approximation, the Lagrange polynomial for x ∈ {-1,-½, 0, ½, 1}
	 * 
	 * Muy rapida pero con un error de 0.18 rad.
	 * 
	 * @param in Matriz de entrada
	 * @return Matriz de salida donde a cada elemento se le ha aplicado el acos
	 */
	public static Mat acos_aprox(Mat in) {
		Mat out = new Mat();
		
		Core.multiply(in, in, out);
		Core.multiply(out, new Scalar(-0.69813170079773212), out);
		Core.add(out, new Scalar(-0.87266462599716477d), out);
		Core.multiply(out, in, out);
		Core.add(out, new Scalar(1.5707963267948966d), out);

		return out;
	}
	
	/**
	 * Funcion origianl de obtencion de angulo de la imagen
	 * 
	 * @param teta
	 * @param mB Canal azul de la imagen
	 * @param mG Canal verde de la imagen
	 * @return
	 */
	private Mat getAngulo_original(Mat teta, Mat mB, Mat mG) {
		
		Mat angulo = Mat.zeros(mB.rows(), mB.cols(), CvType.CV_64F);
			
		for (int k = 0; k < teta.rows(); k++) {
			double[] angulo_row = new double[angulo.cols()];
			
			for (int j = 0; j < teta.cols(); j++) {
				if (mB.get(k, j)[0] > mG.get(k, j)[0]) {
					angulo_row[j] = 2d * Math.PI - Math.acos(teta.get(k, j)[0]);
				}else{
					angulo_row[j] = Math.acos(teta.get(k, j)[0]);
				}
			}
			
			angulo.put(k, 0, angulo_row);
		}
		
		return angulo;
	}
}
