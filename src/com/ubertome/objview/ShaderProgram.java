package com.ubertome.objview;

import android.opengl.GLES20;

public class ShaderProgram {
	
	private int mProgram;
	private GShader vs, fs;
	/**
	 * Shader program that links the vertex and fragment shaders into one unit. <br>
	 * 
	 * @param vertShader - Vertex shader
	 * @param fragShader - Fragment shader
	 */
	public ShaderProgram(GShader vertShader, GShader fragShader){
		
		vs = vertShader;
		fs = fragShader;
		
		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertShader.getHandle());
		GLES20.glAttachShader(mProgram,  fragShader.getHandle());
		GLES20.glLinkProgram(mProgram);
	}
	
	
	//************Get Methods*******************
	/**
	 * 
	 * @return Program handle
	 */
	public int getHandle() { return mProgram; }
	
	/**
	 * 
	 * @return vertex shader
	 */
	public GShader getVertexShader() {return vs;}
	
	/**
	 * 
	 * @return fragment shader
	 */
	public GShader getFragShader() {return fs;}
}
