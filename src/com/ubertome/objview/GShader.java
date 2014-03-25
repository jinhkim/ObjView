package com.ubertome.objview;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import android.content.res.AssetManager;

import android.opengl.GLES20;

public class GShader {

	private int mHandle;
	private int type;
	private String filename;
	private String shaderCode;
	private String glVersion;
	private AssetManager ass;
	

	public GShader(){
		
	}
	
/**
 * Shader object which holds its parsed code, shader handle, and reference to the assets folder
 * @param assets - AssetManager passed from the main activity
 * @param filename - Name of the shader file to compile
 * @param type - Shader type (e.g. GLES20.GL_VERTEX_SHADER)
 */
	public GShader(AssetManager assets, String filename, int type) {
		this.filename = filename;
		this.type = type;
		this.ass = assets;
		
		
		loadShader();
	}
/**
 * Parses and compiles shader
 */
	private void loadShader() {
		
		InputStream is = null;
		try {
			is = ass.open(filename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		shaderCode = convertIStreamToString(is).replaceAll("\r\n", "");
		
		mHandle = GLES20.glCreateShader(type);
		
		GLES20.glShaderSource(mHandle, shaderCode);
		GLES20.glCompileShader(mHandle);
		checkForErrors();
	}
	
	private static String convertIStreamToString(InputStream is){
		java.util.Scanner ss = new java.util.Scanner(is).useDelimiter("\\A");
//		ss = ss.useDelimiter("\\r\\n");
		return ss.hasNext() ? ss.next() : "";
	}
	
	/**
	 * Checks for shader compile errors; any errors are recorded in {@code output.txt}
	 */
	private void checkForErrors(){
		int params[] = new int[1];
		String errorMessage;
		try {
			GLES20.glGetShaderiv(mHandle, GLES20.GL_COMPILE_STATUS, params, 0);
			
			//shader compile error occurs on Galaxy S i9000M,
			// but not on Galaxy S4 i337M
			if(params[0] == GLES20.GL_FALSE)
				throw new ShaderErrorException(GLES20.glGetShaderInfoLog(mHandle));
		}
		// error occurred; let user know
		catch (ShaderErrorException se) {
			Log.writeLog(GLES20.glGetShaderInfoLog(mHandle));
			se.printStackTrace();
		}
	}
	
	//get methods
	/**
	 * 
	 * @return int - Shader handle
	 */
	public int getHandle(){
		return mHandle;
	}

}
