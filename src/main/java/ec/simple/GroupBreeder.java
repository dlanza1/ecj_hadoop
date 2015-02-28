/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.simple;

import ec.BreedingPipeline;
import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.gp.koza.KozaFitnessWithTime;
import ec.time.utils.FitnessWithTime;
import ec.util.Parameter;
import ec.util.QuickSort;
import ec.util.SortComparator;

/* 
 * SimpleBreeder.java
 * 
 * Created: Tue Aug 10 21:00:11 1999
 * By: Sean Luke
 */

/**
 * Breeds each subpopulation separately, with no inter-population exchange, and
 * using a generational approach. A SimpleBreeder may have multiple threads; it
 * divvys up a subpopulation into chunks and hands one chunk to each thread to
 * populate. One array of BreedingPipelines is obtained from a population's
 * Species for each operating breeding thread.
 * 
 * <p>
 * Prior to breeding a subpopulation, a SimpleBreeder may first fill part of the
 * new subpopulation up with the best <i>n</i> individuals from the old
 * subpopulation. By default, <i>n</i> is 0 for each subpopulation (that is,
 * this "elitism" is not done). The elitist step is performed by a single
 * thread.
 * 
 * <p>
 * If the <i>sequential</i> parameter below is true, then breeding is done
 * specially: instead of breeding all Subpopulations each generation, we only
 * breed one each generation. The subpopulation index to breed is determined by
 * taking the generation number, modulo the total number of subpopulations. Use
 * of this parameter outside of a coevolutionary context (see
 * ec.coevolve.MultiPopCoevolutionaryEvaluator) is very rare indeed.
 * 
 * <p>
 * SimpleBreeder adheres to the default-subpop parameter in Population: if
 * either an 'elite' or 'reevaluate-elites' parameter is missing, it will use
 * the default subpopulation's value and signal a warning.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><tt><i>base</i>.elite.<i>i</i></tt><br>
 * <font size=-1>int >= 0 (default=0)</font></td>
 * <td valign=top>(the number of elitist individuals for subpopulation <i>i</i>)
 * </td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.reevaluate-elites.<i>i</i></tt><br>
 * <font size=-1>boolean (default = false)</font></td>
 * <td valign=top>(should we reevaluate the elites of subpopulation <i>i</i>
 * each generation?)</td>
 * </tr>
 * <tr>
 * <td valign=top><tt><i>base</i>.sequential</tt><br>
 * <font size=-1>boolean (default = false)</font></td>
 * <td valign=top>(should we breed just one subpopulation each generation (as
 * opposed to all of them)?)</td>
 * </tr>
 * </table>
 * 
 * 
 * @author Sean Luke
 * @version 1.0
 */

@SuppressWarnings("serial")
public class GroupBreeder extends SimpleBreeder {
	
	public static final String P_NUM_GROPUS = "groups";
	
	private int num_groups;
	
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);
		
		if(state.parameters.exists(base.push(P_NUM_GROPUS), null))
			num_groups = state.parameters.getInt(base.push(P_NUM_GROPUS), null);
		else
			num_groups = 1;
		
		state.output.warning("Number of groups = "+num_groups);
	}
	
	public int getNumGroups(){
		return num_groups;
	}

	/**
	 * A simple breeder that doesn't attempt to do any cross- population
	 * breeding. Basically it applies pipelines, one per thread, to various
	 * subchunks of a new population.
	 */
	public Population breedPopulation(EvolutionState state) {
		Population newpop = null;
		if (clonePipelineAndPopulation)
			newpop = (Population) state.population.emptyClone();
		else {
			if (backupPopulation == null)
				backupPopulation = (Population) state.population.emptyClone();
			newpop = backupPopulation;
			newpop.clear();
			backupPopulation = state.population; // swap in
		}
		
		// load elites into top of newpop
		loadElites(state, newpop);

		int numinds[][] = new int[num_groups][state.population.subpops.length];
		int from[][] = new int[num_groups][state.population.subpops.length];

		for (int subpop_index = 0; subpop_index < state.population.subpops.length; subpop_index++) {
			int length = computeSubpopulationLength(state, newpop, subpop_index, 0);

			// we will have some extra individuals. We distribute these among
			// the early subpopulations
			int individualsPerGroup = length / num_groups; // integer division
			int slop = length - num_groups * individualsPerGroup;
			int currentFrom = 0;

			for (int group_index = 0; group_index < num_groups; group_index++) {
				if (slop > 0) {
					numinds[group_index][subpop_index] = individualsPerGroup + 1;
					slop--;
				} else
					numinds[group_index][subpop_index] = individualsPerGroup;

				if (numinds[group_index][subpop_index] == 0) {
					state.output
							.warnOnce("More groups exist than individuals of some subpopulations (first example: subpopulation "
									+ subpop_index + ")");
				}

				from[group_index][subpop_index] = currentFrom;
				currentFrom += numinds[group_index][subpop_index];
				
				//Set num of group in each individual
				for (int ind_index = from[group_index][subpop_index]; 
						ind_index < from[group_index][subpop_index] + numinds[group_index][subpop_index]; 
						ind_index++) {
					FitnessWithTime fitness = (FitnessWithTime) state.population.subpops[subpop_index].individuals[ind_index].fitness;
					fitness.setGroup(group_index);
				}
			}
			
			//Short individuals by estimated evaluation time
			QuickSort.qsort(state.population.subpops[subpop_index].individuals, new EvaluationTimeComparator());
		}
				
		// start breed
		for (int group_index = 0; group_index < num_groups; group_index++)
			breedPopChunk(newpop, state, numinds[group_index], from[group_index]);
		
		return newpop;
	}

	/**
	 * A private helper function for breedPopulation which breeds a chunk of
	 * individuals in a subpopulation for a given thread. Although this method
	 * is declared public (for the benefit of a private helper class in this
	 * file), you should not call it.
	 */
	protected void breedPopChunk(Population newpop, EvolutionState state,
			int[] numinds, int[] from) {
		
		for (int subpop = 0; subpop < newpop.subpops.length; subpop++) {
			if(numinds[subpop] <= 0)
				break;
			
			// if it's subpop's turn and we're doing sequential breeding...
			if (!shouldBreedSubpop(state, subpop, 0)) {
				// instead of breeding, we should just copy forward this
				// subpopulation. We'll copy the part we're assigned
				for (int ind = from[subpop]; ind < numinds[subpop]
						- from[subpop]; ind++)
					// newpop.subpops[subpop].individuals[ind] =
					// (Individual)(state.population.subpops[subpop].individuals[ind].clone());
					// this could get dangerous
					newpop.subpops[subpop].individuals[ind] = state.population.subpops[subpop].individuals[ind];
			} else {
				// do regular breeding of this subpopulation
				BreedingPipeline bp = null;
				if (clonePipelineAndPopulation)
					bp = (BreedingPipeline) newpop.subpops[subpop].species.pipe_prototype
							.clone();
				else
					bp = (BreedingPipeline) newpop.subpops[subpop].species.pipe_prototype;

				// check to make sure that the breeding pipeline produces
				// the right kind of individuals. Don't want a mistake there!
				// :-)
				int x;
				if (!bp.produces(state, newpop, subpop, 0))
					state.output
							.fatal("The Breeding Pipeline of subpopulation "
									+ subpop
									+ " does not produce individuals of the expected species "
									+ newpop.subpops[subpop].species.getClass()
											.getName()
									+ " or fitness "
									+ newpop.subpops[subpop].species.f_prototype);
				bp.prepareToProduce(state, subpop, 0);

				// start breedin'!

				x = from[subpop];
				int upperbound = from[subpop] + numinds[subpop];
				while (x < upperbound)
					x += bp.produce(from[subpop], from[subpop]+numinds[subpop]-1, 
							1, upperbound - x, x, subpop,
							newpop.subpops[subpop].individuals, state,
							0);
				if (x > upperbound) // uh oh! Someone blew it!
					state.output
							.fatal("Whoa!  A breeding pipeline overwrote the space of another pipeline in subpopulation "
									+ subpop
									+ ".  You need to check your breeding pipeline code (in produce() ).");

				bp.finishProducing(state, subpop, 0);
			}
		}
	}
	
	class EvaluationTimeComparator implements SortComparator {
		
		@Override
		public boolean lt(Object a, Object b) {
			FitnessWithTime f_a = (FitnessWithTime) ((Individual)a).fitness;
			FitnessWithTime f_b = (FitnessWithTime) ((Individual)b).fitness;
			
			return f_a.evaluation_time() < f_b.evaluation_time();
		}

		@Override
		public boolean gt(Object a, Object b) {
			FitnessWithTime f_a = (FitnessWithTime) ((Individual)a).fitness;
			FitnessWithTime f_b = (FitnessWithTime) ((Individual)b).fitness;
			
			return f_a.evaluation_time() > f_b.evaluation_time();
		}
	}

}
