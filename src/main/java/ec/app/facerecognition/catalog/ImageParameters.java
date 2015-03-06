package ec.app.facerecognition.catalog;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ec.app.facerecognition.MatE;

public class ImageParameters {
	
	MatE params;

	public ImageParameters(int size) {
		params = new MatE(Mat.zeros(size, 9, CvType.CV_64F));
	}

	public ImageParameters(ImageParameters parameters) {
		params = parameters.params;
	}

	public void put(POI poi, int param_index, double value) {
		params.put(poi.getNum(), param_index, value);
	}
	
	@Override
	public String toString() {
		return params.toString();
	}

}
