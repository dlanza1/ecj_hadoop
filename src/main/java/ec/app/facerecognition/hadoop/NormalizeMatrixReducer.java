package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class NormalizeMatrixReducer extends
		Reducer<NullWritable, MatEWritable, MatEWritable, MatEWritable> {
	
	@Override
	protected void reduce(
			NullWritable key,
			Iterable<MatEWritable> values,
			Context context)
			throws IOException, InterruptedException {

		MatE matRef = new MatE();

		//Join all params
		List<Mat> mats = new LinkedList<Mat>();
		for (MatEWritable matEWritable : values) {
			mats.add(matEWritable);
		}
		Core.vconcat(mats, matRef);
		
		//Get maximun per column
		MatE max_per_col = new MatE(Mat.zeros(1, 9,CvType.CV_64F));
		Mat ROI = new Mat();
		for (int i = 0; i < matRef.cols(); i++) {
			ROI = matRef.submat(0, matRef.rows(), i, i + 1);
			MinMaxLocResult mmr = Core.minMaxLoc(ROI);
			max_per_col.put(0, i, mmr.maxVal);
		}
		
		//Normalize per column
		MatE matNor = new MatE(Mat.zeros(matRef.rows(), matRef.rows(), matRef.type()));
		for (int c = 0; c < matRef.cols(); c++) {
			double max_col = max_per_col.get(0, c)[0];
			for (int r = 0; r < matRef.rows(); r++) {
				matNor.put(r, c, matRef.get(r, c)[0] / max_col);
			}
		}
		
		context.write(new MatEWritable(max_per_col), new MatEWritable(matNor));
	}
}
