package ec.gp.koza;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.LineNumberReader;

import ec.EvolutionState;
import ec.time.utils.FitnessWithTime;
import ec.util.Code;
import ec.util.DecodeReturn;

@SuppressWarnings("serial")
public class KozaFitnessWithTime extends KozaFitness implements FitnessWithTime {

	/** Basic preamble for printing evaluation time values out */
	public static final String EVALUATION_TIME_PREAMBLE = " Evaluation time (us):";

	protected long evaluation_time;

	protected int group;

	public long evaluation_time() {
		return evaluation_time;
	}

	public void setEvaluation_time(long _t) {
		evaluation_time = _t;
	}

	public String fitnessToString() {
		return super.fitnessToString() + Code.encode(evaluation_time());
	}

	public String fitnessToStringForHumans() {
		return super.fitnessToStringForHumans() + EVALUATION_TIME_PREAMBLE
				+ evaluation_time();
	}

	public void readFitness(final EvolutionState state,
			final LineNumberReader reader) throws IOException {
		DecodeReturn d = Code.checkPreamble(FITNESS_PREAMBLE, state, reader);

		// extract fitness
		Code.decode(d);
		if (d.type != DecodeReturn.T_FLOAT)
			state.output.fatal("Reading Line " + d.lineNumber + ": "
					+ "Bad Fitness.");
		standardizedFitness = (float) d.d;

		// extract hits
		Code.decode(d);
		if (d.type != DecodeReturn.T_INT)
			state.output.fatal("Reading Line " + d.lineNumber + ": "
					+ "Bad Fitness.");

		hits = (int) d.l;

		// extract evaluation time
		Code.decode(d);
		if (d.type != DecodeReturn.T_LONG)
			state.output.fatal("Reading Line " + d.lineNumber + ": "
					+ "Bad Fitness.");

		evaluation_time = d.l;
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
	public long getGroup() {
		return group;
	}

	@Override
	public void setGroup(int group) {
		this.group = group;
	}

}
