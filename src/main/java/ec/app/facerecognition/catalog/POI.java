package ec.app.facerecognition.catalog;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Represents a point of interest
 */
public class POI implements Comparable<POI>{
	
	int num, x, y;

	public POI(int num, int x, int y) {
		this.x = x;
		this.y = y;
		this.num = num;
	}
	
	public POI() {
		num = x = y = -1;
	}

	public int getNum() {
		return num;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "POI [num=" + num + ", x=" + x + ", y=" + y + "]";
	}

	@Override
	public int compareTo(POI other) {
		return Integer.compare(x, y);
	}

	public void write(DataOutput out) throws IOException {
		out.writeInt(num);
		out.writeInt(x);
		out.writeInt(y);
	}

	public void read(DataInput in) throws IOException {
		num = in.readInt();
		x = in.readInt();
		y = in.readInt();
	}
	
	
}
