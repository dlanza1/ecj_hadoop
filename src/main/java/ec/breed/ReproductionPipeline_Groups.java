package ec.breed;


import ec.EvolutionState;
import ec.Individual;
import ec.SelectionMethod;

@SuppressWarnings("serial")
public class ReproductionPipeline_Groups extends ReproductionPipeline {

	@Override
	public int produce(int min, int max, int start, int subpopulation, Individual[] inds,
			EvolutionState state, int thread) {
		
		try {
			throw new Exception("This methos must not be used if you want to use groups.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
	
	@Override
	public int produce(int first_ind_group, int last_ind_group, 
			int min, int max, int start, int subpopulation, Individual[] inds,
			EvolutionState state, int thread) {
		
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(first_ind_group, last_ind_group, min,max,start,subpopulation,inds,state,thread);
                
        // this code is basically the same as BreedingPipeline.reproduce() but we copy it here
        // because of the 'mustClone' option.
                
        if (mustClone || sources[0] instanceof SelectionMethod)
            for(int q=start; q < n+start; q++)
                inds[q] = (Individual)(inds[q].clone());
        return n;
		
	}
}
