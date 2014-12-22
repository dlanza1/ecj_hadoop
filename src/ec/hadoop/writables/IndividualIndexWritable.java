package ec.hadoop.writables;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A {@link WritableComparable} which encapsulates an ordered pair of signed
 * integers.
 */
public final class IndividualIndexWritable implements
		WritableComparable<IndividualIndexWritable>{

	private IntWritable subpopulation;
	private IntWritable individual;
	
	public IndividualIndexWritable() {
		subpopulation = new IntWritable(0);
		individual = new IntWritable(0); 
	}
	
	public IndividualIndexWritable(int _subpop, int _individual) {
		subpopulation = new IntWritable(_subpop);
		individual = new IntWritable(_individual); 
	}

	@Override
	public void write(DataOutput out) throws IOException {
		subpopulation.write(out);
		individual.write(out);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		subpopulation.readFields(in);
		individual.readFields(in);
	}

	@Override
	public int compareTo(IndividualIndexWritable other) {
		int f_c = this.subpopulation.compareTo(other.subpopulation);
		return f_c != 0 ? f_c : this.individual.compareTo(other.individual);
	}

	public int getSubpopulation(){
		return subpopulation.get();
	}
	
	public int getIndividual(){
		return individual.get();
	}
}
