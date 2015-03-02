package ec.app.facerecognition.catalog;

/**
 * Represents a point of interest
 */
public class POI {
	
	int x, y;
	
	int num;

	public POI(int num, int x, int y) {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	public int getNum() {
		return num;
	}

}
