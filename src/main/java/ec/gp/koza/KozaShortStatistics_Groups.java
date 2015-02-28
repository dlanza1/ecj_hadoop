package ec.gp.koza;

import java.text.DecimalFormat;

import ec.EvolutionState;
import ec.Individual;
import ec.simple.GroupBreeder;
import ec.time.utils.FitnessWithTime;

@SuppressWarnings("serial")
public class KozaShortStatistics_Groups extends KozaShortStatistics {
	
	/**
	 * Prints out the statistics, but does not end with a println -- this lets
	 * overriding methods print additional statistics on the same line
	 */
	public void printExtraPopStatisticsAfter(final EvolutionState state) {
		boolean output = (state.generation % modulus == 0);

		int subpops = state.population.subpops.length; // number of supopulations
		
		Individual popBestSoFar = null;

		for (int x = 0; x < subpops; x++) {
			if (bestSoFar[x] != null
					&& (popBestSoFar == null || bestSoFar[x].fitness
							.betterThan(popBestSoFar.fitness)))
				popBestSoFar = bestSoFar[x];
		}
		
		// print out best individual info
		if (output) {
			FitnessWithTime fitness = (FitnessWithTime) popBestSoFar.fitness;
			
			state.output.print("" + fitness.getGroup()
					+ " ", statisticslog); // group of the best individual of pop so far
			
			state.output.print("" + fitness.evaluation_time()
					+ " ", statisticslog); // execution time of the best individual of pop so far
		}
		
		GroupBreeder breeder = (GroupBreeder)state.breeder;
		
		int num_groups = breeder.getNumGroups();
		
		int numinds[][] = new int[num_groups][state.population.subpops.length];
		int from[][] = new int[num_groups][state.population.subpops.length];

		for (int subpop_index = 0; subpop_index < state.population.subpops.length; subpop_index++) {
			int length = breeder.computeSubpopulationLength(state, state.population, subpop_index, 0);

			// we will have some extra individuals. We distribute these among
			// the early subpopulations
			int individualsPerGroup = length / num_groups; // integer division
			int slop = length - num_groups * individualsPerGroup;
			int currentFrom = 0;
			
			//Sale fitness 1 en la primera generacion
			//Checar fitness medio (no es igual el fitness medio de grupos que el de la generacion)

			for (int group_index = 0; group_index < num_groups; group_index++) {
				if (slop > 0) {
					numinds[group_index][subpop_index] = individualsPerGroup + 1;
					slop--;
				} else
					numinds[group_index][subpop_index] = individualsPerGroup;

				from[group_index][subpop_index] = currentFrom;
				currentFrom += numinds[group_index][subpop_index];
				
				//Calculate average size of the group
				int sum = 0;
				for(int indiv_index = from[group_index][subpop_index];indiv_index < from[group_index][subpop_index]+numinds[group_index][subpop_index];indiv_index++){
					sum += state.population.subpops[subpop_index].individuals[indiv_index].size();
				}

				if(output)
					state.output.print((sum/numinds[group_index][subpop_index]) + " ",  statisticslog);
			}
			
			float sum = 0;
			for (int group_index = 0; group_index < num_groups; group_index++) {
				sum = 0;
				for(int indiv_index = from[group_index][subpop_index];indiv_index < from[group_index][subpop_index]+numinds[group_index][subpop_index];indiv_index++){
					sum += state.population.subpops[subpop_index].individuals[indiv_index].fitness.fitness();
				}
				
				double resul = sum/numinds[group_index][subpop_index];
				if(output)
					state.output.print(new DecimalFormat("#0.0000000000000000").format(resul) + " ",  statisticslog);
			}
			
		}//Subpops
	}
	
}
