package ec.app.facerecognition.hadoop;

import java.util.Calendar;

import org.apache.hadoop.conf.Configuration;

import ec.app.facerecognition.hadoop.input.ImageRecordReader;

public class EvaluateIndividual extends Thread {

	public static final String BASE_RUN_DIR = "/user/hdfs/ecj_hadoop/run/";
	public static final String OPENCV_JAR = "/user/hdfs/ecj_hadoop/lib/opencv-2410.jar";
	public static final String IMAGES_DIR = "/user/hdfs/ecj_hadoop/img/";

	public static final String INDIVIDUAL_DIR_PARAM = "ecj_hadoop.hdfs.individual_dir";

	public static void main(String[] args) {
		Configuration conf = new Configuration();
		// conf.set("fs.default.name","hdfs://nodo1:8020/");

		conf.setInt(ImageRecordReader.NUM_OF_SPLITS_PARAM, 30);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "/user/hdfs/ecj_hadoop/files.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "/user/hdfs/ecj_hadoop/poi.csv");
		
		EvaluateIndividual e = new EvaluateIndividual(conf, 0, 0, "00000000" + "00000000" // 0 - 15
																	+ "11111111" + "11111111" // 16 - 31
																	+ "11111111" + "11111111" // 32 - 47
																	+ "11111111" + "11111111" // 48 - 63
																	+ "11111111" + "1111" /* 64 - 75 */);
		e.start();
		EvaluateIndividual e1 = new EvaluateIndividual(conf, 0, 0, "00000000" + "00000000" // 0 - 15
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
	}

	private TrainingJob traningJob;
	private RetrieveJob retrieveJob;

	public EvaluateIndividual(Configuration conf, int gen, int ind, String filter) {
		conf.set(ImageRecordReader.FILTER_POI_PARAM, filter);
		
		conf.set(INDIVIDUAL_DIR_PARAM, new String(BASE_RUN_DIR + getTimestamp() + "/").concat(
									"generation=" + gen + "/").concat("individual=" + ind + "/"));

		traningJob = new TrainingJob(conf, gen, ind);
		retrieveJob = new RetrieveJob(conf, gen, ind);
	}

	@Override
	public void run() {
		super.run();

		try {
			if(!traningJob.run())
				throw new RuntimeException("there was a problem during the training phase");
			
			if(!retrieveJob.run())
				throw new RuntimeException("there was a problem during the retreive phase");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String getTimestamp() {
		Calendar cal = Calendar.getInstance();

		return new String().concat(cal.get(Calendar.YEAR) + "-")
								.concat((cal.get(Calendar.MONTH) + 1) + "-")
								.concat(cal.get(Calendar.DAY_OF_MONTH) + "-")
								.concat(cal.get(Calendar.HOUR) + "-")
								.concat(cal.get(Calendar.MINUTE) + "-")
								.concat(cal.get(Calendar.SECOND) + "");
	}

}
