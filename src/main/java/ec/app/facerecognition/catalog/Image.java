package ec.app.facerecognition.catalog;

import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;


public class Image {
	
	public static final int NUMBER_OF_PARAMS = 9;
	
	protected Integer id;

	protected String file_name;
	
	protected MatE value;
	
	/**
	 * Points of interest
	 */
	protected LinkedList<POI> poi;
	
	public Image() {
		this.id = -1;
		this.value = new MatE();
	}
	
	public Image(Integer id, String file_name, MatE value, LinkedList<POI> poi) {
		this.id = id;
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
		return new HSI(getRGB());
	}
	
	public HSI getHSI_old() {
		RGB rgb = getRGB();
		
		HSI hsi = new HSI(rgb, true);
		rgb.release();
		
		return hsi;
	}
	
	public MatE getParameters(int radius){
		long startTime = System.currentTimeMillis();
		HSI hsi = getHSI();
		MatE H = hsi.getH();
		MatE S = hsi.getS();
		MatE I = hsi.getI();
		System.out.println("  " + (System.currentTimeMillis() - startTime) + " ms (hsi)");

		MatE ROI = new MatE();
		MatOfDouble mean = new MatOfDouble();
		MatOfDouble stdev = new MatOfDouble();
		
		MatE params = new MatE(Mat.zeros(poi.size(), NUMBER_OF_PARAMS, CvType.CV_64F));

		startTime = System.currentTimeMillis();
		for (POI poi : poi) {
			int num_poi = poi.getNum();
			
			ROI = H.getWindows(poi.getX(), poi.getY(), radius);
			Core.meanStdDev(ROI, mean, stdev);
			params.put(num_poi, 0, mean.get(0, 0)[0]);
			params.put(num_poi, 3, stdev.get(0, 0)[0]);
			params.put(num_poi, 6, ROI.homogeinity());
			
			ROI = S.getWindows(poi.getX(), poi.getY(), radius);
			Core.meanStdDev(ROI, mean, stdev);
			params.put(num_poi, 1, mean.get(0, 0)[0]);
			params.put(num_poi, 4, stdev.get(0, 0)[0]);
			params.put(num_poi, 7, ROI.homogeinity());
			
			ROI = I.getWindows(poi.getX(), poi.getY(), radius);
			Core.meanStdDev(ROI, mean, stdev);
			params.put(num_poi, 2, mean.get(0, 0)[0]);
			params.put(num_poi, 5, stdev.get(0, 0)[0]);
			params.put(num_poi, 8, ROI.homogeinity());
		}
		System.out.println("  " + (System.currentTimeMillis() - startTime) + " ms (poi)");
		
		startTime = System.currentTimeMillis();
		hsi.release();
		ROI.release();
		mean.release();
		stdev.release();
		System.out.println("  " + (System.currentTimeMillis() - startTime) + " ms (release)");
		
		return params;
	}
	
	public String getFileName(){
		return file_name;
	}

	
	public MatE getParameters_old(int radius) {
		HSI cs_hsi = getHSI_old();
		MatE H = cs_hsi.getH();
		MatE S = cs_hsi.getS();
		MatE I = cs_hsi.getI();

		MatE ROI_H = new MatE(), ROI_S = new MatE(), ROI_I = new MatE();
		MatOfDouble meanH = new MatOfDouble(), meanS = new MatOfDouble(), meanI = new MatOfDouble();
		MatOfDouble stdevH = new MatOfDouble(), stdevS = new MatOfDouble(), stdevI = new MatOfDouble();
		
		MatE params = new MatE(Mat.zeros(poi.size(), NUMBER_OF_PARAMS, CvType.CV_64F));

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
			params.put(num_poi, 4, stdevS.get(0, 0)[0]);
			params.put(num_poi, 5, stdevI.get(0, 0)[0]);

			params.put(num_poi, 6, ROI_H.homogeinity());
			params.put(num_poi, 7, ROI_S.homogeinity());
			params.put(num_poi, 8, ROI_I.homogeinity());
		}
		
		return params;
	}

	public Integer getId() {
		return id;
	}
}
