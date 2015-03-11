package ec.app.facerecognition.sequential;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import ec.app.facerecognition.catalog.MatE;

public class Recuperacion {

	MatE centers;
	double[] valRef;
	MatE MIndex;
	MatE vecinosDistMIndex, vecinosIdMIndex;

	public Recuperacion(double[] valRef, Mat centers, Mat MIndex,
			Mat vecinosDistMIndex, Mat vecinosIdMindex) {

		this.centers = new MatE(centers);
		this.valRef = valRef;
		this.MIndex = new MatE(MIndex);
		this.vecinosDistMIndex = new MatE(vecinosDistMIndex);
		this.vecinosIdMIndex = new MatE(vecinosDistMIndex);

	}

	public Mat getVecinosIdMIndex() {
		return vecinosIdMIndex;
	}

	public Mat getVecinosDistMIndex() {
		return vecinosDistMIndex;
	}

	public void Consulta() throws IOException {

		BufferedReader nombreImagen = null;
		BufferedReader puntosImagen = null;
		MatE image = new MatE();
		MatE matRef = new MatE(Mat.zeros(60, 9, CvType.CV_64F));
		MatE matNor = new MatE(Mat.zeros(matRef.size(), CvType.CV_64F));
		MatE vectorConsulta =new MatE( Mat.zeros(1368, 10, CvType.CV_64F));
		MatE vecinosIdCenters = new MatE(Mat.zeros(60, 1, CvType.CV_32F));
		MatE vecinosDistCenters = new MatE(Mat.zeros(60, 1, CvType.CV_64F));

		int[] puntos = new int[120];// arreglo que guarda los 60 Pi

		String lineaNombre;
		String lineaPuntos;
		String palabraPuntos;
		Ficheros archivo = new Ficheros();
		// Lectura del fichero
		String rutaNombres = "src/main/java/ec/app/facerecognition/res/nombres.csv";
		String rutaPuntos = "src/main/java/ec/app/facerecognition/res/datos.csv";
		nombreImagen = archivo.abrir(rutaNombres);
		puntosImagen = archivo.abrir(rutaPuntos);

		for (int j = 0; j < 1368; j++) {
			long image_time = System.currentTimeMillis();

			lineaNombre = nombreImagen.readLine();// lee la imagen que se
													// procesara
			// System.out.println("\nimagen: " + lineaNombre);

			image = new MatE(Highgui.imread("res/img/" + lineaNombre));
			System.out.println("\nImagen rec " + j + ": " + "res/img/"
					+ lineaNombre);
			lineaPuntos = puntosImagen.readLine();// lee la linea de puntos de
													// la imagen

			int numTokens = 0; // toquen para guardar los 60 puntos
			StringTokenizer st = new StringTokenizer(lineaPuntos); // bucle

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

			Procesa proc = new Procesa();// objeto para procesar la imagen de
											// consult
//			long time = System.currentTimeMillis();

			Mat h = new Mat();
			Mat s = new Mat();
			Mat i = new Mat();
			proc.rgb2hsi(image, h, s, i);

			// System.out.println(System.currentTimeMillis() - time +
			// " ms (rgb2hsi) ");

//			time = System.currentTimeMillis();

			proc.llenaMatRef(0, matRef, puntos, h, s, i);// Llena la matriz de
															// referencia

			proc.norMatRef(matRef, matNor, valRef, 1);// normaliza la matriz de
														// consulta

			// System.out.println(System.currentTimeMillis() - time +
			// " ms (llenaMatRef) ");
//			time = System.currentTimeMillis();

			algoKNN(centers, vecinosIdCenters, vecinosDistCenters, matNor);

			// llenando el vector consulta
			for (int l = 0; l < 60; l++) {
				// vectorConsulta.at<uchar>(j,(int)vecinosIdsCenters.at<uchar>(i,0))=vectorConsulta.at<uchar>(j,(int)vecinosIdsCenters.at<uchar>(i,0))+1;
				vectorConsulta.put(j, (int) vecinosIdCenters.get(l, 0)[0], vectorConsulta.get(j, (int) vecinosIdCenters.get(l, 0)[0])[0] + 1);
			}

			// System.out.print("valor:"+vectorConsulta.get(j,0)[0]+","+vectorConsulta.get(j,1)[0]+","+vectorConsulta.get(j,2)[0]+","+vectorConsulta.get(j,3)[0]+","+vectorConsulta.get(j,4)[0]+","+vectorConsulta.get(j,5)[0]);
			// System.out.println(","+vectorConsulta.get(j,6)[0]+","+vectorConsulta.get(j,7)[0]+","+vectorConsulta.get(j,8)[0]+","+vectorConsulta.get(j,9)[0]);
			// System.out.println(System.currentTimeMillis() - image_time +
			// " ms (total imagen) ");

			image_time = System.currentTimeMillis();
		}

		algoKnnMI(MIndex, vecinosIdMIndex, vecinosDistMIndex, vectorConsulta, 5);

		PrintWriter out = new PrintWriter("recup.txt");
		for (int k = 0; k < 1368; k++) {
			// System.out.println("Resultado:"+vecinosIdMindex.get(k,0)[0]+","+vecinosIdMindex.get(k,1)[0]+","+vecinosIdMindex.get(k,2)[0]+","+vecinosIdMindex.get(k,3)[0]+","+vecinosIdMindex.get(k,4)[0]);
			out.println("Resultado:" + vecinosIdMIndex.get(k, 0)[0] + ","
					+ vecinosIdMIndex.get(k, 1)[0] + ","
					+ vecinosIdMIndex.get(k, 2)[0] + ","
					+ vecinosIdMIndex.get(k, 3)[0] + ","
					+ vecinosIdMIndex.get(k, 4)[0]);
		}

		out.close();

	}

	private void algoKNN(MatE centers, MatE vecinosIdCenters, MatE vecinosDistCenters, MatE matNor) {

		MatE A = new MatE(), C = new MatE();
		int num_centers;
		centers.convertTo(centers, CvType.CV_64F);

		num_centers = centers.rows();

		for (int l = 0; l < matNor.rows(); l++) {
			MatE B = new MatE(Mat.zeros(num_centers, matNor.cols(), CvType.CV_64F));
			MatE D = new MatE(Mat.zeros(num_centers, 1, CvType.CV_64F));
			MatE E = new MatE(Mat.zeros(num_centers, 1, CvType.CV_64F));
			MatE F = new MatE(Mat.zeros(num_centers, matNor.cols(), CvType.CV_8U));

			A = repMat(matNor, num_centers, l);

			Core.subtract(A, centers, B);
			Core.pow(B, 2.0, C);
			double suma;
			for (int i = 0; i < C.rows(); i++) {
				suma = 0;
				for (int j = 0; j < C.cols(); j++)
					suma = suma + C.get(i, j)[0];
				D.put(i, 0, suma);
			}

			Core.sortIdx(D, F, 1);
			Core.sort(D, E, 1);

			vecinosDistCenters.put(l, 0, E.get(0, 0)[0]);
			vecinosIdCenters.put(l, 0, (int) F.get(0, 0)[0]);
		}
	}

	private void algoKnnMI(MatE MIndex, MatE vecinosIdMindex, MatE vecinosDistMindex, MatE vectorConsulta, int k) {

		MatE A = new MatE(), C = new MatE();
		int datos;
		MIndex.convertTo(MIndex, CvType.CV_64F);

		datos = MIndex.rows();

		for (int l = 0; l < vectorConsulta.rows(); l++) {
			MatE B = new MatE(Mat.zeros(datos, vectorConsulta.cols(), CvType.CV_64F));
			MatE D = new MatE(Mat.zeros(datos, 1, CvType.CV_64F));
			MatE E = new MatE(Mat.zeros(datos, 1, CvType.CV_64F));
			MatE F = new MatE(Mat.zeros(datos, vectorConsulta.cols(), CvType.CV_8U));

			A = repMat(vectorConsulta, datos, l);

			Core.subtract(A, MIndex, B);
			Core.pow(B, 2.0, C);
			double suma;
			for (int i = 0; i < C.rows(); i++) {
				suma = 0;
				for (int j = 0; j < C.cols(); j++)
					suma = suma + C.get(i, j)[0];
				D.put(i, 0, suma);
			}

			Core.sortIdx(D, F, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING);
			Core.sort(D, E, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING);
			for (int j = 0; j < k; j++) {// guardando los 5 mas parecidos
				vecinosDistMindex.put(l, j, E.get(j, 0)[0]);
				vecinosIdMindex.put(l, j, (int) F.get(j, 0)[0]);
			}
		}
	}

	private MatE repMat(MatE matNor, int num_centers, int indice) {

		double valor;
		MatE copia = new MatE(Mat.zeros(num_centers, matNor.cols(), CvType.CV_64F));
		for (int col = 0; col < matNor.cols(); col++) {
			valor = matNor.get(indice, col)[0];
			for (int row = 0; row < num_centers; row++)
				copia.put(row, col, valor);
		}
		return copia;
	}
}
