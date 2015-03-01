/*
 * Esta clase es la encargada de el procesamiento de las imágenes
 * 
 *  se lee cada una de las imagenes
 *  se leen los 60 puntos de interés (PI)
 *  
 *  y 
 * */

package ec.app.facerecognition;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Procesa {
	/**
	 * Epsilon
	 */
	public static Scalar EP = new Scalar(Double.MIN_VALUE);

	/**
	 * Genera la matriz de referencia
	 * 
	 * @param matRef
	 */
	public void generaMatRef(Mat matRef) {
		BufferedReader nombreImagen = null;
		BufferedReader puntosImagen = null;
		Mat image = new Mat();
		int[] puntos = new int[120];// arreglo que guarda los 60 Pi
		try {
			String lineaNombre;
			String lineaPuntos;
			String palabraPuntos;
			Ficheros archivo = new Ficheros();
			// Lectura del fichero
			String rutaNombres = "src/main/java/ec/app/facerecognition/res/nombres.csv";// ruta al archivo de nombres
			String rutaPuntos = "src/main/java/ec/app/facerecognition/res/datos.csv"; // ruta al archivo de coordenadas
												// (x,y) de cada PI
			nombreImagen = archivo.abrir(rutaNombres);
			puntosImagen = archivo.abrir(rutaPuntos);

			for (int j = 0; j < 10; j++) {
				long image_time = System.currentTimeMillis();
				
				lineaNombre = nombreImagen.readLine();// lee la imagen que se
														// procesara
				System.out.println("\nimagen: " + lineaNombre);
				
				image = Highgui.imread("src/main/java/ec/app/facerecognition/img/" + lineaNombre);

				lineaPuntos = puntosImagen.readLine();// lee la linea de puntos
														// de la imagen

				int numTokens = 0; // toquen para guardar los 60 puntos
				StringTokenizer st = new StringTokenizer(lineaPuntos); // bucle
																		// para
																		// extraer
																		// los
																		// PI
				
				while (st.hasMoreTokens()) {
					if (numTokens >= 32) {
						palabraPuntos = st.nextToken();

						puntos[numTokens - 32] = (int) Float.parseFloat(palabraPuntos);
					} else
						palabraPuntos = st.nextToken();
					numTokens++;
				}

				//To RGB tipo Double
				image.convertTo(image, CvType.CV_64FC(3), 1.0 / 255.0);
				
				long time = System.currentTimeMillis();
				
				Mat h = new Mat();
				Mat s = new Mat();
				Mat i = new Mat();
				rgb2hsi(image, h, s, i);
				
				System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi) ");
				time = System.currentTimeMillis();
				
				llenaMatRef(j, matRef, puntos, h, s, i);
				
				System.out.println(System.currentTimeMillis() - time + " ms (llenaMatRef) ");
				
				System.out.println(System.currentTimeMillis() - image_time + " ms (total imagen) ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private boolean compare(Mat m1, Mat m2, String mes, Mat image) {
		for (int k = 0; k < m1.rows(); k++) {
			for (int j = 0; j < m2.cols(); j++) {
				if(Math.abs(m1.get(k, j)[0] - m2.get(k, j)[0]) > 0.001){
					System.out.println(k + "-" + j + "-" + mes + "-" + image.get(k, j)[0]  
							+ "    " + m1.get(k, j)[0] + ",,,, " + m2.get(k, j)[0]);
				}
			}
		}
		
		return true;
	}

	/**
	 * funcion que convierte una imagen RGH a HSI
	 * 
	 * @param image
	 * @param h
	 * @param s
	 * @param i
	 * @param pass 
	 */
	public void rgb2hsi(Mat image, Mat h, Mat s, Mat i) {
		
		long time = System.currentTimeMillis();
		
		List<Mat> mRgb = new ArrayList<Mat>(3);
		Core.split(image, mRgb);// separa la imagen por canales
		Mat mR = mRgb.get(2);// obtiene el canalR
		Mat mG = mRgb.get(1);// obtiene el canalG
		Mat mB = mRgb.get(0);// obtiene el canalB
		// double [] valor;

		Mat parcial = new Mat(), parcial1 = new Mat(), parcial2 = new Mat(), parcial3 = new Mat(), num = new Mat(), rg = new Mat();
		Mat mulRGB = new Mat(), teta = new Mat(), den = new Mat(), minimo = new Mat(), suma = new Mat();
		Core.subtract(mR, mG, parcial);
		Core.subtract(mR, mB, parcial1);
		Core.add(parcial, parcial1, h);
		Core.multiply(h, new Scalar(0.5), num);
		Core.pow(parcial, 2, rg);
		Core.subtract(mG, mB, parcial2);
		Core.multiply(parcial1, parcial2, mulRGB);
		Core.add(rg, mulRGB, parcial3);
		Core.sqrt(parcial3, den);
		Core.add(den, EP, den);
		Core.divide(num, den, teta);
		
		System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi-1) ");
		time = System.currentTimeMillis();
		
		Mat angulo = getAngulo(teta, mB, mG);
		
		System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi-2) ");
		time = System.currentTimeMillis();
			
		angulo.convertTo(angulo, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(angulo, h);
		h.convertTo(h, CvType.CV_64F, 1.0 / 255.0);
		Scalar valorPi = new Scalar(2 * Math.PI);
		Core.divide(h, valorPi, h);
		Core.min(mR, mG, minimo);
		Core.min(minimo, mB, minimo);
		Core.add(mR, mG, suma);
		Core.add(suma, mB, suma);
		Core.add(suma, EP, suma);
		Core.divide(minimo, suma, s);
		Scalar valorS = new Scalar(-3.0);
		Scalar valorS1 = new Scalar(-1.0);
		Core.multiply(s, valorS, s);
		Core.subtract(s, valorS1, s);
		
		System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi-3) ");
		time = System.currentTimeMillis();

		Mat pI = new Mat();
		Core.add(mR, mG, pI);
		Core.add(mB, pI, pI);
		Core.divide(pI, new Scalar(3.0), i);
		i.convertTo(i, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(i, i);
		i.convertTo(i, CvType.CV_64F, 1.0 / 255.0);
		
		s.convertTo(s, CvType.CV_8U, 255, 0);
		Imgproc.equalizeHist(s, s);
		s.convertTo(s, CvType.CV_64F, 1.0 / 255.0);
		
		System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi-4) ");
		time = System.currentTimeMillis();
		
	}
	
	public static Mat getAngulo(Mat teta, Mat mB, Mat mG) {
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

		Mat angulo = acos(teta);
		Core.multiply(angulo, sig_change, angulo);
		Core.add(mat_two_times_pi, angulo, angulo);
		return angulo;
	}

	/**
	 * Funcion adapatada a OpenCV que calcula el acos de forma aproximada.
	 * 
	 * Muy rapida pero con error absoluto de 6.8e-5
	 * 
	 * Extraida de su version en Nvidia CUDA: http://http.developer.nvidia.com/Cg/acos.html
	 * Autors: M. Abramowitz and I.A. Stegun, Ed.
	 * 
	 * @param x
	 * @return
	 */
	public static Mat acos(Mat in) {
//		double negate = x <= 0 ? 1 : 0;
		Mat negate = new Mat();
		Core.inRange(in, new Scalar(-1), new Scalar(0), negate);
		Core.divide(negate, new Scalar(255), negate);
		negate.convertTo(negate, CvType.CV_64F);
//		x = Math.abs(x);
		Mat in_abs = new Mat();
		Core.absdiff(in, new Scalar(0), in_abs);
//		double ret = -0.0187293;
//		ret = ret * x;
		Mat ret = new Mat();
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
		return ret;
	}

	/**
	 * Carga la informacion de extraida de cada imagen en la matriz de referencia
	 * 
	 * @param index
	 * @param matRef
	 * @param puntos
	 * @param H
	 * @param S
	 * @param I
	 */
	public void llenaMatRef(int index, Mat matRef, int puntos[], Mat H, Mat S, Mat I) {
		
		int p = 5;
		Mat ROI_H = new Mat(), ROI_S = new Mat(), ROI_I = new Mat(), cH = new Mat();
		MatOfDouble meanH = new MatOfDouble(), meanS = new MatOfDouble(), meanI = new MatOfDouble();
		MatOfDouble stdevH = new MatOfDouble(), stdevS = new MatOfDouble(), stdevI = new MatOfDouble();
		double homH, homS, homI;
		int posicion, y, x;

		for (int i = 0; i < 60; i++) { // para cada uno de los 60 PI de cada
										// imagen
			cH = H;
			y = puntos[(i * 2) + 1];// renglon
			x = puntos[i * 2];// columna

			posicion = index * 60 + i;
			// extraccion de la vecindad al rededor del PI para cada una de las
			// capas HSI
			ROI_H = cH.submat(y - p, y + (p + 1), x - p, x + (p + 1));
			ROI_S = S.submat(y - p, y + (p + 1), x - p, x + (p + 1));
			ROI_I = I.submat(y - p, y + (p + 1), x - p, x + (p + 1));

			// extraccion de la media y desviacion estandar de cada ventana
			Core.meanStdDev(ROI_H, meanH, stdevH);
			Core.meanStdDev(ROI_S, meanS, stdevS);
			Core.meanStdDev(ROI_I, meanI, stdevI);

			double[] mH = meanH.toArray();
			double[] mS = meanS.toArray();
			double[] mI = meanI.toArray();
			matRef.put(posicion, 0, mH[0]);
			matRef.put(posicion, 1, mS[0]);
			matRef.put(posicion, 2, mI[0]);

			double[] stdH = stdevH.toArray();
			double[] stdS = stdevS.toArray();
			double[] stdI = stdevI.toArray();
			matRef.put(posicion, 3, stdH[0]);
			matRef.put(posicion, 4, stdS[0]);
			matRef.put(posicion, 5, stdI[0]);

			homH = homogeinity(ROI_H); 
			homS = homogeinity(ROI_S);
			homI = homogeinity(ROI_I);

			double[] valhH = matRef.get(posicion, 6);
			double[] valhS = matRef.get(posicion, 7);
			double[] valhI = matRef.get(posicion, 8);
			valhH[0] = homH;
			valhS[0] = homS;
			valhI[0] = homI;
			matRef.put(posicion, 6, valhH);
			matRef.put(posicion, 7, valhS);
			matRef.put(posicion, 8, valhI);
		}
	}

	/**
	 * Funcion que calcula la homogeneidad de una ventana
	 * 
	 * @param ROI
	 * @return Homogeneidad
	 */
	public static double homogeinity(Mat ROI) {
		Mat ROI_conv = new Mat();
		ROI.convertTo(ROI_conv, CvType.CV_8U, 255, 0);
		
		int cont = ROI_conv.rows() * (ROI_conv.cols() - 1) * 2;
		double hom = 0;
		
		for (int i = 0; i < ROI_conv.rows(); i++) {
			for (int j = 0; j < ROI_conv.cols() - 1; j++) {
				int x = (int) ROI_conv.get(i, j)[0];
				int y = (int) ROI_conv.get(i, j + 1)[0];
				
				if(x >= 254 && y >= 254)
					continue;
				
				if(x != y)
					hom += 2d / (cont * (1 + Math.abs(x - y)));
				else
					hom += 1d / (cont * (1 + Math.abs(x - y)));
			}
		}

		return hom;
	}

	/**
	 * Normaliza la matriz de referencia
	 * 
	 * @param matRef
	 * @param matNor
	 * @param val
	 */
	public void norMatRef(Mat matRef, Mat matNor, double val[], int bandera) {
		Mat valor=Mat.zeros(1, 9,CvType.CV_64F);
		if (bandera==0)
		{	Mat ROI = new Mat();
		
			for (int i = 0; i < matRef.cols(); i++) {
				ROI = matRef.submat(0, matRef.rows(), i, i + 1);
				MinMaxLocResult mmr = Core.minMaxLoc(ROI);
				val[i] = mmr.maxVal;
				valor.put(0, i, mmr.maxVal);
				// System.out.println("valorMax:"+val[i]);
			}
		}
		// obitnene el valor maximo de cada columna de la matriz de referencia
		for (int i = 0; i < matRef.cols(); i++) {
			for (int j = 0; j < matRef.rows(); j++) {
				matNor.put(j, i, matRef.get(j, i)[0] / val[0]);

			}
		}

		System.out.println("valores:"+valor.get(0, 0)[0]+valor.get(0, 1)[0]+","+valor.get(0, 2)[0]+","+valor.get(0, 3)[0]+","+valor.get(0, 4)[0]+","+valor.get(0, 5)[0]+","+valor.get(0, 6)[0]+","+valor.get(0, 7)[0]+","+valor.get(0, 8)[0]);
		System.out.println(" valref:"+val[0]+","+val[1]+","+val[2]+","+val[3]+","+val[4]+","+val[5]+","+val[6]+","+val[7]+","+val[8]);
	}

	// Aplica el clasificador K-means para obtener la matriz de indices de
	// textura
	public void generaCK_Mindex(Mat matNor, Mat centers, Mat labels, Mat MIndex) {
		Mat MNor = new Mat();
		matNor.convertTo(MNor, CvType.CV_32F);
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS
				+ TermCriteria.MAX_ITER, 10000, 0.0001);
		Core.kmeans(MNor, 10, labels, criteria, 1, Core.KMEANS_RANDOM_CENTERS,
				centers);
		mIndexada(labels, 1, 10, MIndex);
	}

	/**
	 * Genera la matriz de indices de textura para la base de datos
	 * 
	 * @param labels
	 * @param nImag
	 * @param k
	 * @param mIdx
	 */
	private void mIndexada(Mat labels, int nImag, int k, Mat mIdx) {

		// Mat mIdx=Mat.zeros(nImag, k, CvType.CV_32F);

		int pos;
		for (int i = 0; i < nImag; i++) {
			pos = 60 * i;
			for (int j = pos; j < pos + 60; j++) {
				double[] col = labels.get(j, 0);
				double[] valor = mIdx.get(i, (int) col[0]);
				valor[0] = valor[0] + 1;
				mIdx.put(i, (int) col[0], valor);

			}
		}

	}

}
