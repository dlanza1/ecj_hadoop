package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.Calendar;

import org.apache.hadoop.conf.Configuration;

import ec.app.facerecognition.hadoop.input.ImageRecordReader;

public class EvaluateIndividual extends Thread {

	public static final String BASE_RUN_DIR = "/user/hdfs/ecj_hadoop/run/";
	public static final String BASE_RUN_DIR_PARAM = "mapreduce.ecj_hadoop.basedir";
	
	public static final String OPENCV_JAR = "/user/hdfs/ecj_hadoop/lib/opencv-2410.jar";
	public static final String IMAGES_DIR = "/user/hdfs/ecj_hadoop/img/";

	public static final String INDIVIDUAL_DIR_PARAM = "ecj_hadoop.hdfs.individual_dir";

	public static final String CLASSES_FILE_PARAM = "mapreduce.input.img.classes";
	
	private Float fitness;

	public static void main(String[] args) throws IOException {
		Configuration conf = new Configuration();
		// conf.set("fs.default.name","hdfs://nodo1:8020/");

		conf.set(BASE_RUN_DIR_PARAM, BASE_RUN_DIR + getTimestamp() + "/");
		conf.setInt(ImageRecordReader.NUM_OF_SPLITS_PARAM, 30);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "/user/hdfs/ecj_hadoop/files.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "/user/hdfs/ecj_hadoop/poi.csv");
		conf.set(CLASSES_FILE_PARAM, "/user/hdfs/ecj_hadoop/classes.txt");
		
		EvaluateIndividual e = new EvaluateIndividual(conf, 0, 0, "00000000" + "00000000" // 0 - 15
																	+ "11111111" + "11111111" // 16 - 31
																	+ "11111111" + "11111111" // 32 - 47
																	+ "11111111" + "11111111" // 48 - 63
																	+ "11111111" + "1111" /* 64 - 75 */);
		e.start();
		EvaluateIndividual e1 = new EvaluateIndividual(conf, 0, 1, "00000000" + "00000000" // 0 - 15
																	+ "11111111" + "11111111" // 16 - 31
																	+ "11111111" + "11111111" // 32 - 47
																	+ "11111111" + "11111111" // 48 - 63
																	+ "11111111" + "1111" /* 64 - 75 */);
		e1.start();
		
		try {
			e.join();
			e1.join();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
		
		System.out.println(e.fitness);
		System.out.println(e1.fitness);
	}

	private TrainingJob traningJob;
	private QueryJob retrieveJob;

	public EvaluateIndividual(Configuration conf, int gen, int ind, String filter) throws IOException {
		conf.set(ImageRecordReader.FILTER_POI_PARAM, filter);
		
		conf.set(INDIVIDUAL_DIR_PARAM, 
				conf.get(BASE_RUN_DIR_PARAM).concat("generation=" + gen + "/").concat("individual=" + ind + "/"));

		traningJob = new TrainingJob(conf, gen, ind);
		retrieveJob = new QueryJob(conf, gen, ind);
	}

	@Override
	public void run() {
		super.run();

		try {
			if(!traningJob.run())
				throw new RuntimeException("there was a problem during the training phase");
			
			fitness = retrieveJob.run();
			if(fitness == null)
				throw new RuntimeException("there was a problem during the retreive phase");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getTimestamp() {
		Calendar cal = Calendar.getInstance();
		
		int month = (cal.get(Calendar.MONTH) + 1);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		return new String().concat(cal.get(Calendar.YEAR) + "-")
								.concat((month < 10 ? "0" + month : month) + "-")
								.concat((day < 10 ? "0" + day : day) + "-")
								.concat((hour < 10 ? "0" + hour : hour) + "-")
								.concat((minute < 10 ? "0" + minute : minute) + "-")
								.concat((second < 10 ? "0" + second : second) + "");
	}

}
