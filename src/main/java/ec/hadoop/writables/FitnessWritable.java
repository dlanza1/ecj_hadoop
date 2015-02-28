package ec.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.EvolutionState;
import ec.Fitness;

public class FitnessWritable implements WritableComparable<FitnessWritable> {

	private EvolutionState state;
	private Fitness fitness;
	
	public FitnessWritable(EvolutionState state, Fitness fitness){
		this.state = state;
		this.fitness = fitness;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		fitness.writeFitness(state, out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		fitness.readFitness(state, in);
	}

	@Override
	public int compareTo(FitnessWritable other) {
		return fitness.compareTo(other);
	}

	public Fitness getFitness(){
		return fitness;
	}
}
