package com.ubertome.objview;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.ubertome.objview.RenderFragment.RenderConfig;


public class Mesh {
	
	private FloatBuffer meshVertBuffer, meshTexBuffer, meshNormBuffer;
	Resources res;
	float meshVerts[], meshTex[], meshNormals[];
	private float modelMatrix[];
	float enableLighting, enableTextureHandle;
//	private int vertArray, texArray, normArray;
	
	private static int mProgram = 0;
	private static int mPositionHandle = 0, mNormalHandle = 0, mColorHandle = 0,
			mMVPMatrixHandle = 0, mModelViewMatrixHandle = 0, mViewMatrixHandle;
	private static int mTextureDataHandle = 0, mTexCoordHandle = 0, mTextureSampler = 0;
	private boolean buffersInit = false, texturesLoaded = false, hasTextures = false; 
	private BoundingSphere mSphere;
	private Bitmap material = null;
	private BitmapFactory.Options bmpOptions = null;
	private String texturePath;
	public enum DrawType {
		TRIANGLES,
		QUADS
	};
	DrawType drawMode;
	
	public Quaternion modelMatrixQ;
	
	ShaderProgram sp;
	
	
	static final int COORDS_PER_VERTEX = 3;
	
//	protected final String vertexShaderCode =
//		    "attribute vec3 vPosition;" +
//		    "attribute vec3 vNormal;" +
//		    "uniform mat4 mMVPMatrix;" +
//		    "varying vec3 normal;" +
//		    "void main() {" +
//		    "  gl_Position = mMVPMatrix * vec4(vPosition, 1.0);" +
//		    "  normal = abs(normalize(vNormal));" +
//		    "}";
//
//	protected final String fragmentShaderCode =
//	    "precision mediump float;" +
//	    "uniform vec4 vColor;" +
//	    "varying vec3 normal;" +
//	    "void main() {" +
//	    "  gl_FragColor = vec4(normal, 1.0);" +
//	    "}";
	
	float colors[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
	
	float lightPosVec[] = {0.0f, 1.0f, 3.0f, 1.0f};
	
	public Mesh(){ 
		mSphere = new BoundingSphere();
	}
	
	public Mesh(ShaderProgram sp, Resources res, int vertArray, int normArray){
		this(sp, res, vertArray, -1, normArray);
	}
	
	public Mesh(int programHandle){
		mProgram = programHandle;
//		meshVerts = obj.getVertArray();
//		meshNormals = obj.getNormArray();
//		meshTex = obj.getTexArray();
		
		mSphere = new BoundingSphere();
		initBuffers();
	}
	
	public Mesh(ShaderProgram sp, Resources res, int vertArray, int texArray, int normArray){
 		mProgram = sp.getHandle();
		this.res = res;
		
		mSphere = new BoundingSphere();
		initBuffers();
	}
	
	public void initBuffers(){
		if (!buffersInit) {
			// initialize vertex byte buffer
			// each float requires 4 bytes, so NumberOfCoords * 4
			ByteBuffer bb = ByteBuffer.allocateDirect(meshVerts.length * 4);

			// use device's native byte order
			bb.order(ByteOrder.nativeOrder());

			// create/initialize floating point buffer from ByteBuffer
			meshVertBuffer = bb.asFloatBuffer();

			// add coords to buffer
			meshVertBuffer.put(meshVerts);

			// set buffer to read the first coordinate
			meshVertBuffer.position(0);

			if (meshNormals != null && meshNormals.length > 0) {

				meshNormBuffer = ByteBuffer.allocateDirect(meshNormals.length * 4)
						   				   .order(ByteOrder.nativeOrder())
						   				   .asFloatBuffer();
				meshNormBuffer.put(meshNormals).position(0);

				enableLighting = 1.0f;
			}
			else {
				meshNormBuffer = ByteBuffer.allocateDirect(3*4)
										   .order(ByteOrder.nativeOrder())
										   .asFloatBuffer();
				meshNormBuffer.put(new float[]{0, 0, 0}).position(0);
				enableLighting = 0.0f;
			}
			
			if(meshTex != null && meshTex.length > 0){
				meshTexBuffer = ByteBuffer.allocateDirect(meshTex.length * 4)
		   				   .order(ByteOrder.nativeOrder())
		   				   .asFloatBuffer();
				meshTexBuffer.put(meshTex).position(0);
				enableTextureHandle = 1.0f;
			}
			else {
				meshTexBuffer = ByteBuffer.allocateDirect(2 * 4)
		   				   .order(ByteOrder.nativeOrder())
		   				   .asFloatBuffer();
				meshTexBuffer.put(new float[]{1, 1}).position(0);
				enableTextureHandle = 0.0f;
			}
			
		}
//		Matrix.setIdentityM(modelMatrix, 0);
//		modelMatrixQ.buildFromMatrix(modelMatrix);
	}
	
	public int loadTextures(final Context context) {
		final int[] textureHandle = new int[1];
		
		GLES20.glGenTextures(1, textureHandle, 0);

		if(textureHandle[0] != 0){
			
			//By now, if mesh textures not loaded yet or they don't exist, load default white
			if (material == null) {
				bmpOptions = new BitmapFactory.Options();
				bmpOptions.inScaled = false;

				material = BitmapFactory.decodeResource(
						context.getResources(), R.drawable.default_texture, bmpOptions);
			}
			
			//bind 2D texture to OpenGL
			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
			
			//set filtering
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
			GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
			
			//load texture data
			if(texturesLoaded){
				material.recycle();
				if(texturePath.equals("textures_disabled"))
					material = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_texture, bmpOptions);
				else
					material = BitmapFactory.decodeFile(texturePath, bmpOptions);
			}
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, material, 0);
			
			
			//recycle bitmap since its already been loaded into OpenGL
//			material.recycle();
		}
		else {
			throw new RuntimeException("Textures not initialized!");
		}
		
		texturesLoaded = true;
		return (mTextureDataHandle = textureHandle[0]);
	}
	
	/***************************************************************
	 * 
	 * 				DRAW METHOD
	 * 
	 * *************************************************************/
	/**
	 * Draws this mesh. <br><br>
	 * 
	 * Since draw() is called every frame, calculations for matrices are done 
	 * beforehand to save computation time
	 * @param mMVPMatrix - the Model View Projection matrix passed from the renderer
	 * @param mModelViewMatrix - Model View matrix passed from renderer
	 * @param mViewMatrix -  View matrix; used to calculate eye space coordinates
	 * 							 
	 */
	public void draw(float[] mMVPMatrix, float[] mModelViewMatrix, float[] mModelMatrix, 
			float[] mViewMatrix, RenderConfig configs){
		
		boolean enableWireframe = configs.isWireframeEnabled();
		float brightness[] = new float[]{configs.getBrightness(),
										 configs.getBrightness(),
										 configs.getBrightness()};
		if(meshNormals != null && meshNormals.length > 0)
			enableLighting = (configs.isLightingEnabled()) ? 1.0f : 0.0f;
		
		GLES20.glUseProgram(mProgram);
		
		//get handle to vertex shader's vPosition
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
//		mPositionHandle = 0;
//		GLES20.glBindAttribLocation(mProgram, mPositionHandle, "vPosition");
//		int check = GLES20.glGetAttribLocation(mProgram, "vPosition");
		
		//enable vertex attrib array
		GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		//load up coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, 
				GLES20.GL_FLOAT, false, 0, meshVertBuffer);
		
		//Normals
		mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
		GLES20.glEnableVertexAttribArray(mNormalHandle);
		GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, 0, meshNormBuffer);
		
		
		//Textures
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(mProgram, "texturesEnabled"), enableTextureHandle);
		
		mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "vTexture");
		GLES20.glEnableVertexAttribArray(mTexCoordHandle);
		GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, meshTexBuffer);
		
		mTextureSampler = GLES20.glGetUniformLocation(mProgram, "textures");
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);			//set texture 0 as active
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
		GLES20.glUniform1i(mTextureSampler, 0); 			//tell sampler to use texture 0
		
		
		/**************************************************
		 *****************   UNIFORMS   *******************
		 **************************************************/
		
		//uniform for enabling lighting
		GLES20.glUniform1f(
				GLES20.glGetUniformLocation(mProgram, "lightingEnabled"), enableLighting);
		
		
		//get handle for and load MVP matrix
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "mMVPMatrix");
		
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
		
		//load MV matrix
		mModelViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "modelViewMatrix");
		GLES20.glUniformMatrix4fv(mModelViewMatrixHandle, 1, false, mModelViewMatrix, 0);
		
		//load Model Matrix
		GLES20.glUniformMatrix4fv(
				GLES20.glGetUniformLocation(mProgram, "mModelMatrix"), 1, false, mModelMatrix, 0);
		
		//load View Matrix
		mViewMatrixHandle = GLES20.glGetUniformLocation(mProgram, "mViewMatrix");
		GLES20.glUniformMatrix4fv(mViewMatrixHandle, 1, false, mViewMatrix, 0);
		
//		//load normal Matrix
//		GLES20.glUniformMatrix4fv(
//				GLES20.glGetUniformLocation(mProgram, "normalMatrix"), 1, false, mNormalMatrix, 0);
		
		GLES20.glUniform4f(GLES20.glGetUniformLocation(mProgram, "lightPosVec"), 0.0f, 1.0f, 3.0f, 0.0f);
		
		GLES20.glUniform3fv(GLES20.glGetUniformLocation(mProgram, "vBrightness"), 1, brightness, 0);
		
		//draw!
		GLES20.glDrawArrays(
				(enableWireframe) ? GLES20.GL_LINE_STRIP : GLES20.GL_TRIANGLES, 
				0, meshVerts.length/COORDS_PER_VERTEX);
		
		//disable vertex attrib array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		if (meshNormals != null && meshNormals.length > 0)
			GLES20.glDisableVertexAttribArray(mNormalHandle);
	}
	
	
	/********************************
	 * 
	 * 			Get / Set methods
	 * 
	 * ******************************/
	/**
	 * Sets the shader program to render this mesh
	 * @param programHandle
	 */
	public static void setShaderProgram(int programHandle){mProgram = programHandle;}
	
	public void setBoundingSphere(float[] center, float radius){
		mSphere.setBoundingSphere(center, radius);
	}
	
	public float[] getCenterPoint(){
		return mSphere.getCenter();
	}
	
	public void setMaterial(String pathName){
		texturePath = pathName;
		if(bmpOptions == null){
			bmpOptions = new BitmapFactory.Options();
			bmpOptions.inScaled = false;
		}
		if(pathName.equals("textures_disabled")){
			material = null;
			return;
		}
		material = BitmapFactory.decodeFile(pathName, bmpOptions);
	}
	
	public void setMaterial(File filePath){
		if(filePath.isFile()){
			setMaterial(filePath.getPath());
		}
		else{
			throw new RuntimeException("Mesh.setMaterial: can't open file! Must be a file, not a directory");
		}
	}
	
	public void setDrawMode(int numPointsPerFace){
		this.drawMode = (numPointsPerFace == 3) ? DrawType.TRIANGLES : DrawType.QUADS;
	}
	
	public void enableTextures(){
		hasTextures = true;
	}
	
	public float getBoundingRadius(){ return mSphere.getRadius(); }
	
	public boolean hasTextures(){ return hasTextures; }
	public boolean texturesLoaded(){ return texturesLoaded; }
	
	/*********************************
	 * 			Useful Classes
	 * *******************************/
	public class BoundingSphere {
		private float[] mCenter = null;
		private float mRadius;
		
		public BoundingSphere(){
			mCenter = new float[3];
		}
		
		public BoundingSphere(float[] center, float radius){
			if(mCenter == null)
				mCenter = new float[3];
			setBoundingSphere(center, radius);
		}
		
		public void setBoundingSphere(float[] center, float radius){
			mCenter = center;
			mRadius = radius;
		}
		
		public float[] getCenter(){return mCenter;}
		public float getRadius(){return mRadius;}
	}
	
}
