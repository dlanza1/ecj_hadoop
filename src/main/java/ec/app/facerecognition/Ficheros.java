package ec.app.facerecognition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Ficheros {

	public BufferedReader abrir(String ruta) {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;

		try {
			// Apertura del fichero y creacion de BufferedReader para poder
			// hacer una lectura comoda (disponer del metodo readLine()).
			archivo = new File(ruta);
			System.out.println(ruta);
			fr = new FileReader(archivo);
			br = new BufferedReader(fr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return br;
	}
}
