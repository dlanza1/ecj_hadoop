/*
 * Esta clase genera el entrenamiento para las imagenes dentro de la base de datos
 * 
 * **/
package ec.app.facerecognition;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Entrenamiento {

	Mat centers;
	Mat MIndex;
	double[] valRef;

	public Entrenamiento(double[] valRef, Mat centers, Mat MIndex) {

		this.centers = centers;
		this.MIndex = MIndex;
		this.valRef = valRef;
	}

	public Mat getCenters() {
		return centers;
	}

	public Mat getMIndex() {
		return MIndex;
	}

	public double[] getValRef() {
		return valRef;
	}

	public void Ejemplo() {
		Mat labels = new Mat();
		Mat matRef = Mat.zeros(1368 * 60, 9, CvType.CV_64F);
		Mat norMat = Mat.zeros(1368 * 60, 9, CvType.CV_64F);

		Procesa proceso = new Procesa(); // genera un nuevo objeto del tipo
											// procesa para implementar sus
											// métodos
		proceso.generaMatRef(matRef); // Genera la matriz de conocimiento para
										// cada una de las imagenes
		proceso.norMatRef(matRef, norMat, valRef,0);// normaliza la matriz de
													// conocimiento con los
													// valores máximos
		proceso.generaCK_Mindex(norMat, centers, labels, MIndex);// aplica el
																	// agrupamiento
																	// K-means y
																	// genera la
																	// matriz de
																	// indices
																	// de
																	// textura
																	// para cada
																	// centroide

		// imprime el renglon 0 de la matriz de indices de color
		double[] valor0 = MIndex.get(0, 0);
		double[] valor1 = MIndex.get(0, 1);
		double[] valor2 = MIndex.get(0, 2);
		double[] valor3 = MIndex.get(0, 3);
		double[] valor4 = MIndex.get(0, 4);
		double[] valor5 = MIndex.get(0, 5);
		double[] valor6 = MIndex.get(0, 6);
		double[] valor7 = MIndex.get(0, 7);
		double[] valor8 = MIndex.get(0, 8);
		double[] valor9 = MIndex.get(0, 9);
		System.out.println(valor0[0] + "," + valor1[0] + "," + valor2[0] + ","
				+ valor3[0] + "," + valor4[0] + "," + valor5[0] + ","
				+ valor6[0] + "," + valor7[0] + "," + valor8[0] + ","
				+ valor9[0]);

		System.out.println("valRef:" + valRef[0] + "," + valRef[1] + ","
				+ valRef[2] + "," + valRef[3] + "," + valRef[4] + ","
				+ valRef[5] + "," + valRef[6] + "," + valRef[7] + ","
				+ valRef[8]);
	}
}
