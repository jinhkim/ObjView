package com.ubertome.objview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StopWatch {
	
	private volatile long start, stop;
	
	public StopWatch(){
		
	}

	public long getElapsedMilliseconds(){
		long elapsed = stop - start;
//		DateFormat df = DateFormat.getTimeInstance();
//		NumberFormat nf = NumberFormat.getInstance();
//		nf.format(elapsed);
//		df.setNumberFormat(nf);
		
		return elapsed;
	}
	
	/**
	 * 
	 * @return elapsed seconds
	 */
	public synchronized int getElapsedSeconds(){
		long elapsed = stop - start;
		
		
		return (int) Math.floor(elapsed/1000);
	}
	
	/**
	 *  
	 * @return the decimal part of the elapsed time, in milliseconds
	 */
	public float getElapsedDecimal(){
		long elapsed = stop - start;
		float time = elapsed/1000f;
		return (float) (time - Math.floor(time));
	}
	
	/**
	 * 
	 * @return The time elapsed in seconds, including decimals
	 */
	public float getElapsedFloat(){
		return (stop-start)/1000f;
	}
	
	public synchronized String getTimeString(){
		Date date = new Date(stop-start);
		DateFormat dateFormat = new SimpleDateFormat("mm:ss:SSS");
		return dateFormat.format(date);
	}
	
	public synchronized void startTime(){
		start = System.currentTimeMillis();
	}
	
	public synchronized void stopTime(){
		stop = System.currentTimeMillis();
	}
	
}
