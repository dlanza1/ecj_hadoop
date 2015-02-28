package ec.time.utils;

public interface FitnessWithTime {

	public long evaluation_time();
	
	public void setEvaluation_time(long _t);
	
	public long getGroup();
	
	public void setGroup(int group);
	
}
