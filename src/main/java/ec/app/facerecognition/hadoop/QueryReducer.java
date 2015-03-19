package ec.app.facerecognition.hadoop;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;

public class QueryReducer extends Reducer<MatEWritable, MatEWithIDWritable, NullWritable, FloatWritable> {

	/**
	 * Association of ID image, ID class
	 */
	HashMap<Integer, Integer> img_class;
	
	private int num_nearest;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		
		num_nearest = context.getConfiguration().getInt(EvaluateIndividual.NUM_NEAREST_PARAM, 6);
		
		img_class = getImagesClass(conf);
		
		System.out.println("Association img-class: " + img_class);
	}
	
	private HashMap<Integer, Integer> getImagesClass(Configuration conf) throws IOException {

		//Get classes
		HashMap<String, Integer> classes = readClasses(conf);
		
		System.out.println("Classes: " + classes);
		
		//Associate each image with the class
		HashMap<Integer, Integer> img_class = new HashMap<Integer, Integer>();

		String file = conf.get(ImageRecordReader.IMAGES_FILE_PARAM);
		FileSystem fs = FileSystem.get(conf);
		BufferedReader br_names = new BufferedReader(new InputStreamReader(fs.open(new Path("hdfs:" + file))));
		
		String file_name;
		Integer index = 0;
		while ((file_name = br_names.readLine()) != null){
			String cl = file_name.substring(0, 4);
			
			img_class.put(index, classes.get(cl));
			
			index++;
		}
		
		return img_class;
	}

	private HashMap<String, Integer> readClasses(Configuration conf) throws IOException {
		HashMap<String, Integer> classes = new HashMap<String, Integer>();
		
		String file = conf.get(EvaluateIndividual.CLASSES_FILE_PARAM);
		FileSystem fs = FileSystem.get(conf);
		BufferedReader br_classes = new BufferedReader(new InputStreamReader(fs.open(new Path("hdfs:" + file))));
		
		String class_line;
		Integer index = 0;
		while ((class_line = br_classes.readLine()) != null){
			classes.put(class_line, index);
			index++;
		}
		
		br_classes.close();
		
		return classes;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void reduce(MatEWritable textureIndexMatrix, Iterable<MatEWithIDWritable> queryVectors, Context context)
			throws IOException, InterruptedException {
		
		MatE query_mat = new MatE();

		//Join all vectors
		List<MatEWithIDWritable> vectors = new LinkedList<MatEWithIDWritable>();
		for (MatEWithIDWritable queryVector : queryVectors) {
			MatEWithIDWritable tmp = new MatEWithIDWritable();
			queryVector.copyTo(tmp);
			queryVector.release();
			
			vectors.add(tmp);
		}
		Collections.sort(vectors);
		Core.vconcat((List<Mat>)(List<?>) vectors, query_mat);
		
		System.out.println("Join all vectors: ");
		System.out.println(query_mat.dump());

		MatE nearestIds = knn(textureIndexMatrix, query_mat, num_nearest);
		
		System.out.println("Nearest ids: ");
		System.out.println(nearestIds.dump());
		
		MatE confusionMatrix = generateConfusionMatrix(nearestIds, num_nearest);
		
		System.out.println("Confusion matrix:: ");
		System.out.println(confusionMatrix.dump());
		
		//Compute percentage
		float suma=0;
		for (int i=0;i<confusionMatrix.rows();i++)
			suma = suma + (float)confusionMatrix.get(i,i)[0];
		
		float percentage = suma / (float)vectors.size() / (float)(num_nearest - 1);
		
		context.write(NullWritable.get(), new FloatWritable(percentage));
	}
	
	private MatE generateConfusionMatrix(Mat nearestIds, int num_nearest){
		MatE confusionMatrix = MatE.zeros(228, 228,CvType.CV_32F);
		
		Integer c1,c2;
		
		for (int img = 0;img < nearestIds.rows();img++){
			c1 = img_class.get(img);
			
			for (int j = 1; j < num_nearest; j++) {
				c2 = img_class.get((int) nearestIds.get(img, j)[0]);
				
				confusionMatrix.put(c1, c2, (int) confusionMatrix.get(c1, c2)[0] + 1);
			}
		}
		
		return confusionMatrix;
	}
	
	private MatE knn(MatE textureIndexMatrix, MatE query_mat, int num_nearest) {
		MatE nearestIds = MatE.zeros(query_mat.rows(), num_nearest, CvType.CV_32F);
		
		MatE A = new MatE(), C = new MatE();
		int datos;
		textureIndexMatrix.convertTo(textureIndexMatrix, CvType.CV_64F);

		datos = textureIndexMatrix.rows();

		for (int l = 0; l < query_mat.rows(); l++) {
			MatE B = new MatE(Mat.zeros(datos, query_mat.cols(), CvType.CV_64F));
			MatE D = new MatE(Mat.zeros(datos, 1, CvType.CV_64F));
			MatE E = new MatE(Mat.zeros(datos, 1, CvType.CV_64F));
			MatE F = new MatE(Mat.zeros(datos, query_mat.cols(), CvType.CV_8U));

			A = query_mat.rep(datos, l);

			Core.subtract(A, textureIndexMatrix, B);
			Core.pow(B, 2.0, C);
			double suma;
			for (int i = 0; i < C.rows(); i++) {
				suma = 0;
				for (int j = 0; j < C.cols(); j++)
					suma = suma + C.get(i, j)[0];
				D.put(i, 0, suma);
			}

			Core.sortIdx(D, F, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING);
			Core.sort(D, E, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING);
			for (int j = 0; j < num_nearest; j++) {
				nearestIds.put(l, j, (int) F.get(j, 0)[0]);
			}
		}
		
		return nearestIds;
	}
}
