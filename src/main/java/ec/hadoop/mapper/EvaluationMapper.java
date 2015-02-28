package ec.hadoop.mapper;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;

import ec.EvolutionState;
import ec.Individual;
import ec.hadoop.writables.FitnessWritable;
import ec.hadoop.writables.IndividualWritable;
import ec.hadoop.writables.IndividualIndexWritable;
import ec.simple.SimpleProblemForm;

public class EvaluationMapper extends Mapper<IndividualIndexWritable, IndividualWritable, IndividualIndexWritable, FitnessWritable> {

	public static EvolutionState state;

	@Override
	protected void map(IndividualIndexWritable key, IndividualWritable value, Context context)
			throws IOException, InterruptedException {
		
		state = value.getEvaluationState();
		Individual ind = value.getIndividual();
		
		//Evaluate individual
		SimpleProblemForm problem = ((SimpleProblemForm) state.evaluator.p_problem);
		problem.evaluate(
				state,
				ind,
				key.getSubpopulation(), 
				0);

		//Emit key and value
		context.write(
				key,
				new FitnessWritable(state, ind.fitness));
	}
}
