/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ec.app.facerecognition.hadoop.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * An abstract {@link InputFormat} that returns {@link MultiFileSplit}'s in
 * {@link #getSplits(JobConf, int)} method. Splits are constructed from the
 * files under the input paths. Each split returned contains <i>nearly</i> equal
 * content length. <br>
 * Subclasses implement {@link #getRecordReader(InputSplit, JobConf, Reporter)}
 * to construct <code>RecordReader</code>'s for <code>MultiFileSplit</code>'s.
 * 
 * @see MultiFileSplit
 */
@InterfaceAudience.Public
@InterfaceStability.Stable
public abstract class MultiFileInputFormat<K, V> extends FileInputFormat<K, V> {

	@Override
	public List<InputSplit> getSplits(JobContext jobContext) throws IOException {

		int numSplits = jobContext.getConfiguration().getInt("mapreduce.input.multifileinputformat.splits", 1);

		List<FileStatus> fileStatus = listStatus(jobContext);
		FileStatus[] fileStatus_ = new FileStatus[fileStatus.size()]; 
		fileStatus.toArray(fileStatus_);
		
		Path[] paths = FileUtil.stat2Paths(fileStatus_);
		List<InputSplit> splits = new ArrayList<InputSplit>(Math.min(numSplits, paths.length));
		
		if (paths.length != 0) {
			// HADOOP-1818: Manage splits only if there are paths
			long[] lengths = new long[paths.length];
			long totLength = 0;
			for (int i = 0; i < paths.length; i++) {
				FileSystem fs = paths[i].getFileSystem(jobContext.getConfiguration());
				lengths[i] = fs.getContentSummary(paths[i]).getLength();
				totLength += lengths[i];
			}
			double avgLengthPerSplit = ((double) totLength) / numSplits;
			long cumulativeLength = 0;

			int startIndex = 0;

			for (int i = 0; i < numSplits; i++) {
				int splitSize = findSize(i, avgLengthPerSplit,
						cumulativeLength, startIndex, lengths);
				if (splitSize != 0) {
					// HADOOP-1818: Manage split only if split size is not
					// equals to 0
					Path[] splitPaths = new Path[splitSize];
					long[] splitLengths = new long[splitSize];
					System.arraycopy(paths, startIndex, splitPaths, 0,
							splitSize);
					System.arraycopy(lengths, startIndex, splitLengths, 0,
							splitSize);
					splits.add(new CombineFileSplit(splitPaths, splitLengths));
					startIndex += splitSize;
					for (long l : splitLengths) {
						cumulativeLength += l;
					}
				}
			}
		}
		
		return splits;
	}

	private int findSize(int splitIndex, double avgLengthPerSplit,
			long cumulativeLength, int startIndex, long[] lengths) {

		if (splitIndex == lengths.length - 1)
			return lengths.length - startIndex;

		long goalLength = (long) ((splitIndex + 1) * avgLengthPerSplit);
		long partialLength = 0;
		// accumulate till just above the goal length;
		for (int i = startIndex; i < lengths.length; i++) {
			partialLength += lengths[i];
			if (partialLength + cumulativeLength >= goalLength) {
				return i - startIndex + 1;
			}
		}
		return lengths.length - startIndex;
	}

}