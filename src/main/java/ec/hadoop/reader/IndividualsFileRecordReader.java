package ec.hadoop.reader;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import ec.EvolutionState;
import ec.Individual;
import ec.util.Checkpoint;
import ec.hadoop.writables.IndividualWritable;
import ec.hadoop.writables.IndividualIndexWritable;

public class IndividualsFileRecordReader extends RecordReader<IndividualIndexWritable, IndividualWritable> {

	private EvolutionState state;
	private SequenceFile.Reader in;
	private long start;
	private long end;
	private boolean more = true;
	private IndividualIndexWritable key = null;
	private IndividualWritable value = null;
	protected Configuration conf;

	@Override
	public void initialize(InputSplit split, TaskAttemptContext context)
			throws IOException, InterruptedException {
		FileSplit fileSplit = (FileSplit) split;
		conf = context.getConfiguration();
		Path path = fileSplit.getPath();
		
		path.getFileSystem(conf);
		this.in = new SequenceFile.Reader(conf, Reader.file(path));
		
		this.end = fileSplit.getStart() + fileSplit.getLength();

		if (fileSplit.getStart() > in.getPosition()) {
			in.sync(fileSplit.getStart()); // sync to start
		}

		this.start = in.getPosition();
		more = start < end;
		
		//Get state from distributed cache
		Path[] uris = DistributedCache.getLocalCacheFiles(context
				.getConfiguration());

		try {
			state = Checkpoint.restoreFromCheckpoint(uris[0].toString());

			System.out.println("Checkpoint loaded: " + uris[0].toString());
		} catch (Exception e) {
			System.err.println("Checkpoint can't be restored.");
			e.printStackTrace();

			System.exit(1);
		}
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		if (!more) {
			return false;
		}
		long pos = in.getPosition();
		
		key = new IndividualIndexWritable();
		if (!in.next(key) || (pos >= end && in.syncSeen())) {
			more = false;
			key = null;
			value = null;
		} else {
			Individual ind = state.population.subpops[key.getSubpopulation()]
					.species.newIndividual(state, 0);
			value = new IndividualWritable(state, ind);
			in.getCurrentValue(value);	
		}		
		
		return more;
	}

	@Override
	public IndividualIndexWritable getCurrentKey() {
		return key;
	}

	@Override
	public IndividualWritable getCurrentValue() {
		return value;
	}

	/**
	 * Return the progress within the input split
	 * 
	 * @return 0.0 to 1.0 of the input byte range
	 */
	public float getProgress() throws IOException {
		if (end == start) {
			return 0.0f;
		} else {
			return Math.min(1.0f, (in.getPosition() - start)
					/ (float) (end - start));
		}
	}

	public synchronized void close() throws IOException {
		in.close();
	}

}
