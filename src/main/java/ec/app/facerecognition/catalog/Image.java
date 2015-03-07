package ec.app.facerecognition.catalog;

import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;


public class Image {
	
	protected String file_name;
	
	protected MatE value;
	
	/**
	 * Points of interest
	 */
	protected LinkedList<POI> poi;
	
	public Image() {
		this.value = new MatE();
	}
	
	public Image(String file_name, MatE value, LinkedList<POI> poi) {
		this.file_name = file_name;
		this.value = value;
		this.poi = poi;
	}

	public MatE getValue(){
		return value;
	}
	
	public LinkedList<POI> getPOI(){
		return poi;
	}
	
	public RGB getRGB(){
		return new RGB(value);
	}
	
	public HSI getHSI() {
		return getRGB().toHSI();
	}
	
	public MatE getParameters(int radius){
		HSI cs_hsi = getHSI();
		MatE H = cs_hsi.getH();
		MatE S = cs_hsi.getS();
		MatE I = cs_hsi.getI();

		MatE ROI_H = new MatE(), ROI_S = new MatE(), ROI_I = new MatE();
		MatOfDouble meanH = new MatOfDouble(), meanS = new MatOfDouble(), meanI = new MatOfDouble();
		MatOfDouble stdevH = new MatOfDouble(), stdevS = new MatOfDouble(), stdevI = new MatOfDouble();
		
		MatE params = new MatE(Mat.zeros(poi.size(), 9, CvType.CV_64F));

		for (POI poi : poi) {
			int num_poi = poi.getNum();
			
			//Extract region of interest
			ROI_H = H.getWindows(poi.getX(), poi.getY(), radius);
			ROI_S = S.getWindows(poi.getX(), poi.getY(), radius);
			ROI_I = I.getWindows(poi.getX(), poi.getY(), radius);

			//Calculate averages and standar deviation
			Core.meanStdDev(ROI_H, meanH, stdevH);
			Core.meanStdDev(ROI_S, meanS, stdevS);
			Core.meanStdDev(ROI_I, meanI, stdevI);

			//Set params
			params.put(num_poi, 0, meanH.get(0, 0)[0]);
			params.put(num_poi, 1, meanS.get(0, 0)[0]);
			params.put(num_poi, 2, meanI.get(0, 0)[0]);

			params.put(num_poi, 3, stdevH.get(0, 0)[0]);
			params.put(num_poi, 4, stdevH.get(0, 0)[0]);
			params.put(num_poi, 5, stdevH.get(0, 0)[0]);

			params.put(num_poi, 6, ROI_H.homogeinity());
			params.put(num_poi, 7, ROI_S.homogeinity());
			params.put(num_poi, 8, ROI_I.homogeinity());
		}
		
		return params;
	}
	
	public String getFileName(){
		return file_name;
	}

}
