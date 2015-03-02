package ec.app.facerecognition.catalog;

import org.opencv.core.Core;
import org.opencv.core.MatOfDouble;

import ec.app.facerecognition.MatE;


public class Image {
	
	protected MatE value;
	private int[] poi_y;
	private int[] poi_x;
	
	public Image() {
		this.value = new MatE();
	}
	
	public Image(MatE value) {
		this.value = value;
	}
	
	public MatE getValue(){
		return value;
	}
	
	public RGB getRGB(){
		return new RGB(value);
	}
	
	public HSI getHSI() {
		return getRGB().toHSI();
	}
	
	public ImageParameters getParameters(int radius){
		HSI cs_hsi = getHSI();
		MatE H = cs_hsi.getH();
		MatE S = cs_hsi.getS();
		MatE I = cs_hsi.getI();

		MatE ROI_H = new MatE(), ROI_S = new MatE(), ROI_I = new MatE();
		MatOfDouble meanH = new MatOfDouble(), meanS = new MatOfDouble(), meanI = new MatOfDouble();
		MatOfDouble stdevH = new MatOfDouble(), stdevS = new MatOfDouble(), stdevI = new MatOfDouble();
		
		ImageParameters params = new ImageParameters();

		//For thought every point of interest
		for (int poi_index = 0; poi_index < 60; poi_index++) {
			POI poi = getPOI(poi_index);

			//Extract region of interest
			ROI_H = H.getWindows(poi.getX(), poi.getY(), radius);
			ROI_S = S.getWindows(poi.getX(), poi.getY(), radius);
			ROI_I = I.getWindows(poi.getX(), poi.getY(), radius);

			//Calculate averages and standar deviation
			Core.meanStdDev(ROI_H, meanH, stdevH);
			Core.meanStdDev(ROI_S, meanS, stdevS);
			Core.meanStdDev(ROI_I, meanI, stdevI);

			//Set params
			params.put(poi_index, 0, meanH.get(0, 0)[0]);
			params.put(poi_index, 1, meanS.get(0, 0)[0]);
			params.put(poi_index, 2, meanI.get(0, 0)[0]);

			params.put(poi_index, 3, stdevH.get(0, 0)[0]);
			params.put(poi_index, 4, stdevH.get(0, 0)[0]);
			params.put(poi_index, 5, stdevH.get(0, 0)[0]);

			params.put(poi_index, 6, ROI_H.homogeinity());
			params.put(poi_index, 7, ROI_S.homogeinity());
			params.put(poi_index, 8, ROI_I.homogeinity());
		}
		
		return params;
	}

	private POI getPOI(int poi_index) {
		return new POI(poi_x[poi_index], poi_y[poi_index]);
	}

}
