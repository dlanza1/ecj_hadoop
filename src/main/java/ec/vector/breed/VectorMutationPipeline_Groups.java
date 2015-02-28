/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.vector.*;
import ec.*;

/* 
 * VectorMutationPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 VectorMutationPipeline is a BreedingPipeline which implements a simple default Mutation
 for VectorIndividuals.  Normally it takes an individual and returns a mutated 
 child individual. VectorMutationPipeline works by calling defaultMutate(...) on the 
 parent individual.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 (however many its source produces)

 <p><b>Number of Sources</b><br>
 1

 <p><b>Default Base</b><br>
 vector.mutate (not that it matters)

 * @author Sean Luke
 * @version 1.0
 */

@SuppressWarnings("serial")
public class VectorMutationPipeline_Groups extends VectorMutationPipeline{

	public int produce(
	    	final int min, 
	        final int max, 
	        final int start,
	        final int subpopulation,
	        final Individual[] inds,
	        final EvolutionState state,
	        final int thread) {
	
		try {
			throw new Exception("This methos must not be used if you want to use groups.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
	
    public int produce(
    	final int first_ind_group,
    	final int last_ind_group,
    	final int min, 
        final int max, 
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread) {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(first_ind_group, last_ind_group, min,max,start,subpopulation,inds,state,thread);
        
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did

        // clone the individuals if necessary
        if (!(sources[0] instanceof BreedingPipeline))
            for(int q=start;q<n+start;q++)
                inds[q] = (Individual)(inds[q].clone());

        // mutate 'em
        for(int q=start;q<n+start;q++)
            {
            ((VectorIndividual)inds[q]).defaultMutate(state,thread);
            ((VectorIndividual)inds[q]).evaluated=false;
            }

        return n;
        }

}
    
    
