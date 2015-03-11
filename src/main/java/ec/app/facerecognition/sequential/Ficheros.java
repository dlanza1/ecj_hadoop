package ec.app.facerecognition.sequential;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Ficheros {

	public BufferedReader abrir(String ruta) throws FileNotFoundException {
		File archivo = null;
		FileReader fr = null;
		BufferedReader br = null;

		// Apertura del fichero y creacion de BufferedReader para poder
		// hacer una lectura comoda (disponer del metodo readLine()).
		archivo = new File(ruta);
		fr = new FileReader(archivo);
		br = new BufferedReader(fr);

		return br;
	}
}
