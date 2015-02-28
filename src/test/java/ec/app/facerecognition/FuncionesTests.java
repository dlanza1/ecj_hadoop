package ec.app.facerecognition;

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
	public void rgb2hsi() {
		Mat image = Highgui.imread("src/main/java/ec/app/facerecognition/img/i000qa-fn.jpg");

		image.convertTo(image, CvType.CV_64FC(3), 1.0 / 255.0);
		
		Mat H = new Mat();
		Mat S = new Mat();
		Mat I = new Mat();
		rgb2hsi(image, H, S, I);
	}

	@Test
	public void homogeneidad() throws IOException {
		Mat image = Highgui.imread("src/main/java/ec/app/facerecognition/img/i000qa-fn.jpg");
		
		int numTokens = 0;
		String lineaPuntos;
		String palabraPuntos;
		int[] puntos = new int[120];
		Ficheros archivo = new Ficheros();
		String rutaPuntos = "src/main/java/ec/app/facerecognition/res/datos.csv";
		BufferedReader puntosImagen = null;
		puntosImagen = archivo.abrir(rutaPuntos);
		lineaPuntos = puntosImagen.readLine();
		StringTokenizer st = new StringTokenizer(lineaPuntos);
		while (st.hasMoreTokens()) {
			if (numTokens >= 32) {
				palabraPuntos = st.nextToken();

				puntos[numTokens - 32] = (int) Float
						.parseFloat(palabraPuntos);// se almacenan las
													// coordenadas (x,y)
			} else
				palabraPuntos = st.nextToken();
			numTokens++;
		}
		
		//Pasar a Double
		image.convertTo(image, CvType.CV_64FC(3), 1.0 / 255.0);

		Mat H = new Mat();
		Mat S = new Mat();
		Mat I = new Mat();
		rgb2hsi(image, H, S, I);

		
		Mat ROI_H = new Mat(), ROI_S = new Mat(), ROI_I = new Mat(), cH = new Mat();
		MatOfDouble meanH = new MatOfDouble(), meanS = new MatOfDouble(), meanI = new MatOfDouble();
		MatOfDouble stdevH = new MatOfDouble(), stdevS = new MatOfDouble(), stdevI = new MatOfDouble();
		double homH, homS, homI;
		int posicion, y, x;
		
		int i = 1;//numero del punto
		y = puntos[(i * 2) + 1];// renglon
		x = puntos[i * 2];// columna

		// extraccion de la vecindad al rededor del PI para cada una de las capas HSI
		int p = 5;
		cH = H;
		ROI_H = cH.submat(y - p, y + (p + 1), x - p, x + (p + 1));
		ROI_S = S.submat(y - p, y + (p + 1), x - p, x + (p + 1));
		ROI_I = I.submat(y - p, y + (p + 1), x - p, x + (p + 1));

		// extraccion de la media y desviacion estandar de cada ventana
		Core.meanStdDev(ROI_H, meanH, stdevH);
		Core.meanStdDev(ROI_S, meanS, stdevS);
		Core.meanStdDev(ROI_I, meanI, stdevI);

		// extraccion de la Homogeneidad de la ventana
		// ROI_H.convertTo(ROI_H, CvType.CV_8U, 255, 0);
		homH = homogeinity(ROI_H);
		// System.err.println(homogeinity_improved(ROI_H) + " -- " +
		// homogeinity(ROI_H));
		// ROI_S.convertTo(ROI_S, CvType.CV_8U, 255, 0);
		homS = homogeinity(ROI_S); // if(homS != homogeinity_improved(ROI_S))
									// System.err.println("error");
		// ROI_I.convertTo(ROI_I, CvType.CV_8U, 255, 0);
		homI = homogeinity(ROI_I); // if(homI != homogeinity_improved(ROI_I))
									// System.err.println("error");
		// System.out.println("punto"+i+"	promH:"+mH[0]+"	promS:"+mS[0]+"	promI:"+mI[0]+"	desvH:"+stdH[0]+"	desvS:"+stdS[0]+"	desvI:"+stdI[0]+"	hH:"+homH+"	hS:"+homS+"	hI:"+homI);

	}

	/**
	 * Funcion ORIGINAL que calcula la homogeneidad de una ventana
	 * 
	 * @param ROI
	 * @return homogeneidad
	 */
	@Deprecated
	private double homogeinity(Mat ROI) {

		Mat mC = Mat.zeros(256, 256, CvType.CV_64F);
		Mat mCP = Mat.zeros(256, 256, CvType.CV_64F);
		int cont = 0;
		// System.out.println("tamaño ROI:"+ROI.rows()+"x"+ROI.cols());
		for (int i = 0; i < ROI.rows(); i++) {
			for (int j = 0; j < ROI.cols() - 1; j++) {
				double[] dato1 = ROI.get(i, j);
				double[] dato2 = ROI.get(i, j + 1);
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
	 * Funcion ORIGINAL que convierte una imagen RGH a HSI
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
	 * Check if two Mat have the same size and content
	 * @param mat1 One Mat
	 * @param mat2 Other Mat
	 * @return True = same, False - not same
	 */
	private boolean compare(Mat mat1, Mat mat2) {
		if(mat1.rows() != mat2.rows() || mat1.cols() != mat2.cols())
			return false;
		
		for (int k = 0; k < mat1.rows(); k++) {
			for (int j = 0; j < mat1.cols(); j++) {
				if(mat1.get(k, j)[0] != mat2.get(k, j)[0])
					return false;
			}
		}
		
		return true;
	}
}
