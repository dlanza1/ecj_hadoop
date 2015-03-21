/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.hadoop;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.mapreduce.Job;

import ec.*;
import ec.simple.SimpleProblemForm;
import ec.time.utils.Timer;
import ec.util.*;

/**
 * The SimpleEvaluator is a simple, non-coevolved generational evaluator which
 * evaluates every single member of every subpopulation individually in its own
 * problem space. One Problem instance is cloned from p_problem for each
 * evaluating thread. The Problem must implement SimpleProblemForm.
 * ddfdf
 * @author Sean Lukee
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

	public static final String P_HDFS_PREFIX = "hdfs-prefix";
	public static final String P_JOBTRACKER_ADDRESS = "jobtracker-address";
	public static final String P_JOBTRACKER_PORT = "jobtracker-port";
	public static final String P_HDFS_ADDRESS = "hdfs-address";
	public static final String P_HDFS_PORT = "hdfs-port";

	public static final int MERGE_MEAN = 0;
	public static final int MERGE_MEDIAN = 1;
	public static final int MERGE_BEST = 2;

	public int numTests = 1;
	public int mergeForm = MERGE_MEAN;
	public boolean cloneProblem;

	Object[] lock = new Object[0]; // Arrays are serializable
	int chunkSize; // a value >= 1, or C_AUTO
	private String work_folder;
	private String hdfs_address;
	private String hdfs_port;
	private String jobtracker_address;
	private String jobtracker_port;
	
	public static final int C_AUTO = 0;

	// checks to make sure that the Problem implements SimpleProblemForm
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		if (!(p_problem instanceof SimpleProblemForm))
			state.output.fatal("" + this.getClass()
					+ " used, but the Problem is not of SimpleProblemForm",
					base.push(P_PROBLEM));

		cloneProblem = state.parameters.getBoolean(base.push(P_CLONE_PROBLEM),
				null, true);
		if (!cloneProblem && (state.breedthreads > 1)) // uh oh, this can't be
														// right
			state.output
					.fatal("The Evaluator is not cloning its Problem, but you have more than one thread.",
							base.push(P_CLONE_PROBLEM));

		numTests = state.parameters.getInt(base.push(P_NUM_TESTS), null, 1);
		if (numTests < 1)
			numTests = 1;
		else if (numTests > 1) {
			String m = state.parameters.getString(base.push(P_MERGE), null);
			if (m == null)
				state.output
						.warning("Merge method not provided to SimpleEvaluator.  Assuming 'mean'");
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

		// Warnning only 1 thread
		if (state.evalthreads > 1) {
			state.output
					.warning("The execution is going to be only in one thread. (state.evalthreads = 1)");
		}

		// Load directory prefix parameter
		work_folder = state.parameters.getStringWithDefault(
				base.push(P_HDFS_PREFIX), null, "ecj_work_folder_hc");
		
		hdfs_address = state.parameters.getStringWithDefault(
				base.push(P_HDFS_ADDRESS), null, "nodo1");
		hdfs_port = state.parameters.getStringWithDefault(
				base.push(P_HDFS_PORT), null, "8020");
		jobtracker_address = state.parameters.getStringWithDefault(
				base.push(P_JOBTRACKER_ADDRESS), null, "nodo1");
		jobtracker_port = state.parameters.getStringWithDefault(
				base.push(P_JOBTRACKER_PORT), null, "8021");
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
		// Population oldpop = state.population;
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

		SimpleProblemForm prob = null;
		if (cloneProblem)
			prob = (SimpleProblemForm) (p_problem.clone());
		else
			prob = (SimpleProblemForm) (p_problem); // just use the
													// prototype
		
		((ec.Problem) prob).prepareToEvaluate(state, 0);
		
		try {
			HadoopClient hadoopClient = new HadoopClient(
					hdfs_address,
					hdfs_port,
					jobtracker_address,
					jobtracker_port);
			
			hadoopClient.setWorkFolder(work_folder);
			
			Timer t = new Timer().start();
			System.out.println("Evaluación iniciada.");
			
			// Create and upload checkpoint (without population)
			LinkedList<Individual[]> individuals_tmp = new LinkedList<Individual[]>();
			for (Subpopulation subp : state.population.subpops){
				individuals_tmp.add(subp.individuals);
				subp.individuals = null;
			}
			Checkpoint.setCheckpoint(state);
			int i = 0;
			for (Individual[] individuals : individuals_tmp) {
				state.population.subpops[i++].individuals = individuals;
			}
			
			hadoopClient.addCacheFile(new File("" + state.checkpointPrefix + "." + state.generation + ".gz"), true, true);
			System.out.println("Añadido checkpoint a cache " + t.getMili());
			t.start();

			//Generate input file
			hadoopClient.createInput(state);
			System.out.println("Creado y subido fichero de entrada " + t.getMili());
			t.start();
			
			// Submit job
			Job job = hadoopClient.getEvaluationJob(); 
			job.waitForCompletion(true);
			System.out.println("Trabajo hecho " + t.getMili());
			t.start();
			
			hadoopClient.readFitness(state);
			System.out.println("Leido fitness " + t.getMili());
			t.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		((ec.Problem) prob).finishEvaluating(state, 0);
		
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
				if (state.population.subpops[x].individuals[y].fitness.isIdealFitness())
					return true;
		
		return false;
	}

}
