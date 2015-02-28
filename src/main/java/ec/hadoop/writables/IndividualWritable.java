package ec.hadoop.writables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

import ec.EvolutionState;
import ec.Individual;

public class IndividualWritable implements WritableComparable<IndividualWritable> {

	private EvolutionState state;
	private Individual individual;
	
	public IndividualWritable(EvolutionState state, Individual individual){
		this.state = state;
		this.individual = individual;
	}
	
	@Override
	public void write(DataOutput out) throws IOException {
		individual.writeIndividual(state, out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		individual.readIndividual(state, in);
	}

	@Override
	public int compareTo(IndividualWritable other) {
		return individual.compareTo(other.individual);
	}

	public Individual getIndividual(){
		return individual;
	}
	
	public EvolutionState getEvaluationState(){
		return state;
	}
}
