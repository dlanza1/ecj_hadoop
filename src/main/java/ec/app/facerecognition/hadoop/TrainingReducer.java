package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.TermCriteria;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class TrainingReducer extends
		Reducer<NullWritable, MatEWithIDWritable, NullWritable, TrainingResultsWritable> {
	
	private int num_centers;
	
	@Override
	protected void setup(
			Reducer<NullWritable, MatEWithIDWritable, NullWritable, TrainingResultsWritable>.Context context)
			throws IOException, InterruptedException {
		num_centers = context.getConfiguration().getInt(EvaluateIndividual.NUM_CENTERS_PARAM, 10);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void reduce(
			NullWritable key,
			Iterable<MatEWithIDWritable> values,
			Context context)
			throws IOException, InterruptedException {
		
		int number_of_images = 0;
		
		MatE matRef = new MatE();

		//Join all params
		List<MatEWithIDWritable> mats = new LinkedList<MatEWithIDWritable>();
		for (MatEWithIDWritable mat : values) {
			MatEWithIDWritable tmp = new MatEWithIDWritable();
			mat.copyTo(tmp);
			mat.release();
			mats.add(tmp);
			
			number_of_images++;
		}
		Collections.sort(mats);
		Core.vconcat((List<Mat>)(List<?>) mats, matRef);
		
		System.out.println("Reference matrix: ");
		System.out.println(matRef);

		int number_of_poi = matRef.rows() / number_of_images;
		
		MatE max_per_column = matRef.getMaxPerColumn();
		matRef = matRef.normalize(max_per_column);
		
		System.out.println("Max per column: ");
		System.out.println(max_per_column);
		
		//Compute KMeans
		MatE centers = new MatE();
		matRef.convertTo(matRef, CvType.CV_32F);
		TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 10000, 0.0001);
		MatE labels = new MatE();
		Core.kmeans(matRef, num_centers , labels, criteria, 1, Core.KMEANS_RANDOM_CENTERS, centers);
		
		System.out.println("Centers:");
		System.out.println(centers);
		
		System.out.println("Labels:");
		System.out.println(labels);
		
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
		
		System.out.println("Texture index matrix:");
		System.out.println(textureIndexMatriz);
		
		centers.convertTo(centers, CvType.CV_64F);
		textureIndexMatriz.convertTo(textureIndexMatriz, CvType.CV_64F);
		
		context.write(NullWritable.get(), new TrainingResultsWritable(max_per_column, centers, textureIndexMatriz));
	}

}
