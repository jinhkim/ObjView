package com.ubertome.objview;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
/**
 * Logs errors in an output file within the application
 * @author Jin
 *
 */
public class Log {
	static FileOutputStream file = null;
	static BufferedWriter bb = null;
	
	static boolean fileInit = false, bufferedWriterInit = false;
	
	public enum errorTypes{
		COMPILE_ERROR
	};
	
	/**
	 * Log class keeps only one file open, and writes only to that file until program execution ends
	 * @param fos - must be initialized with an output file before passing in
	 * @param mode - the file mode (MODE_PRIVATE, MODE_APPEND, etc.)
	 */
	public Log(FileOutputStream fos, int mode) {
		file = fos;
		fileInit = true;
		bb = null;
	}
	/**
	 * Log class keeps only one file open, and writes only to that file until program execution ends
	 * @param filename - name of file to open
	 * @param append - if true: append text onto the end of the file <br>
	 * 				   if false: create new file (overwrite existing file)
	 */
	public Log(String filename, boolean append){
		try{
			bb = new BufferedWriter(new FileWriter(filename, append));
			bufferedWriterInit = true;
		} catch (IOException io){
			io.printStackTrace();
		}
		file = null;
	}
	
	/**
	 * Writes a message to the current log file
	 * @param message - String value of message
	 */
	public static void writeLog(String message) {
		try {
			if (file != null) {
				if(bb == null){
					bb = new BufferedWriter(new OutputStreamWriter(file));
					bufferedWriterInit = true;
				}
				bb.write(message);
//				file.write(message.getBytes());
			} 
			else if(bb != null && file == null){
				bb.write(message);
			}
			else {
				System.out.println("Log not initialized properly");
				throw new LogNotInitializedException();
			}
		} catch (IOException io) {
			io.printStackTrace();
		} catch (LogNotInitializedException ee) {
			ee.printStackTrace();
		}
	}

	public static void closeLog(){
		try {
			if(fileInit)
				file.close();
			if(bufferedWriterInit)
				bb.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
