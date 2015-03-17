/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.app.facerecognition;

import java.util.Collections;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import ec.Evaluator;
import ec.EvolutionState;
import ec.Fitness;
import ec.Individual;
import ec.Population;
import ec.Subpopulation;
import ec.app.facerecognition.hadoop.EvaluateIndividual;
import ec.app.facerecognition.hadoop.input.ImageRecordReader;
import ec.util.Parameter;
import ec.util.ThreadPool;
import ec.vector.BitVectorIndividual;

/* 
 * SimpleEvaluator.java
 * 
 * Created: Wed Aug 18 21:31:18 1999
 * By: Sean Luke
 */

/**
 * The SimpleEvaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its own
 * problem space. One Problem instance is cloned from p_problem for each
 * evaluating thread. The Problem must implement SimpleProblemForm.
 * 
 * @author Sean Luke
 * @version 2.0
 * 
 *          Thanks to Ralf Buschermohle <lobequadrat@googlemail.com> for early
 *          versions of code which led to this version.
 * 
 */

@SuppressWarnings("serial")
public class HadoopEvaluator extends Evaluator {
	public static final String P_CLONE_PROBLEM = "clone-problem";
	public static final String P_NUM_TESTS = "num-tests";
	public static final String P_MERGE = "merge";

	public static final String V_MEAN = "mean";
	public static final String V_MEDIAN = "median";
	public static final String V_BEST = "best";

	public static final String P_CHUNK_SIZE = "chunk-size";
	public static final String V_AUTO = "auto";

	public static final int MERGE_MEAN = 0;
	public static final int MERGE_MEDIAN = 1;
	public static final int MERGE_BEST = 2;

	public int numTests = 1;
	public int mergeForm = MERGE_MEAN;

	Object[] lock = new Object[0]; // Arrays are serializable
	int individualCounter = 0;
	int subPopCounter = 0;
	int chunkSize; // a value >= 1, or C_AUTO
	public static final int C_AUTO = 0;

	public ThreadPool pool = new ThreadPool();
	
	private Configuration conf;

	// checks to make sure that the Problem implements SimpleProblemForm
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		numTests = state.parameters.getInt(base.push(P_NUM_TESTS), null, 1);
		if (numTests < 1)
			numTests = 1;
		else if (numTests > 1) {
			String m = state.parameters.getString(base.push(P_MERGE), null);
			if (m == null)
				state.output.warning("Merge method not provided to SimpleEvaluator.  Assuming 'mean'");
			else if (m.equals(V_MEAN))
				mergeForm = MERGE_MEAN;
			else if (m.equals(V_MEDIAN))
				mergeForm = MERGE_MEDIAN;
			else if (m.equals(V_BEST))
				mergeForm = MERGE_BEST;
			else
				state.output.fatal("Bad merge method: " + m,
						base.push(P_NUM_TESTS), null);
		}

		if (!state.parameters.exists(base.push(P_CHUNK_SIZE), null)) {
			chunkSize = C_AUTO;
		} else if (state.parameters.getString(base.push(P_CHUNK_SIZE), null)
				.equalsIgnoreCase(V_AUTO)) {
			chunkSize = C_AUTO;
		} else {
			chunkSize = (state.parameters.getInt(base.push(P_CHUNK_SIZE), null,
					1));
			if (chunkSize == 0) // uh oh
				state.output.fatal(
						"Chunk Size must be either an integer >= 1 or 'auto'",
						base.push(P_CHUNK_SIZE), null);
		}
		
		conf = new Configuration();
		conf.set(EvaluateIndividual.BASE_RUN_DIR_PARAM, EvaluateIndividual.BASE_RUN_DIR 
				+ EvaluateIndividual.getTimestamp() + "/");
		conf.setInt(ImageRecordReader.NUM_OF_SPLITS_PARAM, 10);
		conf.set(ImageRecordReader.IMAGES_FILE_PARAM, "/user/hdfs/ecj_hadoop/files.csv");
		conf.set(ImageRecordReader.POI_FILE_PARAM, "/user/hdfs/ecj_hadoop/poi.csv");
		conf.set(EvaluateIndividual.CLASSES_FILE_PARAM, "/user/hdfs/ecj_hadoop/classes.txt");
		
		//Disable logging
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for (Logger logger : loggers ) {
		    logger.setLevel(Level.OFF);
		}
	}

	Population oldpop = null;

	// replace the population with one that has some N copies of the original
	// individuals
	void expand(EvolutionState state) {
		Population pop = (Population) (state.population.emptyClone());

		// populate with clones
		for (int i = 0; i < pop.subpops.length; i++) {
			pop.subpops[i].individuals = new Individual[numTests
					* state.population.subpops[i].individuals.length];
			for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
				for (int k = 0; k < numTests; k++) {
					pop.subpops[i].individuals[numTests * j + k] = (Individual) (state.population.subpops[i].individuals[j]
							.clone());
				}
			}
		}

		// swap
		//Population oldpop = state.population;
		state.population = pop;
	}

	// Take the N copies of the original individuals and fold their fitnesses
	// back into the original
	// individuals, replacing them with the original individuals in the process.
	// See expand(...)
	void contract(EvolutionState state) {
		// swap back
		Population pop = state.population;
		state.population = oldpop;

		// merge fitnesses again
		for (int i = 0; i < pop.subpops.length; i++) {
			Fitness[] fits = new Fitness[numTests];
			for (int j = 0; j < state.population.subpops[i].individuals.length; j++) {
				for (int k = 0; k < numTests; k++) {
					fits[k] = pop.subpops[i].individuals[numTests * j + k].fitness;
				}

				if (mergeForm == MERGE_MEAN) {
					state.population.subpops[i].individuals[j].fitness
							.setToMeanOf(state, fits);
				} else if (mergeForm == MERGE_MEDIAN) {
					state.population.subpops[i].individuals[j].fitness
							.setToMedianOf(state, fits);
				} else // MERGE_BEST
				{
					state.population.subpops[i].individuals[j].fitness
							.setToBestOf(state, fits);
				}

				state.population.subpops[i].individuals[j].evaluated = true;
			}
		}
	}

	/**
	 * A simple evaluator that doesn't do any coevolutionary evaluation.
	 * Basically it applies evaluation pipelines, one per thread, to various
	 * subchunks of a new population.
	 */
	public void evaluatePopulation(final EvolutionState state) {
		if (numTests > 1)
			expand(state);

		Subpopulation[] subpops = state.population.subpops;
		Individual[] inds = subpops[0].individuals;

		try{
			EvaluateIndividual[] threads = new EvaluateIndividual[inds.length];

			//Creates threads
			for (int ind = 0; ind < inds.length; ind++)
				threads[ind] = new EvaluateIndividual(state, conf, state.generation, ind, (BitVectorIndividual) inds[ind]);
			
			//Run all thread
			for (int ind = 0; ind < inds.length; ind++)
				threads[ind].start();
			
			//Wait for all threads
			for (int ind = 0; ind < inds.length; ind++)
				threads[ind].join();
			
		}catch(Exception e){
			System.err.println("there was a problem evaluating the individuals");
		}

		if (numTests > 1)
			contract(state);
	}

	/**
	 * The SimpleEvaluator determines that a run is complete by asking each
	 * individual in each population if he's optimal; if he finds an individual
	 * somewhere that's optimal, he signals that the run is complete.
	 */
	public boolean runComplete(final EvolutionState state) {
		for (int x = 0; x < state.population.subpops.length; x++)
			for (int y = 0; y < state.population.subpops[x].individuals.length; y++)
				if (state.population.subpops[x].individuals[y].fitness
						.isIdealFitness())
					return true;
		return false;
	}

	// computes the chunk size if 'auto' is set. This may be different depending
	// on the subpopulation,
	// which is backward-compatible with previous ECJ approaches.
	int computeChunkSizeForSubpopulation(EvolutionState state, int subpop,
			int threadnum) {
		int numThreads = state.evalthreads;

		// we will have some extra individuals. We distribute these among the
		// early subpopulations
		int individualsPerThread = state.population.subpops[subpop].individuals.length
				/ numThreads; // integer division
		int slop = state.population.subpops[subpop].individuals.length
				- numThreads * individualsPerThread;

		if (threadnum >= slop) // beyond the slop
			return individualsPerThread;
		else
			return individualsPerThread + 1;
	}

}
