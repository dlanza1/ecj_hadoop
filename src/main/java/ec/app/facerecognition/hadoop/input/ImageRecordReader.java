package ec.app.facerecognition.hadoop.input;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;

import ec.app.facerecognition.catalog.MatE;
import ec.app.facerecognition.catalog.POI;
import ec.app.facerecognition.hadoop.writables.ImageWritable;

public class ImageRecordReader extends RecordReader<NullWritable, ImageWritable> {
	
//	private static final Log LOG = LogFactory.getLog(ImageRecordReader.class);

	public static final String IMAGES_FILE_PARAM = "mapreduce.input.img.files";
	public static final String NUMBER_OF_IMAGES_PARAM = "mapreduce.input.img.num";
	
	public static final String POI_FILE_PARAM = "mapreduce.input.poi.file";
	public static final String FILTER_POI_PARAM = "mapreduce.input.poi.filter";
	
	public static final String NUM_OF_SPLITS_PARAM = "mapreduce.input.multifileinputformat.splits";
	
	private static final int DEFAULT_NUMBER_OF_POI = 76;
	
	private Path[] paths;
	private int index_path;
	
	private HashMap<String, LinkedList<POI>> filesAndPoi;
	
	private TaskAttemptContext context;
	private int number_of_images;

	private Path actualPath;
	private LinkedList<POI> actualPoi;

	@Override
	public void close() throws IOException {
	}

	@Override
	public NullWritable getCurrentKey() throws IOException, InterruptedException {
		return NullWritable.get();
	}

	@Override
	public ImageWritable getCurrentValue() throws IOException,
			InterruptedException {
		FileSystem fs = actualPath.getFileSystem(context.getConfiguration());
		
		ImageWritable image = new ImageWritable(actualPath.getName(), 
												MatE.fromFile(fs.open(actualPath)), 
												actualPoi);

		if(image.getValue() == null || !image.getValue().hasContent())
			throw new IOException("the image " + actualPath.getName() +" couldn't be loaded");
		
		return image;
	}

	@Override
	public float getProgress() throws IOException, InterruptedException {
		return (float) (number_of_images - filesAndPoi.size()) / (float) number_of_images;
	}

	@Override
	public void initialize(InputSplit inputSplit, TaskAttemptContext context)
			throws IOException, InterruptedException {
		this.paths = ((CombineFileSplit) inputSplit).getPaths();
		this.index_path = -1;
		this.context = context;
		
		loadConfigFiles();
	}

	private void loadConfigFiles() throws IOException {
		String filterPath_s = context.getConfiguration().get(IMAGES_FILE_PARAM);
		String poiPath_s = context.getConfiguration().get(POI_FILE_PARAM);
		if(filterPath_s == null || poiPath_s == null)
			throw new IllegalStateException(POI_FILE_PARAM 
					+ " and " + IMAGES_FILE_PARAM + " parameters should be set");
		
		String poi_filter =  context.getConfiguration().get(FILTER_POI_PARAM);
		int num_poi = -1;
		if(poi_filter != null){
			if(poi_filter.length() != DEFAULT_NUMBER_OF_POI)
				throw new IllegalStateException("the POI filter '" + poi_filter 
						+ "' doesn't have 76 characters (one per POI).");
			
			String poi_filter_ones = poi_filter.replaceAll("0", "");
			
			if(poi_filter_ones.replaceAll("1", "").length() != 0)
				throw new IllegalStateException("the POI filter '" + poi_filter 
						+ "' should only contain 1s and 0s.");
			
			num_poi = poi_filter_ones.replaceAll("0", "").length();
		}else{
			num_poi = DEFAULT_NUMBER_OF_POI;
		}
		
		Path filterPath = new Path(filterPath_s );
		Path poiPath = new Path(poiPath_s );
		
		FileSystem fs = filterPath.getFileSystem(context.getConfiguration());
		BufferedReader br_filter = new BufferedReader(new InputStreamReader(fs.open(filterPath)));
		BufferedReader br_poi = new BufferedReader(new InputStreamReader(fs.open(poiPath)));
		
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
        	for (int i = 0; i < coords.length && i < (2 * DEFAULT_NUMBER_OF_POI); i = i + 2){
        		if(poi_filter == null || poi_filter.charAt(i/2) == '1'){
        			pois.add(new POI(poi_index, 
        					(int)Float.parseFloat(coords[i]), 
        					(int)Float.parseFloat(coords[i+1])));
        			
        			poi_index++;
        		}
        	}
        	
        	if(num_poi != poi_index)
        		throw new IllegalStateException("there was an error reading the POI "
        				+ "(the number of poi readed is " + poi_index + " and should be " + num_poi + ")");
        	
        	filesAndPoi.put(file_name, pois);
        }
        
    	if(br_poi.readLine() != null)
    		throw new IllegalStateException("the configuration file " + poiPath.getName()
    				+ "has more lines that " + filterPath.getName());
    	
    	number_of_images = filesAndPoi.size();
    	
    	if(number_of_images != context.getConfiguration().getInt(NUMBER_OF_IMAGES_PARAM, -1))
    		throw new IOException("the number of lines of the config files must be equal that " + NUMBER_OF_IMAGES_PARAM);
        	
		br_filter.close();
		br_poi.close();
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
		index_path++;
		
		while(filesAndPoi != null 
				&& index_path < paths.length 
				&& !filesAndPoi.containsKey(paths[index_path].getName()))
			index_path++;
		
		if(index_path < paths.length){
			actualPath = paths[index_path];
			actualPoi = filesAndPoi.remove(actualPath.getName());
			
			return true;
		}else{
			return false;
		}
	}

}
