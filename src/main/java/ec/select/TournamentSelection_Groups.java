/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.select;


import ec.*;
import ec.steadystate.*;

/* 
 * TournamentSelection.java
 * 
 * Created: Mon Aug 30 19:27:15 1999
 * By: Sean Luke
 */

/**
 * Does a simple tournament selection, limited to the subpopulation it's working
 * in at the time.
 * 
 * <p>
 * Tournament selection works like this: first, <i>size</i> individuals are
 * chosen at random from the population. Then of those individuals, the one with
 * the best fitness is selected.
 * 
 * <p>
 * <i>size</i> can be any floating point value >= 1.0. If it is a non- integer
 * value <i>x</i> then either a tournament of size ceil(x) is used (with
 * probability x - floor(x)), else a tournament of size floor(x) is used.
 * 
 * <p>
 * Common sizes for <i>size</i> include: 2, popular in Genetic Algorithms
 * circles, and 7, popularized in Genetic Programming by John Koza. If the size
 * is 1, then individuals are picked entirely at random.
 * 
 * <p>
 * Tournament selection is so simple that it doesn't need to maintain a cache of
 * any form, so many of the SelectionMethod methods just don't do anything at
 * all.
 * 
 * 
 * <p>
 * <b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 * Always 1.
 * 
 * <p>
 * <b>Parameters</b><br>
 * <table>
 * <tr>
 * <td valign=top><i>base.</i><tt>size</tt><br>
 * <font size=-1>float &gt;= 1</font></td>
 * <td valign=top>(the tournament size)</td>
 * </tr>
 * 
 * <tr>
 * <td valign=top><i>base.</i><tt>pick-worst</tt><br>
 * <font size=-1> bool = <tt>true</tt> or <tt>false</tt> (default)</font></td>
 * <td valign=top>(should we pick the <i>worst</i> individual in the tournament
 * instead of the <i>best</i>?)</td>
 * </tr>
 * 
 * </table>
 * 
 * <p>
 * <b>Default Base</b><br>
 * select.tournament
 * 
 * 
 * @author Sean Luke
 * @version 1.0
 */

@SuppressWarnings("serial")
public class TournamentSelection_Groups extends TournamentSelection implements
		SteadyStateBSourceForm {
	
	/**
	 * @deprecated
	 */
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
	
	/**
	 * Produces the index of a (typically uniformly distributed) randomly chosen
	 * individual to fill the tournament. <i>number</> is the position of the
	 * individual in the tournament.
	 * @deprecated
	 */
	public int getRandomIndividual(int number, int subpopulation,
			EvolutionState state, int thread) {
		
		try {
			throw new Exception("This methos must not be used if you want to use groups.");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return 0;
	}
	
	/**
	 * Produces the index of a (typically uniformly distributed) randomly chosen
	 * individual of the group to fill the tournament. <i>number</> is the position of the
	 * individual in the tournament.
	 */
	public int getRandomIndividual(
			int number, 
			int subpopulation,
			int fisrt_ind_group,
			int last_ind_group,
			EvolutionState state, 
			int thread) {
		
		return fisrt_ind_group + state.random[thread].nextInt(last_ind_group - fisrt_ind_group + 1);
	}
	
	public int produce(
			final int subpopulation, 
			final int fisrt_ind_group,
			final int last_ind_group,
			final EvolutionState state,
			final int thread) {
		
		// pick size random individuals, then pick the best.
		Individual[] oldinds = state.population.subpops[subpopulation].individuals;
		int best = getRandomIndividual(0, subpopulation, fisrt_ind_group, last_ind_group, state, thread);

		int s = getTournamentSizeToUse(state.random[thread]);

		if (pickWorst)
			for (int x = 1; x < s; x++) {
				int j = getRandomIndividual(x, subpopulation, fisrt_ind_group, last_ind_group, state, thread);
				if (!betterThan(oldinds[j], oldinds[best], subpopulation,
						state, thread)) // j is at least as bad as best
					best = j;
			}
		else
			for (int x = 1; x < s; x++) {
				int j = getRandomIndividual(x, subpopulation, fisrt_ind_group, last_ind_group, state, thread);
				if (betterThan(oldinds[j], oldinds[best], subpopulation, state,
						thread)) // j is better than best
					best = j;
			}

		return best;
	}
}
