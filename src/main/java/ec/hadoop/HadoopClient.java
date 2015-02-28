package ec.hadoop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.SequenceFile.Writer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import ec.EvolutionState;
import ec.Individual;
import ec.Subpopulation;
import ec.hadoop.mapper.EvaluationMapper;
import ec.hadoop.reader.IndividualsFileInputFormat;
import ec.hadoop.writables.FitnessWritable;
import ec.hadoop.writables.IndividualWritable;
import ec.hadoop.writables.IndividualIndexWritable;

/**
 * 
 * @author daniellanzagarcia
 * 
 */
public class HadoopClient {

	private Configuration conf;
	private FileSystem hdfs;
	private String work_folder;

	public HadoopClient() throws IOException {
		this("localhost", "8020", "localhost", "8021");
	}

	public HadoopClient(String server_address) throws IOException {
		this(server_address, "8020", server_address, "8021");
	}

	public HadoopClient(String hdfs_address, String hdfs_port,
			String jobtracker_address, String jobtracker_port)
			throws IOException {

		conf = new Configuration();
		conf.set("fs.defaultFS", "hdfs://" + hdfs_address + ":" + hdfs_port);
		conf.set("mapred.job.tracker", jobtracker_address + ":"
				+ jobtracker_port);
		conf.set("fs.hdfs.impl",
				org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());

		hdfs = FileSystem.get(conf);

		work_folder = "hadoop_client_work_folder";

		hdfs.delete(new Path(work_folder), true);
	}

	public void setWorkFolder(String _work_folder) {
		work_folder = _work_folder;
	}

	public File addCacheFile(File local_file) throws IOException {
		return addCacheFile(local_file, false, false);
	}

	public File addCacheFile(File local_file, boolean delSrc, boolean overwrite)
			throws IOException {

		Path cache_folder = new Path(work_folder.concat("/cache"));
		File target_file = new File(cache_folder.toString().concat("/")
				.concat(local_file.getName()));

		hdfs.copyFromLocalFile(delSrc, overwrite,
				new Path(local_file.getAbsolutePath()),
				new Path(target_file.toString()));

		DistributedCache.addCacheFile(
				new Path(cache_folder, target_file.getName()).toUri(), conf);

		return new File(target_file.toString());
	}

	public Writer getSequenceFileWriter(Path output_file, Class<?> key_class,
			Class<?> value_class) throws IOException {
		return SequenceFile.createWriter(conf, Writer.file(output_file),
				Writer.keyClass(key_class), Writer.valueClass(value_class),
				Writer.compression(SequenceFile.CompressionType.NONE),
				Writer.blockSize((int) (1048576 * 14)),
				Writer.replication((short) 1), Writer.bufferSize(1024 * 100));
	}

	public void createInput(EvolutionState state) throws IOException {
		Path input_file = new Path(work_folder.concat("/input/population.seq"));
		Writer writer = getSequenceFileWriter(input_file,
				IndividualIndexWritable.class, IndividualWritable.class);

		Subpopulation[] subpops = state.population.subpops;
		int len = subpops.length;
		for (int pop = 0; pop < len; pop++) {
			for (int x = 0; x < subpops[pop].individuals.length; x++) {

				if (!subpops[pop].individuals[x].evaluated)
					writer.append(new IndividualIndexWritable(pop, x),
							new IndividualWritable(state,
									subpops[pop].individuals[x]));
			}
		}

		FileStatus fs = hdfs.getFileStatus(input_file);
		System.out.println(fs.toString());

		writer.close();
	}

	public void createInputHBase(EvolutionState state) throws IOException {
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "nodo1");
		conf.set("hbase.zookeeper.property.clientPort", "2181");

		HTable table;
		Put put = null;
		table = new HTable(conf, "population");

//		HBaseAdmin admin = new HBaseAdmin(conf);
//		admin.disableTable("population");
//        admin.deleteTable("population");
//        admin.close();
        
		Subpopulation[] subpops = state.population.subpops;
		int len = subpops.length;
		for (int pop = 0; pop < len; pop++) {
			for (int x = 0; x < subpops[pop].individuals.length; x++) {

				if (!subpops[pop].individuals[x].evaluated){
					DataOutputBuffer out = new DataOutputBuffer();
					
					new IndividualIndexWritable(pop, x).write(out);
					put = new Put(out.getData());
					
					out.reset();
					subpops[pop].individuals[x].writeIndividual(state, out);
					put.add(Bytes.toBytes("individual"), Bytes.toBytes("ind"), out.getData());
					
					table.put(put);
				}
			}
		}

		table.close();
	}

	public Job getEvaluationJob() throws IOException {

		conf.set("mapred.job.reuse.jvm.num.tasks", "-1");
		Job job = new Job(conf);
		job.setJarByClass(EvaluationMapper.class);
		job.setInputFormatClass(IndividualsFileInputFormat.class);
		job.setMapperClass(EvaluationMapper.class);
		job.setMapOutputKeyClass(IndividualIndexWritable.class);
		job.setMapOutputValueClass(FitnessWritable.class);

		job.setNumReduceTasks(0);

		Path input_directory = new Path(work_folder.concat("/input"));
		IndividualsFileInputFormat.addInputPath(job, input_directory);
		// IndividualsFileInputFormat.setMaxInputSplitSize(job, 2500000);
		job.setOutputFormatClass(SequenceFileOutputFormat.class);
		job.setOutputKeyClass(IndividualIndexWritable.class);
		job.setOutputValueClass(FitnessWritable.class);

		Path output_directory = new Path(work_folder.concat("/output"));
		hdfs.delete(output_directory, true);
		SequenceFileOutputFormat.setOutputPath(job, output_directory);

		SequenceFileOutputFormat.setOutputCompressionType(job,
				CompressionType.BLOCK);

		return job;
	}

	public void readFitness(EvolutionState state) throws FileNotFoundException,
			IOException {
		Path output_directory = new Path(work_folder.concat("/output"));

		// Get output files using a path filter
		FileStatus[] output_files = hdfs.listStatus(output_directory,
				new PathFilter() {
					@Override
					public boolean accept(Path path) {
						try {
							if (!hdfs.isFile(path))
								return false;
						} catch (IOException e) {
							return false;
						}

						if (path.getName().startsWith("part-"))
							return true;

						return false;
					}
				});

		// Set fitness
		IndividualIndexWritable key;
		SequenceFile.Reader reader;
		for (FileStatus output_file : output_files) {
			reader = new SequenceFile.Reader(conf,
					SequenceFile.Reader.file(output_file.getPath()));

			key = new IndividualIndexWritable();
			while (reader.next(key)) {
				Individual individual = state.population.subpops[key.getSubpopulation()].individuals[key
						.getIndividual()];
				reader.getCurrentValue(new FitnessWritable(state,
						individual.fitness));
				individual.evaluated = true;
			}

			reader.close();
		}

		// Check if all individuals were evaluated
		for (Individual individual : state.population.subpops[0].individuals) {
			if (individual.evaluated == false) {
				state.output.fatal("Some individuals weren't evaluated.");
			}
		}
	}
}
