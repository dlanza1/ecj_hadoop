/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;


import ec.vector.*;
import ec.*;
import ec.util.*;

/* 
 * VectorCrossoverPipeline.java
 * 
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 VectorCrossoverPipeline is a BreedingPipeline which implements a simple default crossover
 for VectorIndividuals.  Normally it takes two individuals and returns two crossed-over 
 child individuals.  Optionally, it can take two individuals, cross them over, but throw
 away the second child (a one-child crossover).  VectorCrossoverPipeline works by calling
 defaultCrossover(...) on the first parent individual.
 
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 2 * minimum typical number of individuals produced by each source, unless tossSecondParent
 is set, in which case it's simply the minimum typical number.

 <p><b>Number of Sources</b><br>
 2

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base</i>.<tt>toss</tt><br>
 <font size=-1>bool = <tt>true</tt> or <tt>false</tt> (default)</font>/td>
 <td valign=top>(after crossing over with the first new individual, should its second sibling individual be thrown away instead of adding it to the population?)</td></tr>
 </table>

 <p><b>Default Base</b><br>
 vector.xover

 * @author Sean Luke
 * @version 1.0
 */

@SuppressWarnings("serial")
public class VectorCrossoverPipeline_Groups extends VectorCrossoverPipeline{
	
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
		
        // how many individuals should we make?
        int n = typicalIndsProduced();
        if (n < min) n = min;
        if (n > max) n = max;
                
        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, true);  // DO produce children from source -- we've not done so already

        for(int q=start;q<n+start; /* no increment */)  // keep on going until we're filled up
            {
            // grab two individuals from our sources
            if (sources[0]==sources[1])  // grab from the same source
                {
                sources[0].produce(first_ind_group, last_ind_group, 2,2,0,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    { 
                    parents[0] = (VectorIndividual)(parents[0].clone());
                    parents[1] = (VectorIndividual)(parents[1].clone());
                    }
                }
            else // grab from different sources
                {
                sources[0].produce(first_ind_group, last_ind_group, 1,1,0,subpopulation,parents,state,thread);
                sources[1].produce(first_ind_group, last_ind_group, 1,1,1,subpopulation,parents,state,thread);
                if (!(sources[0] instanceof BreedingPipeline))  // it's a selection method probably
                    parents[0] = (VectorIndividual)(parents[0].clone());
                if (!(sources[1] instanceof BreedingPipeline)) // it's a selection method probably
                    parents[1] = (VectorIndividual)(parents[1].clone());
                }
                
            // at this point, parents[] contains our two selected individuals,
            // AND they're copied so we own them and can make whatever modifications
            // we like on them.
    
            // so we'll cross them over now.  Since this is the default pipeline,
            // we'll just do it by calling defaultCrossover on the first child
            
            parents[0].defaultCrossover(state,thread,parents[1]);
            parents[0].evaluated=false;
            parents[1].evaluated=false;
            
            // add 'em to the population
            inds[q] = parents[0];
            q++;
            if (q<n+start && !tossSecondParent)
                {
                inds[q] = parents[1];
                q++;
                }
            }
        return n;
	}
	
}
    
    
    
    
    
    
    
