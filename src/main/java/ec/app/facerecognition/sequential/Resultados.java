package ec.app.facerecognition.sequential;


import java.io.BufferedReader;

import org.opencv.core.CvType;
import org.opencv.core.Mat;



public class Resultados {

	Mat vecinosIdMIndex;
	double NUMIMAGES=1368;
	public Resultados(Mat vecinosIdMIndex){
		this.vecinosIdMIndex=vecinosIdMIndex;
	}
	
	
	public void porcentaje(){
		Mat MConfusion=Mat.zeros(228, 228,CvType.CV_32F);
		generaMConfucion(vecinosIdMIndex,MConfusion);
		System.out.println("El porcentaje de recuperacion es:"+generaPorcentaje(MConfusion));
	}
	
	/* genera el porcentaje de la matriz de confusion 
	* a partir de la diagonal principal  */
	private double generaPorcentaje(Mat MConfusion) {
		double suma=0;
		for (int i=0;i<MConfusion.rows();i++)
			suma=suma+MConfusion.get(i,i)[0];
		
		return suma/NUMIMAGES;
	}


	//genera la matriz de confusion a partir de la matriz resultante
	private void generaMConfucion(Mat vecinosIdMIndex,Mat MConfusion){
		
		int c1,c2;
		
		for (int i=0;i<vecinosIdMIndex.rows();i++){
			
			c1=clase(i);
			c2=clase((int)vecinosIdMIndex.get(i,0)[0]);
			MConfusion.put(c1,c2,(int)MConfusion.get(c1,c2 )[0]+1);
		}
	}
		
	// calcula la clase a la que pertenece cada resultado
	private int clase(int dato){
		BufferedReader nombreImagen = null;
	    BufferedReader clases = null;
	    int bandera=0;
	    int clase=0;
	     try {
	    	 	String lineaNombre=null;
	    	 	String lineaClase=null;
	    	 	String subName=null;
	    	 	Ficheros archivo=new Ficheros();
	    	 	// Lectura del fichero
	    	 	String rutaNombres="src/main/java/ec/app/facerecognition/res/nombres.csv";//ruta al archivo de nombres
	    	 	String rutaClases="src/main/java/ec/app/facerecognition/res/clases.txt"; //ruta al archivo de coordenadas (x,y) de cada PI
	    	 	nombreImagen=archivo.abrir(rutaNombres);
	    	 	clases=archivo.abrir(rutaClases);       	
	    	 
	    	 	nombreImagen = archivo.abrir(rutaNombres);
	    	 	clases = archivo.abrir(rutaClases);

	    	 	for (int j = 0; j <=dato ; j++) 
	    	 		lineaNombre = nombreImagen.readLine();// lee la imagen que se procesara
	    	 		
	    	 	subName=lineaNombre.substring(0, 4);//sub cadena
	    	 	
	    	 	while(clase<228){  //mientras las clases no sean iguales y sean menor de 228
	    	 		lineaClase=clases.readLine();// se lee la nueva clase
	    	 		if(subName.equals(lineaClase))// comparacion de cadenas
	    	 		{	bandera=clase; // si las cadenas son iguales
	    	 			break; //sale del while
	    	 		}
	    	 		clase=clase+1;
	    	 	}
	    	 				 
	     } catch (Exception e) {
				e.printStackTrace();
	     	}
		return bandera;
	}
	
}
