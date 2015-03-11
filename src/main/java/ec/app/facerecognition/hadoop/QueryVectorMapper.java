package ec.app.facerecognition.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.Mapper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.QueryVectorWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class QueryVectorMapper extends Mapper<NullWritable, ImageWritable, NullWritable, QueryVectorWritable> {

	private int roi_radius;
	
	private TrainingResultsWritable trainingResults;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		roi_radius = conf.getInt("ec.app.facerecognition.roi.radius", 5);
		
		//Get training results
		String file = context.getConfiguration().get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("training/part-r-00000");
		System.out.println("reading training results from " + file);
		
		Path path = new Path("hdfs:" + file);
		
		FileSystem fs = FileSystem.get(conf);
		System.out.println("Exists " + path + ": " + fs.exists(path));
		
		@SuppressWarnings("resource")
		Reader reader = new Reader(fs.getConf(), Reader.file(path));
	    NullWritable key = NullWritable.get();
	    trainingResults = new TrainingResultsWritable();
		reader.next(key, trainingResults);
	}
	
	@Override
	protected void map(NullWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {
		
		MatE normalized_params = image.getParameters(roi_radius).normalize(trainingResults.getMaxPerCol());
		
		MatE idCenters = knn(trainingResults.getCenters(), normalized_params);

		MatE queryVector = new MatE(Mat.zeros(1, trainingResults.getCenters().rows(), CvType.CV_64F));
		for (int poi_index = 0; poi_index < 60; poi_index++) {
			queryVector.put(0, (int) idCenters.get(poi_index, 0)[0], queryVector.get(0, (int) idCenters.get(poi_index, 0)[0])[0] + 1);
		}
		
		context.write(NullWritable.get(), new QueryVectorWritable(image.getId(), queryVector));
	}

	private MatE knn(MatE centers, MatE normalized_params) {
		MatE idCenters = MatE.zeros(normalized_params.rows(), 1, CvType.CV_32F);

		MatE dis;
		int num_centers = centers.rows();

		for (int poi_index = 0; poi_index < normalized_params.rows(); poi_index++) {
			MatE dis1 = MatE.zeros(num_centers, 1, CvType.CV_64F);

			dis = repMat(normalized_params, num_centers, poi_index);

			Core.subtract(dis, centers, dis);
			Core.pow(dis, 2.0, dis);
			double suma;
			for (int i = 0; i < dis.rows(); i++) {
				suma = 0;
				
				for (int j = 0; j < dis.cols(); j++){
					suma = suma + dis.get(i, j)[0];
				}
				
				dis1.put(i, 0, suma);
			}
			
			dis.release();

			Core.sortIdx(dis1, dis1, 1);

			idCenters.put(poi_index, 0, (int) dis1.get(0, 0)[0]);
		}
		
		return idCenters;
	}
	
	private MatE repMat(MatE matNor, int num_centers, int poi_num) {
		double valor;
		MatE copia = MatE.zeros(num_centers, matNor.cols(), CvType.CV_64F);
		for (int col = 0; col < matNor.cols(); col++) {
			valor = matNor.get(poi_num, col)[0];
			for (int row = 0; row < num_centers; row++)
				copia.put(row, col, valor);
		}
		return copia;
	}
}