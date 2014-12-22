package ec.breed;


import ec.BreedingSource;
import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;

@SuppressWarnings("serial")
public class MultiBreedingPipeline_Groups extends MultiBreedingPipeline {

	/**
	 * @deprecated
	 */
	@Override
	public int produce(int min, int max, int start, int subpopulation,
			Individual[] inds, EvolutionState state, int thread) {

		try {
			throw new Exception("This methos must not be used if you want to use groups.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}

	@Override
	public int produce(int first_ind_group, int last_ind_group, int min,
			int max, int start, int subpopulation, Individual[] inds,
			EvolutionState state, int thread) {
		
		BreedingSource s = sources[BreedingSource.pickRandom(
                sources,state.random[thread].nextFloat())];
        int total;
        
        if (generateMax)
            {
            if (maxGeneratable==0)
                maxGeneratable = maxChildProduction();
            int n = maxGeneratable;
            if (n < min) n = min;
            if (n > max) n = max;

            total = s.produce(
                first_ind_group, last_ind_group, n,n,start,subpopulation,inds,state,thread);
            }
        else
            {
            total = s.produce(
                first_ind_group, last_ind_group, min,max,start,subpopulation,inds,state,thread);
            }
            
        // clone if necessary
        if (s instanceof SelectionMethod)
            for(int q=start; q < total+start; q++)
                inds[q] = (Individual)(inds[q].clone());
        
        return total;
	}
}
