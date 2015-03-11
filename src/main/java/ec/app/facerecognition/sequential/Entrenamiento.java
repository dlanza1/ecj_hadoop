/*
 * Esta clase genera el entrenamiento para las imagenes dentro de la base de datos
 * 
 * **/
package ec.app.facerecognition.sequential;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class Entrenamiento {
	
	Mat centers;
	Mat MIndex;
	double [] valRef;
	
	public Entrenamiento(double[]valRef, Mat centers,Mat MIndex){
		this.valRef=valRef;
		this.centers=centers;
		this.MIndex=MIndex;
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

	public void Ejemplo() throws IOException{
		
		Mat labels=new Mat(); 
        Mat matRef=Mat.zeros(1368*60, 9,CvType.CV_64F);
        Mat norMat=Mat.zeros(1368*60, 9,CvType.CV_64F);
        
        Procesa proc=new Procesa();  //genera un nuevo objeto del tipo procesa para implementar sus métodos
       proc.generaMatRef(matRef); //Genera la matriz de conocimiento para cada una de las imagenes
       proc.norMatRef(matRef, norMat,valRef,0);// normaliza la matriz de conocimiento con los valores máximos 
       proc.generaCK_Mindex(norMat, centers, labels, MIndex);//aplica el agrupamiento K-means y genera la matriz de indices de textura para cada centroide
    		
       
       PrintWriter out = new PrintWriter("entre.txt");

		for(int j=0;j<1368;j++){
//			System.out.print("MIvalor:"+MIndex.get(j,0)[0]+","+MIndex.get(j,1)[0]+","+MIndex.get(j,2)[0]+","+MIndex.get(j,3)[0]+","+MIndex.get(j,4)[0]+","+MIndex.get(j,5)[0]);
//			System.out.println(","+MIndex.get(j,6)[0]+","+MIndex.get(j,7)[0]+","+MIndex.get(j,8)[0]+","+MIndex.get(j,9)[0]);
			
			out.print("MIvalor:"+MIndex.get(j,0)[0]+","+MIndex.get(j,1)[0]+","+MIndex.get(j,2)[0]+","+MIndex.get(j,3)[0]+","+MIndex.get(j,4)[0]+","+MIndex.get(j,5)[0]);
			out.println(","+MIndex.get(j,6)[0]+","+MIndex.get(j,7)[0]+","+MIndex.get(j,8)[0]+","+MIndex.get(j,9)[0]);
		}
		
		out.close();
		
	}
}