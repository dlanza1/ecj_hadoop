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

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.hadoop.writables.ImageWritable;
import ec.app.facerecognition.hadoop.writables.MatEWithIDWritable;
import ec.app.facerecognition.hadoop.writables.MatEWritable;
import ec.app.facerecognition.hadoop.writables.TrainingResultsWritable;

public class QueryVectorMapper extends Mapper<NullWritable, ImageWritable, MatEWritable, MatEWithIDWritable> {

	private int windows_size;
	
	private TrainingResultsWritable trainingResults;
	
	@Override
	public void run(
			Mapper<NullWritable, ImageWritable, MatEWritable, MatEWithIDWritable>.Context context
			) throws IOException, InterruptedException {
		setup(context);
		try {
			long startTime = System.currentTimeMillis();
			
			int i = 0;
			while (context.nextKeyValue()) {
				map(context.getCurrentKey(), context.getCurrentValue(), context);
				i++;
				
				if(i % 50 == 0) 
					System.gc();
			}
			
			System.out.println((System.currentTimeMillis() - startTime) + " ms to run the loop");
		} finally {
			cleanup(context);
		}
	}
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		
		windows_size = conf.getInt(EvaluateIndividual.WINDOWS_SIZE_PARAM, 5);
		
		//Get training results
		String file = context.getConfiguration().get(EvaluateIndividual.INDIVIDUAL_DIR_PARAM).concat("training/part-r-00000");
		System.out.println("reading training results from " + file);
		
		Path path = new Path("hdfs:" + file);
		
		FileSystem fs = FileSystem.get(conf);
		
		@SuppressWarnings("resource")
		Reader reader = new Reader(fs.getConf(), Reader.file(path));
	    NullWritable key = NullWritable.get();
	    trainingResults = new TrainingResultsWritable();
	    
		reader.next(key, trainingResults);
		
		System.out.println("Training results:");
		System.out.println(trainingResults);
	}
	
	@Override
	protected void map(NullWritable key, ImageWritable image, Context context)
			throws IOException, InterruptedException {;
		
		MatE normalized_params = image.getParameters(windows_size).normalize(trainingResults.getMaxPerCol());
		
//		System.out.println("Normalized params:");
//		System.out.println(normalized_params);
		
		MatE idCenters = knn(trainingResults.getCenters(), normalized_params);

//		System.out.println("ID centers:");
//		System.out.println(idCenters);
		
		MatE queryVector = MatE.zeros(1, trainingResults.getCenters().rows(), CvType.CV_64F);
		
		for (int poi_index = 0; poi_index < idCenters.rows(); poi_index++) {
			int x = (int) idCenters.get(poi_index, 0)[0];
			double y = queryVector.get(0, x)[0];
			queryVector.put(0, x, y + 1);
		}
		
//		System.out.println("Query vector:");
//		System.out.println(queryVector);
		
		context.write(new MatEWritable(trainingResults.getTextureIndexMatrix()), 
						new MatEWithIDWritable(image.getId(), queryVector));
	}

	private MatE knn(MatE centers, MatE normalized_params) {
		MatE idCenters = MatE.zeros(normalized_params.rows(), 1, CvType.CV_32F);

		MatE dis;
		int num_centers = centers.rows();

		for (int poi_index = 0; poi_index < normalized_params.rows(); poi_index++) {
			MatE dis1 = MatE.zeros(num_centers, 1, CvType.CV_64F);

			dis = normalized_params.rep(num_centers, poi_index);

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

}