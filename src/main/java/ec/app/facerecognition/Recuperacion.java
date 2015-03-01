package ec.app.facerecognition;

import java.io.BufferedReader;
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
		Mat vectorConsulta=Mat.zeros(1368, 10,CvType.CV_64F);
		Mat vecinosIdCenters=Mat.zeros(60,1,CvType.CV_32F);
		Mat vecinosDistCenters=Mat.zeros(60, 1,CvType.CV_64F);
		
		Mat vecinosIdMindex=Mat.zeros(1368,5,CvType.CV_32F); 
		Mat vecinosDistMIndex=Mat.zeros(1368,5,CvType.CV_64F);
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

		    	for (int j = 0; j < 10; j++) {
		    		long image_time = System.currentTimeMillis();
			
		    		lineaNombre = nombreImagen.readLine();// lee la imagen que se procesara
		    		//System.out.println("\nimagen: " + lineaNombre);

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
		    			
		    	//System.out.println(System.currentTimeMillis() - time + " ms (rgb2hsi) ");
							
		    	time = System.currentTimeMillis();
			
		    	proc.llenaMatRef(0, matRef, puntos, h, s, i);// Llena la matriz de referencia
		    	
		    	proc.norMatRef(matRef, matNor, valRef,1);//normaliza la matriz de consulta
		    	
		    	//System.out.println(System.currentTimeMillis() - time + " ms (llenaMatRef) ");
		    	time = System.currentTimeMillis();
		    	
		    	algoKNN(centers, vecinosIdCenters, vecinosDistCenters, matNor, 1);
		    	
		    	//llenando el vector consulta
		    	for(int l=0;l<60;l++){
		    		//vectorConsulta.at<uchar>(j,(int)vecinosIdsCenters.at<uchar>(i,0))=vectorConsulta.at<uchar>(j,(int)vecinosIdsCenters.at<uchar>(i,0))+1;
		    		vectorConsulta.put(j,(int)vecinosIdCenters.get(l,0)[0],vectorConsulta.get(j,(int)vecinosIdCenters.get(l,0)[0])[0]+1);	
		    	}
		    		System.out.print("valor:"+vectorConsulta.get(j,0)[0]+","+vectorConsulta.get(j,1)[0]+","+vectorConsulta.get(j,2)[0]+","+vectorConsulta.get(j,3)[0]+","+vectorConsulta.get(j,4)[0]+","+vectorConsulta.get(j,5)[0]);
		    		System.out.println(","+vectorConsulta.get(j,6)[0]+","+vectorConsulta.get(j,7)[0]+","+vectorConsulta.get(j,8)[0]+","+vectorConsulta.get(j,9)[0]);
		    	//System.out.println(System.currentTimeMillis() - image_time + " ms (total imagen) ");
		    	image_time = System.currentTimeMillis();
		    }
		    	algoKnnMI(MIndex,vecinosIdMindex,vecinosDistMIndex,vectorConsulta,5);
		    	for (int k=0;k<10;k++)
		    	System.out.println("Resultado:"+vecinosIdMindex.get(k,0)[0]+","+vecinosIdMindex.get(k,1)[0]+","+vecinosIdMindex.get(k,2)[0]+","+vecinosIdMindex.get(k,3)[0]+","+vecinosIdMindex.get(k,4)[0]);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		 }
	 }
	 
	 private void algoKNN(Mat centers, Mat vecinosIdCenters,Mat vecinosDistCenters,Mat matNor,int k){
		 
		 Mat A=new Mat(),C=new Mat();
		 int datos;
		 centers.convertTo(centers, CvType.CV_64F);
		 
		 datos=centers.rows();

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
	 
private void algoKnnMI(Mat MIndex, Mat vecinosIdMindex,Mat vecinosDistMindex,Mat vectorConsulta,int k){
		 
		 Mat A=new Mat(),C=new Mat();
		 int datos;
		 MIndex.convertTo(MIndex, CvType.CV_64F);
		 
		 datos=MIndex.rows();

		 for (int l=0;l<vectorConsulta.rows();l++){
			 Mat B=Mat.zeros(datos,vectorConsulta.cols(),CvType.CV_64F);
			 Mat D=Mat.zeros(datos,1,CvType.CV_64F);
			 Mat E=Mat.zeros(datos,1,CvType.CV_64F);
			 Mat F=Mat.zeros(datos,vectorConsulta.cols(),CvType.CV_8U);
			 
			 A=repMat(vectorConsulta,datos,l);
			 
			 Core.subtract(A, MIndex, B);
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
			 for(int j=0;j<k;j++){//guardando los 5 mas parecidos
				 vecinosDistMindex.put(l,j, E.get(j, 0)[0]);
				 vecinosIdMindex.put(l,j,(int) F.get(j, 0)[0]);
			 }
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