package ec.simple;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;

import ec.EvolutionState;
import ec.time.utils.FitnessWithTime;
import ec.util.Code;

/*
 * SimpleTimeFitness.java
 *
 * Created: 30/6/2014
 * By: Daniel Lanza
 */

@SuppressWarnings("serial")
public class SimpleFitnessWithTime extends SimpleFitness implements FitnessWithTime {
	
	/** Basic preamble for printing evaluation time values out */
	public static final String EVALUATION_TIME_PREAMBLE = " Evaluation time (us): ";

	protected long evaluation_time;

	protected int group;

	/**
	 * Deprecated -- now redefined to set the fitness but ALWAYS say that it's
	 * not ideal. If you need to specify that it's ideal, you should use the new
	 * function setFitness(final EvolutionState state, float _f, boolean
	 * _isIdeal).
	 * 
	 * @deprecated
	 */
	public void setFitness(final EvolutionState state, float _f, long _t) {
		setFitness(state, _f, _t, false);
	}

	public void setFitness(final EvolutionState state, float _f, long _t,
			boolean _isIdeal) {
		super.setFitness(state, _f, _isIdeal);
		
		evaluation_time = _t;
	}
	
	public long evaluation_time() {
		return evaluation_time;
	}

	public String fitnessToString() {
		return super.fitnessToString() 
				+ EVALUATION_TIME_PREAMBLE + Code.encode(evaluation_time());
	}

	public String fitnessToStringForHumans() {
		return super.fitnessToStringForHumans()
				+ EVALUATION_TIME_PREAMBLE + evaluation_time();
	}

	/** Presently does not decode the fact that the fitness is ideal or not */
	public void readFitness(final EvolutionState state,
			final LineNumberReader reader) throws IOException {
		
		setFitness(state, Code.readFloatWithPreamble(FITNESS_PREAMBLE, state,
				reader), Code.readLongWithPreamble(EVALUATION_TIME_PREAMBLE,
				state, reader));
	}

	public void writeFitness(final EvolutionState state,
			final DataOutput dataOutput) throws IOException {
		super.writeFitness(state, dataOutput);
		dataOutput.writeLong(evaluation_time);
	}

	public void readFitness(final EvolutionState state,
			final DataInput dataInput) throws IOException {
		super.readFitness(state, dataInput);
		evaluation_time = dataInput.readLong();
	}

	@Override
	public void setEvaluation_time(long _t) {
		evaluation_time = _t;
	}

	@Override
	public long getGroup() {
		return group;
	}

	@Override
	public void setGroup(int group) {
		this.group = group;
	}

}
