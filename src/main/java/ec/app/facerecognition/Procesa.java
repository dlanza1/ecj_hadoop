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

	/* Funcion que genera la matriz de referencia para la imagen analizada */
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
			String rutaNombres = "nombres.csv";// ruta al archivo de nombres
			String rutaPuntos = "datos.csv"; // ruta al archivo de coordenadas
												// (x,y) de cada PI
			nombreImagen = archivo.abrir(rutaNombres);
			puntosImagen = archivo.abrir(rutaPuntos);

			for (int j = 0; j < 10; j++) {
				long image_time = System.currentTimeMillis();
				
				lineaNombre = nombreImagen.readLine();// lee la imagen que se
														// procesara
				System.out.println("\nimagen: " + lineaNombre);

//				image = Highgui.imread(Aplicacion.class.getResource("imagenes/" + lineaNombre).getPath());
				image = Highgui.imread("imagenes/" + lineaNombre);

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

						puntos[numTokens - 32] = (int) Float
								.parseFloat(palabraPuntos);// se almacenan las
															// coordenadas (x,y)
					} else
						palabraPuntos = st.nextToken();
					numTokens++;
				}

				image.convertTo(image, CvType.CV_64FC(3), 1.0 / 255.0);// convierte
																		// la
																		// imagen
																		// en
																		// RGB
																		// tipo
																		// double
				
				long time = System.currentTimeMillis();
				
				Mat h = new Mat();
				Mat s = new Mat();
				Mat i = new Mat();
				rgb2hsi_improved(image, h, s, i); // convierte la imagen RGB a HSI
				
				System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi) ");
				
				//Comprobacion de que dan lo mismo
//				Mat h_test = new Mat();
//				Mat s_test = new Mat();
//				Mat i_test = new Mat();
//				rgb2hsi(image, h_test, s_test, i_test);
//				if(!compare(h, h_test) || !compare(h, h_test) || !compare(h, h_test))
//					System.err.println("error!");
				
				time = System.currentTimeMillis();
				
				llenaMatRef(j, matRef, puntos, h, s, i);// Llena la matriz de
														// referencia
				
				System.out.println(System.currentTimeMillis() - time + " ms (llenaMatRef) ");
				time = System.currentTimeMillis();
				
				System.out.println(System.currentTimeMillis() - image_time + " ms (total imagen) ");
				image_time = System.currentTimeMillis();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

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

	// ++++++++++funcion que convierte una imagen RGH a HSI
	private void rgb2hsi_improved(Mat image, Mat h, Mat s, Mat i) {
		double EP = 0.00000001;
		Scalar epsilon = new Scalar(EP);

		List<Mat> mRgb = new ArrayList<Mat>(3);
		Core.split(image, mRgb);// separa la imagen por canales
		Mat mR = mRgb.get(2);// obtiene el canalR
		Mat mG = mRgb.get(1);// obtiene el canalG
		Mat mB = mRgb.get(0);// obtiene el canalB
		// double [] valor;

		Mat parcial = new Mat(), parcial1 = new Mat(), parcial2 = new Mat(), parcial3 = new Mat(), num = new Mat(), rg = new Mat();
		Mat mulRGB = new Mat(), teta = new Mat(), den = new Mat(), minimo = new Mat(), suma = new Mat();
		Mat angulo = Mat.zeros(mR.rows(), mR.cols(), CvType.CV_64F);
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
		
		
		long time = System.currentTimeMillis();
		
		double two_times_pi = 2 * Math.PI;
		for (int k = 0; k < teta.rows(); k++) {
			double[] angulo_row = new double[angulo.cols()];
			
			for (int j = 0; j < teta.cols(); j++) {
				if (mB.get(k, j)[0] > mG.get(k, j)[0]) {
					angulo_row[j] = two_times_pi - Math.acos(teta.get(k, j)[0]);
				}else{
					angulo_row[j] = Math.acos(teta.get(k, j)[0]);
				}
			}
			
			angulo.put(k, 0, angulo_row);
		}
		
		System.out.println(System.currentTimeMillis() - time + " ms (for inside rgb2hsi) ");
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

	// ++++++++++funcion que convierte una imagen RGH a HSI
	private void rgb2hsi(Mat image, Mat h, Mat s, Mat i) {
		double EP = 0.00000001;
		Scalar epsilon = new Scalar(EP);

		List<Mat> mRgb = new ArrayList<Mat>(3);
		Core.split(image, mRgb);// separa la la imagen por canales
		Mat mR = mRgb.get(2);// obtiene el canalR
		Mat mG = mRgb.get(1);// obtiene el canalG
		Mat mB = mRgb.get(0);// obtiene el canalB
		
		// double [] valor;

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

	// carga la informacion de extraida de cada imagen en la
	private void llenaMatRef(int index, Mat matRef, int puntos[], Mat H, Mat S, Mat I) {
		
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

			// extraccion de la Homogeneidad de la ventana
//			ROI_H.convertTo(ROI_H, CvType.CV_8U, 255, 0);
			homH = homogeinity_improved(ROI_H); 
//			System.err.println(homogeinity_improved(ROI_H) + " -- " + homogeinity(ROI_H));
//			ROI_S.convertTo(ROI_S, CvType.CV_8U, 255, 0);
			homS = homogeinity_improved(ROI_S); //if(homS != homogeinity_improved(ROI_S)) System.err.println("error");
//			ROI_I.convertTo(ROI_I, CvType.CV_8U, 255, 0);
			homI = homogeinity_improved(ROI_I); //if(homI != homogeinity_improved(ROI_I)) System.err.println("error");
			// System.out.println("punto"+i+"	promH:"+mH[0]+"	promS:"+mS[0]+"	promI:"+mI[0]+"	desvH:"+stdH[0]+"	desvS:"+stdS[0]+"	desvI:"+stdI[0]+"	hH:"+homH+"	hS:"+homS+"	hI:"+homI);

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

	// Funcion que calcula la homogeneidad de una ventana
	private double homogeinity_improved(Mat ROI) {
		
		ROI.convertTo(ROI, CvType.CV_8U, 255, 0);
		
		int cont = ROI.rows() * (ROI.cols() - 1) * 2;
		double hom = 0;
		
		for (int i = 0; i < ROI.rows(); i++) {
			for (int j = 0; j < ROI.cols() - 1; j++) {
				int x = (int) ROI.get(i, j)[0];
				int y = (int) ROI.get(i, j + 1)[0];
				
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
	
	// Funcion que calcula la homogeneidad de una ventana
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

	// normaliza la matriz de referencia
	public void norMatRef(Mat matRef, Mat matNor, double val[]) {
		Mat ROI = new Mat();
		for (int i = 0; i < matRef.cols(); i++) {
			ROI = matRef.submat(0, matRef.rows(), i, i + 1);
			MinMaxLocResult mmr = Core.minMaxLoc(ROI);
			val[i] = mmr.maxVal;
			// System.out.println("valorMax:"+val[i]);
		}

		// obitnene el valor maximo de cada columna de la matriz de referencia
		for (int i = 0; i < matRef.cols(); i++) {
			for (int j = 0; j < matRef.rows(); j++) {
				double[] valor = matRef.get(j, i);
				valor[0] = valor[0] / val[0];
				matNor.put(j, i, valor);

			}
		}
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

	// genera la matriz de indices de textura para la base de datos
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
