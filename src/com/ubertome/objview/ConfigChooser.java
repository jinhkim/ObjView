package com.ubertome.objview;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

import android.opengl.GLSurfaceView;

public class ConfigChooser implements GLSurfaceView.EGLConfigChooser {

	@Override
	public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
		
		
		int[] num_configs = new int[1];
		egl.eglGetConfigs(display, null, 0, num_configs);
		int configurations = num_configs[0];
		
		return null;
	}

}
