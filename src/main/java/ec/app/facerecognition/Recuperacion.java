package ec.app.facerecognition;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

public class Recuperacion {
	
	Mat centers;
	double[] valRef;
	Mat MIndex;
	
	 public Recuperacion(double[] valRef, Mat centers, Mat MIndex) {
	
		 this.centers = centers;
		 this.valRef=valRef;
		 this.MIndex=MIndex;
		 
	}
	
	 public void Consulta(){
		 
		BufferedReader nombreImagen = null;
		BufferedReader puntosImagen = null;
		Mat image=new Mat();
		Mat matRef=Mat.zeros(60, 9,CvType.CV_64F);
		Mat matNor=Mat.zeros(matRef.size(), CvType.CV_64F);
		Mat vecinosIdCenters=new Mat(),vecinosDistCenters=new Mat();
		int [] puntos= new int[120];// arreglo que guarda los 60 Pi
		try {
				String lineaNombre;
		    	String lineaPuntos;
		    	String palabraPuntos;
		    	Ficheros archivo=new Ficheros();
		    	// Lectura del fichero
		    	String rutaNombres="/home/cesar/Escritorio/nombres.csv";//ruta al archivo de nombres
		    	String rutaPuntos="/home/cesar/Escritorio/datos.csv"; //ruta al archivo de coordenadas (x,y) de cada PI
		    	nombreImagen=archivo.abrir(rutaNombres);
		    	puntosImagen=archivo.abrir(rutaPuntos);       	
		    	nombreImagen = archivo.abrir(rutaNombres);
		    	puntosImagen = archivo.abrir(rutaPuntos);

		    	for (int j = 0; j < 1; j++) {
		    		long image_time = System.currentTimeMillis();
			
		    		lineaNombre = nombreImagen.readLine();// lee la imagen que se procesara
		    		System.out.println("\nimagen: " + lineaNombre);

		    		image = Highgui.imread(Aplicacion.class.getResource("files/images/" + lineaNombre).getPath());

		    		lineaPuntos = puntosImagen.readLine();// lee la linea de puntos de la imagen

		    		int numTokens = 0; // toquen para guardar los 60 puntos
		    		StringTokenizer st = new StringTokenizer(lineaPuntos); // bucle
			
		    		while (st.hasMoreTokens()) {
		    			if (numTokens >= 32) {
		    				palabraPuntos = st.nextToken();
		    				puntos[numTokens - 32] = (int) Float.parseFloat(palabraPuntos);// se almacenan las
														// coordenadas (x,y)
		    			 } else
		    				palabraPuntos = st.nextToken();
		    			 numTokens++;
		    		}

		    	image.convertTo(image, CvType.CV_64FC(3), 1.0 / 255.0);// convierte
		    	
		    	Procesa proc= new Procesa();// objeto para procesar la imagen de consult
		    	long time = System.currentTimeMillis();
			
		    	Mat h = new Mat();
		    	Mat s = new Mat();
		    	Mat i = new Mat();
		    	proc.rgb2hsi_improved(image, h, s, i);
		    			
		    	System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi) ");
							
		    	time = System.currentTimeMillis();
			
		    	proc.llenaMatRef(0, matRef, puntos, h, s, i);// Llena la matriz de referencia
		    	
		    	proc.norMatRef(matRef, matNor, valRef,1);//normaliza la matriz de consulta
		    	
		    	System.out.println(System.currentTimeMillis() - time + " ms (llenaMatRef) ");
		    	time = System.currentTimeMillis();
		    	
		    	algoKNN(centers, vecinosIdCenters, vecinosDistCenters, matNor, 1);
		    	
		    	
		    	System.out.println(System.currentTimeMillis() - image_time + " ms (total imagen) ");
		    	image_time = System.currentTimeMillis();
		    }
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		 }
	 }
	 
	 private void algoKNN(Mat centers, Mat vecinosIdCenters,Mat vecinosDistCenters,Mat matNor,int k){
		 Mat A=new Mat(), arreglo=new Mat(),C=new Mat();
		 int datos, consulta;
		 centers.convertTo(centers, CvType.CV_64F);
		 datos=centers.rows();
		 consulta=matNor.rows();
		 for (int l=0;l<matNor.rows();l++){
			 Mat B=Mat.zeros(datos,matNor.cols(),CvType.CV_64F);
			 Mat D=Mat.zeros(datos,1,CvType.CV_64F);
			 Mat E=Mat.zeros(datos,1,CvType.CV_64F);
			 Mat F=Mat.zeros(datos,matNor.cols(),CvType.CV_8U);
			 
			 A=repMat(matNor,datos,l);
			 
			 Core.subtract(A, centers, B);
			 Core.pow(B, 2.0, C);
			 double suma;
			 for (int i=0;i<C.rows();i++ ){
				 suma=0;
				 for (int j=0;j<C.cols();j++)
					 suma=suma+C.get(i, j)[0];
				 D.put(i,0,suma);
			 }
		 
			 Core.sortIdx(D, F,1 );
			 Core.sort(D, E, 1);
			 vecinosDistCenters.put(l,0, E.get(0, 0)[0]);
			 vecinosIdCenters.put(l,0,(int) F.get(0, 0)[0]);
		 }
	 }

	private Mat repMat(Mat matNor, int datos, int indice) {
		
		double valor;
		Mat copia=Mat.zeros(datos, matNor.cols(),CvType.CV_64F);
		for (int i=0;i<matNor.cols();i++){
			valor=matNor.get(indice, i)[0];
			for(int j=0;j<datos;j++)
				copia.put(j, i, valor);
		}
		return copia;
	}
	
}
