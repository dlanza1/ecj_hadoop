package ec.app.facerecognition.sequential;

import java.io.IOException;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class AplicacionWithThreads extends Thread {

	public static void main(String[] args) throws IOException {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		int numberOfThreads = 4;
		AplicacionWithThreads[] aplicacionWithThreads = new AplicacionWithThreads[numberOfThreads]; 
		
		for (int i = 0; i < aplicacionWithThreads.length; i++) {
			aplicacionWithThreads[i] = new AplicacionWithThreads();
		}
		
		for (int i = 0; i < aplicacionWithThreads.length; i++) {
			aplicacionWithThreads[i].start();
		}

		for (int i = 0; i < aplicacionWithThreads.length; i++) {
			try {
				aplicacionWithThreads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@Override
	public void run() {

		long start = System.currentTimeMillis();
		long sstart = System.currentTimeMillis();

		double[] valRef = new double[9];
		Mat vecinosIdMIndex = Mat.zeros(1368, 5, CvType.CV_32F);
		Mat vecinosDistMIndex = Mat.zeros(1368, 5, CvType.CV_64F);

		Mat centers = new Mat(), MIndex = new Mat();

		MIndex = Mat.zeros(1368, 10, CvType.CV_32F);

		// llamando al Entrenamiento
		Entrenamiento entre = new Entrenamiento(valRef, centers, MIndex);
		try {
			entre.Ejemplo();
		} catch (IOException e) {
			e.printStackTrace();
		}

		centers = entre.getCenters();
		valRef = entre.getValRef();
		MIndex = entre.getMIndex();
		System.out.println("ValRef:" + valRef[0] + "," + valRef[1] + ","
				+ valRef[2] + "," + valRef[3] + "," + valRef[4] + ","
				+ valRef[5] + "," + valRef[6] + "," + valRef[7] + ","
				+ valRef[8]);

		System.out.println("Train time " + (System.currentTimeMillis() - start)
				/ 1000 + " seconds");
		start = System.currentTimeMillis();
		// System.out.println("ValRef:"+valRef[0]+","+valRef[1]+","+valRef[2]+","+valRef[3]+","+valRef[4]+","+valRef[5]+","+valRef[6]+","+valRef[7]+","+valRef[8]);

		// LLamando a la recuperación
		Recuperacion Recupera = new Recuperacion(valRef, centers, MIndex,
				vecinosDistMIndex, vecinosIdMIndex);
		try {
			Recupera.Consulta();
		} catch (IOException e) {
			e.printStackTrace();
		}
		vecinosDistMIndex = Recupera.getVecinosDistMIndex();
		vecinosIdMIndex = Recupera.getVecinosIdMIndex();
		System.out.println(vecinosIdMIndex.dump());

		Resultados Resul = new Resultados(vecinosIdMIndex);
		Resul.porcentaje();

		System.out.println("Query time " + (System.currentTimeMillis() - start) / 1000 + " seconds");
		System.out.println("Total time " + (System.currentTimeMillis() - sstart) / 1000 + " seconds");
	}
}
