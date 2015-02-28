package ec.app.facerecognition;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Aplicacion {

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		double[] valRef = new double[9];

		Mat centers = new Mat(), MIndex = new Mat();

		MIndex = Mat.zeros(1368, 10, CvType.CV_32F);

		// llamando al Entrenamiento
		Entrenamiento entre = new Entrenamiento(valRef, centers, MIndex);
		entre.Ejemplo();
		centers = entre.getCenters();
		valRef = entre.getValRef();
		MIndex = entre.getMIndex();
		System.out.println("ValRef:" + valRef[0] + "," + valRef[1] + ","
				+ valRef[2] + "," + valRef[3] + "," + valRef[4] + ","
				+ valRef[5] + "," + valRef[6] + "," + valRef[7] + ","
				+ valRef[8]);

		// LLamando a la recuperación
		// Recuperacion Recupera=new Recuperacion(valRef,centers,MIndex);

	}
}
