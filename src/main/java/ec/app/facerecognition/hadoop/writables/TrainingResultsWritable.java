package ec.app.facerecognition.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.app.facerecognition.catalog.MatE;

public class TrainingResultsWritable implements WritableComparable<TrainingResultsWritable> {

	MatE max_per_col;
	MatE centers;
	MatE textureIndexMatriz;
	
	public TrainingResultsWritable(MatE max_per_col, MatE centers, MatE textureIndexMatriz) {
		this.max_per_col = max_per_col;
		this.centers = centers;
		this.textureIndexMatriz = textureIndexMatriz;
	}

	public TrainingResultsWritable() {
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		max_per_col = new MatE();
		max_per_col.read(in);
		centers = new MatE();
		centers.read(in);
		textureIndexMatriz = new MatE();
		textureIndexMatriz.read(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		max_per_col.write(out);
		max_per_col.release();
		centers.write(out);
		centers.release();
		textureIndexMatriz.write(out);
		textureIndexMatriz.release();
	}

	@Override
	public int compareTo(TrainingResultsWritable o) {
		return max_per_col.equals(o.max_per_col)
				&& centers.equals(o.centers)
				&& textureIndexMatriz.equals(o.textureIndexMatriz) ? 0 : -1;
	}

	@Override
	public String toString() {
		return "TrainingResultsWritable [max_per_col=" + max_per_col
				+ ", centers=" + centers + ", textureIndexMatriz="
				+ textureIndexMatriz + "]";
	}

	public MatE getMaxPerCol() {
		return max_per_col;
	}

	public MatE getCenters() {
		return centers;
	}

	public MatE getTextureIndexMatriz() {
		return textureIndexMatriz;
	}

}
