package ec.app.facerecognition.catalog;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ec.app.facerecognition.MatE;

public class ImageParameters {
	
	MatE params;

	public ImageParameters() {
		params = new MatE(Mat.zeros(60, 9, CvType.CV_64F));
	}

	public ImageParameters(ImageParameters parameters) {
		params = parameters.params;
	}

	public void put(POI poi, int param_index, double value) {
		params.put(poi.getNum(), param_index, value);
	}

}
