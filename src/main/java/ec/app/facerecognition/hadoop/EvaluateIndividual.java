package ec.app.facerecognition.hadoop;

import java.io.IOException;
import java.util.Calendar;

import org.apache.hadoop.conf.Configuration;

import ec.EvolutionState;
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.simple.SimpleFitness;
import ec.vector.BitVectorIndividual;

public class EvaluateIndividual extends Thread {

	public static final String BASE_RUN_DIR = "/user/hdfs/ecj_hadoop/run/";
	public static final String BASE_RUN_DIR_PARAM = "mapreduce.ecj_hadoop.basedir";
	
	public static final String OPENCV_JAR = "/user/hdfs/ecj_hadoop/lib/opencv-2410.jar";
	public static final String IMAGES_DIR = "/user/hdfs/ecj_hadoop/img/";

	public static final String INDIVIDUAL_DIR_PARAM = "ecj_hadoop.hdfs.individual_dir";

	public static final String CLASSES_FILE_PARAM = "mapreduce.input.img.classes";
	
	public static final String WINDOWS_SIZE_PARAM = "app.facerecognition.windows.size";
	public static final String NUM_CENTERS_PARAM = "app.facerecognition.centers.num";
	public static final String NUM_NEAREST_PARAM = "app.facerecognition.nearest.classes.num";
	
	private BitVectorIndividual individual;

	private TrainingJob traningJob;
	private QueryJob queryJob;
	private EvolutionState state;
	private int ind;

	public EvaluateIndividual(EvolutionState state, 
			Configuration conf, 
			int gen, 
			int ind, 
			BitVectorIndividual individual) 
					throws IOException {
		
		this.ind = ind;
		
		this.state = state;
		this.individual = individual;
		
		if(this.individual.evaluated)
			return;
		
		conf.set(ImageRecordReader.FILTER_POI_PARAM, individual.genotypeToStringForHumans());
		
		conf.set(INDIVIDUAL_DIR_PARAM, conf.get(BASE_RUN_DIR_PARAM).concat("generation=" + gen + "/").concat("individual=" + ind + "/"));

		traningJob = new TrainingJob(conf, gen, ind);
		queryJob = new QueryJob(conf, gen, ind);
	}

	@Override
	public void run() {
		super.run();
		
		if(this.individual.evaluated)
			return;

		try {
			System.out.println("Individual " + ind + ": running training phase...");
			
			if(traningJob != null && !traningJob.run())
				throw new RuntimeException("Individual " + ind + ": there was a problem during the training phase");
			
			System.out.println("Individual " + ind + ": training phase finished");
		} catch (Exception e) {
			throw new RuntimeException("Individual " + ind + ": there was a problem during the training phase");
		}
		
		try {
			System.out.println("Individual " + ind + ": running query phase...");
			
			Float fitness = queryJob.run();
			if(fitness == null)
				throw new RuntimeException("Individual " + ind + ": there was a problem during the query phase");
			
			System.out.println("Individual " + ind + ": query phase finished (fitness=" + fitness + ")");
			
			((SimpleFitness) individual.fitness).setFitness(state, fitness, fitness >= 1F);
			individual.evaluated = true;
		} catch (Exception e) {
			throw new RuntimeException("Individual " + ind + ": there was a problem during the query phase");
		}
	}

	public static String getTimestamp() {
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
