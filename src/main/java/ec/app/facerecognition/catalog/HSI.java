package ec.app.facerecognition.catalog;


public class HSI {
	
	/**
	 * Hue
	 */
	MatE h;
	
	/**
	 * Saturation
	 */
	MatE s;
	
	/**
	 * Intensity
	 */
	MatE i;

	public HSI(MatE h, MatE s, MatE i) {
		this.h = h;
		this.s = s;
		this.i = i;
	}

	public MatE getH() {
		return h;
	}
	
	public MatE getS() {
		return s;
	}
	
	public MatE getI() {
		return i;
	}

}
