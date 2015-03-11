package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.MatEWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class TrainingReducer extends
		Reducer<NullWritable, MatEWritable, NullWritable, TrainingResultsWritable> {
	
	private int num_centers = 10;

	@Override
	protected void reduce(
			NullWritable key,
			Iterable<MatEWritable> values,
			Context context)
			throws IOException, InterruptedException {
		
		int number_of_images = 0;
		
		MatE matRef = new MatE();

		//Join all params
		List<Mat> mats = new LinkedList<Mat>();
		for (MatEWritable matEWritable : values) {
			MatE tmp = new MatE();
			matEWritable.copyTo(tmp);
			matEWritable.release();
			mats.add(tmp);
			
			number_of_images++;
		}
		Core.vconcat(mats, matRef);
		
		int number_of_poi = matRef.rows() / number_of_images;
		
		MatE max_per_column = matRef.getMaxPerColumn();
		matRef = matRef.normalize(max_per_column);
		
		//Compute KMeans
		MatE centers = new MatE();
		matRef.convertTo(matRef, CvType.CV_32F);
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10000, 0.0001);
		MatE labels = new MatE();
		Core.kmeans(matRef, num_centers , labels, criteria, 1, Core.KMEANS_RANDOM_CENTERS, centers);
		
		//Generates texture index matrix
		MatE textureIndexMatriz = new MatE(Mat.zeros(number_of_images, num_centers, CvType.CV_32F));
		int pos;
		for (int i = 0; i < number_of_images; i++) {
			pos = number_of_poi * i;
			for (int j = pos; j < pos + number_of_poi; j++) {
				double[] valor = textureIndexMatriz.get(i, (int) labels.get(j, 0)[0]);
				valor[0] = valor[0] + 1;
				textureIndexMatriz.put(i, (int)labels.get(j,0)[0], valor);
			}
		}
		
		labels.release();
		
		centers.convertTo(centers, CvType.CV_64F);
		textureIndexMatriz.convertTo(textureIndexMatriz, CvType.CV_64F);
		
		context.write(NullWritable.get(), new TrainingResultsWritable(max_per_column, centers, textureIndexMatriz));
	}

}
