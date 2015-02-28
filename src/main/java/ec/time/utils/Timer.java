package ec.time.utils;

public class Timer {
	
	private long time_nano;
	private long time_ms;
	
	private boolean running;
	
	public Timer(){
		time_nano = 0;
		time_ms = 0;
		
		running = false;
	}
	
	public Timer start(){
		running = true;
		
		time_nano = System.nanoTime();
		time_ms = System.currentTimeMillis();
		
		return this;
	}
	
	public Timer reset(){
		time_nano = Math.abs(System.nanoTime() - time_nano);
		time_ms = System.currentTimeMillis();
		
		running = true;
		
		return this;
	}
	
	public Timer stop(){
		time_nano = Math.abs(System.nanoTime() - time_nano);
		time_ms = System.currentTimeMillis();
		
		running = false;
		
		return this;
	}
	
	public long getNano(){
		
		if(running)
			return Math.abs(System.nanoTime() - time_nano);
			
		return time_nano;
	}
	
	public long getMicro(){
		
		if(running)
			return Math.abs(System.nanoTime() - time_nano) / 1000;
		
		return time_nano / 1000;
	}
	
	public long getMili(){
		
		if(running)
			return System.currentTimeMillis() - time_ms;
		
		return time_ms;
	}
	
	public long getSec(){
		
		if(running)
			return (System.currentTimeMillis() - time_ms) / 1000;
		
		return time_ms;
	}

}
