package com.ubertome.objview;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.preference.PreferenceManager;
import android.renderscript.Matrix3f;

import com.ubertome.objview.RenderFragment.RenderConfig;
import com.ubertome.objview.GLUtils;

public class RendererGL implements GLSurfaceView.Renderer{
	
	public static final float PI = 3.141592f;
	public static final float RAD2DEG = (180f/PI);
	public static final float DEG2RAD = (PI/180f);
	
	//because the renderer is running on a separate thread, 
	//must declare 'volatile' 
	public volatile float xAngle = 0.f, yAngle = 0.f, zAngle = 0.f, 
						  lightX, lightY = 4.0f,
						  xAccumAngle = 0, yAccumAngle = 0,
						  xLastAngle = 0, yLastAngle = 0;
	/**
	 * angle (radians) to rotate model in FREE_ROTATION mode
	 */
	public volatile float rotationAngle = 0f;
	public volatile float pinchScaleFactor = 1.0f, pinchLightDist = 1.5f,
						  pinchAccumScale = 0, pinchLastScale = 0;
	public volatile float translationX = 0, translationY = 0;
	public volatile boolean enableLighting = true;
	/**
	 * axis to rotate model about in FREE_ROTATION mode
	 */
	public volatile Vector3 rotationAxis;
	public boolean modelLoaded = false;
	
	private int mProgramHandle = -1;
	private boolean lockToX = true, lockToY = true, lockToZ = true;
	
	static float red = 0.0f, green = 0.0f, blue = 0.0f, alpha = 1.0f;
	static float count = 1.0f;
	RenderConfig renderConfig;
	Mesh[] models;
	Context context;
	Resources res;
	AssetManager asm;
	ParsedObj obj = null;
	GShader vert, frag;
	ShaderProgram sp;
	Quaternion modelMatrixQ, rotMatrixQ, freeRotQ, yAxisRotQ;
	Quaternion rotX, rotY, rotZ;
	
	Bitmap textBitmap;
	Canvas canvas;
	Paint textPaint;
	
	float[] vec3, camOrigin, xAxis, yAxis, zAxis,
			forwardVec, rightVec, upVec;
	int[] textures;
	float ratio;
	public float fovy = 25;
	
	float mModelMatrix[] = new float[16], mViewMatrix[] = new float[16], 
			mProjMatrix[] = new float[16], mMVPMatrix[] = new float[16], 
			mMVMatrix[] = new float[16], mNormalMatrix[] = new float[16],
			worldToEyeCamOrigin[] = new float[16], mRotMatrix[] = new float[16],
			mTransMatrix[] = new float[16];
	
	static float rotMat3x3[] = new float[16], camPosFromViewMat[] = new float[4];
	
	public enum RotationAxis {
		X_AXIS,
		Y_AXIS,
		Z_AXIS,
		FREE_ROTATION
	};
	
	RotationAxis rotAxis;	
//	public RendererGL(Resources resource, AssetManager AssetManager){
//		this.res = resource;
//		this.asm = AssetManager;
//		vert = new GShader(asm, "render.vert", GLES20.GL_VERTEX_SHADER);
//		frag = new GShader(asm, "render.frag", GLES20.GL_FRAGMENT_SHADER);
//		sp = new ShaderProgram(vert, frag);
//		mProgramHandle = sp.getHandle();
//	}
	
//	public RendererGL(AssetManager asm, ParsedObj pObj){
//		this.asm = asm;
//		obj = pObj;
//	}
	
	public RendererGL(Context context, RenderConfig configs){
		this.context = context;
		this.asm = context.getAssets();
		renderConfig = configs;
		vert = new GShader(asm, "shaders/render.vert", GLES20.GL_VERTEX_SHADER);
		frag = new GShader(asm, "shaders/render.frag", GLES20.GL_FRAGMENT_SHADER);
		sp = new ShaderProgram(vert, frag);
		Mesh.setShaderProgram(mProgramHandle = sp.getHandle());
		modelMatrixQ = new Quaternion();
		rotMatrixQ = new Quaternion();
		freeRotQ = new Quaternion();
		yAxisRotQ = new Quaternion();
		vec3 = new float[3];
		rotX = new Quaternion();
		rotY = new Quaternion();
		rotZ = new Quaternion();
		xAxis = new float[]{1f, 0f, 0f};
		yAxis = new float[]{0f, 1f, 0f};
		zAxis = new float[]{0f, 0f, 1f};
		camOrigin = new float[]{0f, 0f, 4f, 0f};
		forwardVec = new float[3];
		rightVec = new float[3];
		upVec = new float[3];
		Matrix.setIdentityM(mModelMatrix, 0);
		Matrix.setIdentityM(mRotMatrix, 0);
		Matrix.setIdentityM(mTransMatrix, 0);
		Matrix.setIdentityM(mMVMatrix, 0);
		rotationAxis = new Vector3(0.0f, 1.0f, 0.0f);
		
//		textBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
//		canvas = new Canvas(textBitmap);
//		textBitmap.eraseColor(0);
//		textures = new int[1];
//		textPaint = new Paint();
//		textPaint.setTextSize(32);
//		textPaint.setAntiAlias(true);
//		textPaint.setARGB(255, 255, 255, 255);
//		canvas.drawText("Hello world", 0, 0, textPaint);
//		
//		GLES20.glGenTextures(0, textures, 0);
//		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
//		
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
//		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
//		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, textBitmap, 0);
//		
//		textBitmap.recycle();
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		//sets background frame colour
//		GLES20.glClearColor(0.8f, 0.3f, 0.3f, 1.0f);
		GLES20.glClearColor(red, green, blue, alpha);
		
//		initShaders();
		
		vert = new GShader(asm, "shaders/render.vert", GLES20.GL_VERTEX_SHADER);
		frag = new GShader(asm, "shaders/render.frag", GLES20.GL_FRAGMENT_SHADER);
		sp = new ShaderProgram(vert, frag);
		Mesh.setShaderProgram(mProgramHandle = sp.getHandle());
		
		//initialize textures
		if(modelLoaded){
			for (int j = 0; j < models.length; j++) {
				models[j].loadTextures(context);
			}
		}
		
		renderConfig.updateRotationAxis();
		rotAxis = renderConfig.getRotationAxis();
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthMask(true);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthRangef(0.0f, 1.0f);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPref.getBoolean("pref_culling_key", true))
			GLES20.glEnable(GLES20.GL_CULL_FACE);
		else
			GLES20.glDisable(GLES20.GL_CULL_FACE);
				
	}
	
	@Override
	public void onDrawFrame(GL10 unused){
		//Redraw background colour
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		GLES20.glClearColor(renderConfig.bgBrightness,
				renderConfig.bgBrightness, renderConfig.bgBrightness, 1.0f);
		
		//Culling setting
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPref.getBoolean("pref_culling_key", true))
			GLES20.glEnable(GLES20.GL_CULL_FACE);
		else
			GLES20.glDisable(GLES20.GL_CULL_FACE);
		
		fovy = Float.valueOf(sharedPref.getString("pref_fovy_key", "25"));
		
		//Projection setting
		if (renderConfig.projectionChanged) {
			if (renderConfig.isOrthoView())
				Matrix.orthoM(mProjMatrix, 0, -ratio, ratio, -1f, 1f, 1.0f, 10f);
			else
				Matrix.perspectiveM(mProjMatrix, 0, fovy, ratio, 1.0f, 10.0f);
			renderConfig.projectionChanged = false;
		}
		
		if (modelLoaded) {
			for (int i = 0; i < models.length; i++) {
				float boundingRadius = models[i].getBoundingRadius();
				
				if(!models[i].texturesLoaded())
					models[i].loadTextures(context);
				
				rotAxis = renderConfig.getRotationAxis();
				
				switch(rotAxis){
				
				case Y_AXIS:{
					modelMatrixQ.buildFromEuler(-yAngle, -xAngle, 0);
					modelMatrixQ.normalize();
//					yAxisRotQ.multiplyThisWith(modelMatrixQ);
					mModelMatrix = modelMatrixQ.getMatrix();
					break;
				}
				
				case FREE_ROTATION:{
					
					freeRotQ.buildFromAxisAngle(
							rotationAxis.getDataArray(), rotationAngle);
					freeRotQ.normalize();
					modelMatrixQ = freeRotQ.multiplyThisWith(modelMatrixQ);
					mModelMatrix = modelMatrixQ.getMatrix();

//					Matrix.setIdentityM(mModelMatrix, 0);
//					rotMatrixQ.buildFromEuler(yAngle*0.01f*RAD2DEG, xAngle*0.01f*RAD2DEG, zAngle);
//					rotMatrixQ.normalize();
//					camOrigin[0] = -4*(float)Math.sin(yAngle*0.01f);
//					camOrigin[1] = -4*(float)Math.cos(xAngle*0.01f);
//					camOrigin[2] = -4*(float)Math.cos(xAngle*0.01f)-4*(float)Math.cos(yAngle*0.01f);
//					LookAt(rotMatrixQ, camOrigin[0], camOrigin[1], camOrigin[2]);
					break;
				}
				default:
					break;
				}
				
				Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 4, 0f, 0f, 0f, 0f, 1f, 0f);
				
				//Adjust mesh to the center of the world and scale to unit size
				Matrix.scaleM(mModelMatrix, 0, pinchScaleFactor,
						pinchScaleFactor, pinchScaleFactor);

				Matrix.scaleM(mModelMatrix, 0, 1.0f / boundingRadius,
						1.0f / boundingRadius, 1.0f / boundingRadius);
				
//				Matrix.multiplyMM(mNormalMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
				Matrix.translateM(mModelMatrix, 0, translationX, translationY, 0);
				
				Matrix.translateM(mModelMatrix, 0,
						-models[i].getCenterPoint()[0],
						-models[i].getCenterPoint()[1],
						-models[i].getCenterPoint()[2]);
				
				Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
				Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVMatrix, 0);
				
				
				models[i].draw(mMVPMatrix, mMVMatrix, mModelMatrix, mViewMatrix,
						       renderConfig);
			}
		}

	}
	
	private void LookAt(Quaternion quat, float eyeX, float eyeY, float eyeZ){
		mRotMatrix = quat.getMatrix();
		
		mTransMatrix[12] = -eyeX;
		mTransMatrix[13] = -eyeY;
		mTransMatrix[14] = -eyeZ;
		
		Matrix.multiplyMM(mViewMatrix, 0, mRotMatrix, 0, mTransMatrix, 0);

		//extract cam position from viewMatrix
		//this assumes modelMatrix is identity
		mat4ToMat3(mViewMatrix, rotMat3x3);
		camPosFromViewMat[0] = -mViewMatrix[12];
		camPosFromViewMat[1] = -mViewMatrix[13];
		camPosFromViewMat[2] = -mViewMatrix[14];
		Matrix.multiplyMV(camOrigin, 0, rotMat3x3, 0, camPosFromViewMat, 0);
	}
	
	/**
	 * Takes any number of rotations and forms the view matrix. 
	 * @param translation - 4x4 translation matrix
	 */
	private void LookAt(float eyeX, float eyeY, float eyeZ, 
			float targetX, float targetY, float targetZ, 
			float upX, float upY, float upZ) {
		upVec[0] = upX;
		upVec[1] = upY;
		upVec[2] = upZ;
		
		// forward vector
		forwardVec[0] = targetX - eyeX;
		forwardVec[1] = targetY - eyeX;
		forwardVec[2] = targetZ - eyeZ;
		forwardVec = GLUtils.normalize(forwardVec);

		// compute right vector
		rightVec = GLUtils.crossProduct(forwardVec[0], forwardVec[1], forwardVec[2],
								upVec[0], upVec[1], upVec[2]);
		rightVec = GLUtils.normalize(rightVec);

		// re-compute up vector for consistency
		upVec = GLUtils.crossProduct(rightVec[0], rightVec[1], rightVec[2], 
							forwardVec[0], forwardVec[1], forwardVec[2]);
		upVec = GLUtils.normalize(upVec);
		
		mRotMatrix[0] = rightVec[0];
		mRotMatrix[1] = upVec[0];
		mRotMatrix[2] = -forwardVec[0];
		mRotMatrix[3] = 0f;
		
		mRotMatrix[4] = rightVec[1];
		mRotMatrix[5] = upVec[1];
		mRotMatrix[6] = -forwardVec[1];
		mRotMatrix[7] = 0f;
		
		mRotMatrix[8] = rightVec[2];
		mRotMatrix[9] = upVec[2];
		mRotMatrix[10] = -forwardVec[2];
		mRotMatrix[11] = 0f;
		
		mRotMatrix[12] = 0f;
		mRotMatrix[13] = 0f;
		mRotMatrix[14] = 0f;
		mRotMatrix[15] = 1f;
		
		mTransMatrix[12] = -eyeX;
		mTransMatrix[13] = -eyeY;
		mTransMatrix[14] = -eyeZ;
		
		Matrix.multiplyMM(mViewMatrix, 0, mRotMatrix, 0, mTransMatrix, 0);
	}
	
	/**
	 * Converts a Mat4x4 to a Mat3x3
	 * @param mat4 - Source matrix (unmodified)
	 * @param mat3 - Destination matrix as mat3 or mat4 with an extra row/column
	 */
	private void mat4ToMat3(float[] mat4, float[] mat3){
		try {
			if((mat4.length != 16))
				throw new Exception();
			if((mat3.length != 12) && (mat3.length != 16))
				throw new Exception();
		} catch (Exception e){
			e.printStackTrace();
			android.util.Log.e("RendererGL", "Can't convert mat4 to mat3 because of incorrect dimensions!");
		}
		if (mat3.length == 12) {
			mat3[0] = mat4[0];
			mat3[1] = mat4[1];
			mat3[2] = mat4[2];

			mat3[3] = mat4[4];
			mat3[4] = mat4[5];
			mat3[5] = mat4[6];

			mat3[6] = mat4[8];
			mat3[7] = mat4[9];
			mat3[8] = mat4[10];
		}
		else if(mat3.length == 16){
			mat3[0] = mat4[0];
			mat3[1] = mat4[1];
			mat3[2] = mat4[2];
			mat3[3] = 0f;
			
			mat3[4] = mat4[4];
			mat3[5] = mat4[5];
			mat3[6] = mat4[6];
			mat3[7] = 0f;
			
			mat3[8] = mat4[8];
			mat3[9] = mat4[9];
			mat3[10] = mat4[10];
			mat3[11] = 0f;
			
			mat3[12] = 0f;
			mat3[13] = 0f;
			mat3[14] = 0f;
			mat3[15] = 1f;
		}
	}
	
	private void updateModelMatrix(float angleX, float angleY){
//		accumAngleY += angleY - 
	}
	
//	private void updateModelMatrix(){
//		xAccumAngle += xAngle - xLastAngle;
//		Matrix.rotateM(mModelMatrix, 0, xAccumAngle, 1, 0, 0);
//		xLastAngle = xAngle;
//		
//		yAccumAngle += yAngle - yLastAngle;
//		Matrix.rotateM(mModelMatrix, 0, yAccumAngle, 0, 1, 0);
//		yLastAngle = yAngle;
//		
//		pinchAccumScale = (pinchLastScale != 0) ? (pinchScaleFactor / pinchLastScale) : 1.0f;
//		Matrix.scaleM(mModelMatrix, 0, pinchAccumScale,
//				pinchAccumScale, pinchAccumScale);
//		pinchLastScale = pinchScaleFactor;
//	}
	
	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height){
		GLES20.glViewport(0, 0, width, height);
		
		ratio = (float) width / height;
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if(sharedPref.getBoolean("pref_culling_key", true))
			GLES20.glEnable(GLES20.GL_CULL_FACE);
		else
			GLES20.glDisable(GLES20.GL_CULL_FACE);
		fovy = Float.valueOf(sharedPref.getString("pref_fovy_key", "25"));
		
		if(renderConfig.isOrthoView())
			Matrix.orthoM(mProjMatrix, 0, -ratio, ratio, -1f, 1f, 1.0f, 10f);
		else
			Matrix.perspectiveM(mProjMatrix, 0, fovy, ratio, 1.0f, 10.0f);
		
//		renderConfig.updateRotationAxis();
//		rotAxis = renderConfig.getRotationAxis();
	}
	

	public void loadModel(ParsedObj obj){
		this.obj = obj;
		try {
			// check that renderer has been initialized
			if (mProgramHandle != -1) {
				models = obj.getMeshes();
				for(int f = 0; f < models.length; f++){
					models[f].initBuffers();
//					models[f].loadTextures(context);
				}
				modelLoaded = true;
			} else {
				throw new ShaderErrorException(
						"Shaders have not been properly initialized! \r\nSource: RendererGL.loadModel()");
			}
		} catch(ShaderErrorException se){
			se.printStackTrace();
			Log.writeLog("Shaders have not been properly initialized! \r\nSource: RendererGL.loadModel()");
		}
	}
	
}
