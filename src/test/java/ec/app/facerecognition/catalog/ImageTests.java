package ec.app.facerecognition.catalog;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ImageTests {
	
	@Test
	public void lessMemoryOK(){
		
		LinkedList<Mat> channels = new LinkedList<Mat>();
		
		MatE c1 = new MatE(400, 600, CvType.CV_64F);
		Core.randu(c1, 0, 255);
		channels.add(c1);
		MatE c2 = new MatE(400, 600, CvType.CV_64F);
		Core.randu(c2, 0d, 1d);
		channels.add(c2);
		MatE c3 = new MatE(400, 600, CvType.CV_64F);
		Core.randu(c3, 0d, 1d);
		channels.add(c3);
		
		MatE image = new MatE(400, 600, CvType.CV_64F);
		Core.merge(channels, image);
		
		LinkedList<POI> poi = new LinkedList<POI>();
		for (int i = 0; i < 20; i++)
			poi.add(new POI(i, (int) (Math.random() * 500d) + 50 , (int) (Math.random() * 300d) + 50));
		
		Image i = new Image("", image, poi);
		
		assertEquals(i.getParameters(5), i.getParameters_old(5));
	}

}
