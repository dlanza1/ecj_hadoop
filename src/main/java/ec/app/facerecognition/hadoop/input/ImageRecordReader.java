package ec.app.facerecognition.hadoop.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.mortbay.log.Log;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.catalog.POI;
import ec.app.facerecognition.hadoop.writables.ImageWritable;

public class ImageRecordReader extends RecordReader<IntWritable, ImageWritable> {

	public static final String NAMES_FILE_PARAM = "mapreduce.input.namesfile";
	public static final String POI_FILE_PARAM = "mapreduce.input.poi.file";
	public static final String FILTER_POI_PARAM = "mapreduce.input.poi.filter";
	
	private static final int NUMBER_OF_POI = 76;
	
	//TODO Do not use paths, iterate over the HashMap
	private Path[] paths;
	private HashMap<String, LinkedList<POI>> filesAndPoi;
	
	private int index_path;
	private TaskAttemptContext context;

	@Override
	public void close() throws IOException {
	}

	@Override
	public IntWritable getCurrentKey() throws IOException, InterruptedException {
		return new IntWritable(index_path);
	}

	@Override
	public ImageWritable getCurrentValue() throws IOException,
			InterruptedException {
		
		Path path = paths[index_path];
		
		FileSystem fs = path.getFileSystem(context.getConfiguration());
		
		ImageWritable image = new ImageWritable(path.getName(), 
												MatE.fromFile(fs.open(path)), 
												filesAndPoi.get(path.getName()));

		if(image.getValue() == null || !image.getValue().hasContent())
			throw new IOException("the image " + path.getName() +" couldn't be loaded");
		
		return image;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return (float) index_path / (float) paths.length;
	}

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.paths = ((CombineFileSplit) inputSplit).getPaths();
		this.index_path = -1;
		this.context = context;
		
		loadConfigFiles();
	}

	private void loadConfigFiles() {
		String filterPath_s = context.getConfiguration().get(NAMES_FILE_PARAM);
		String poiPath_s = context.getConfiguration().get(POI_FILE_PARAM);
		if(filterPath_s == null || poiPath_s == null)
			throw new IllegalStateException(POI_FILE_PARAM 
					+ " and " + NAMES_FILE_PARAM + " parameters should be set");
		
		String poi_filter =  context.getConfiguration().get(FILTER_POI_PARAM);
		int num_poi = -1;
		if(poi_filter != null){
			if(poi_filter.length() != NUMBER_OF_POI)
				throw new IllegalStateException("the POI filter '" + poi_filter 
						+ "' doesn't have 76 characters (one per POI).");
			
			String poi_filter_ones = poi_filter.replaceAll("0", "");
			
			if(poi_filter_ones.replaceAll("1", "").length() != 0)
				throw new IllegalStateException("the POI filter '" + poi_filter 
						+ "' should only contain 1s and 0s.");
			
			num_poi = poi_filter_ones.replaceAll("0", "").length();
		}
		
		Path filterPath = new Path(filterPath_s );
		Path poiPath = new Path(poiPath_s );
		
		BufferedReader br_filter = null;
		BufferedReader br_poi = null;
		try {
			FileSystem fs = filterPath.getFileSystem(context.getConfiguration());
			br_filter = new BufferedReader(new InputStreamReader(fs.open(filterPath)));
			br_poi = new BufferedReader(new InputStreamReader(fs.open(poiPath)));
			
			filesAndPoi = new HashMap<String, LinkedList<POI>>();
			LinkedList<POI> pois;
			String file_name, poi_line;
	        while ((file_name = br_filter.readLine()) != null){
	        	poi_line = br_poi.readLine();
	        	if(poi_line == null)
	        		throw new IllegalStateException("the configuration file " + poiPath.getName()
	        				+ "has less lines that " + filterPath.getName());
	        	
	        	pois = new LinkedList<POI>();
	        	String[] coords = poi_line.split(" ");
	        	int poi_index = 0;
	        	for (int i = 0; i < coords.length && i < (2 * NUMBER_OF_POI); i = i + 2){
	        		if(poi_filter == null || poi_filter.charAt(i/2) == '1'){
	        			pois.add(new POI(poi_index, 
	        					(int)Float.parseFloat(coords[i]), 
	        					(int)Float.parseFloat(coords[i+1])));
	        			
	        			poi_index++;
	        		}
	        	}
	        	
	        	if(num_poi != -1 && poi_index != num_poi)
	        		throw new IllegalStateException("there was an error reading the POI "
	        				+ "(the number of poi readed is " + poi_index + " and should be " + num_poi + ")");
	        	
	        	filesAndPoi.put(file_name, pois);
	        }
	        
        	if(br_poi.readLine() != null)
        		throw new IllegalStateException("the configuration file " + poiPath.getName()
        				+ "has more lines that " + filterPath.getName());
		} catch (IOException e) {
			Log.info("there was a IO problem reading the configration files: " + filterPath + ", " + poiPath);
			e.printStackTrace();
			filesAndPoi = null;
		}finally{
			try {
				br_filter.close();
				br_poi.close();
			} catch (Exception e) {}
		}
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		index_path++;
		
		while(filesAndPoi != null 
				&& index_path < paths.length 
				&& !filesAndPoi.containsKey(paths[index_path].getName()))
			index_path++;
		
		return index_path < paths.length;
	}

}
