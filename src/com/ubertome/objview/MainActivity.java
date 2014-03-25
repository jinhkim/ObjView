package com.ubertome.objview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//import com.ubertome.modelviewer.RenderFragment.RedrawFragment;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends Activity implements FileExplorerFragment.FragToActivity, SettingsFragment.ClearList,
													  RenderFragment.OnActionItemsReceived {

	final File root = Environment.getExternalStorageDirectory();
	
	private ParsedObj parsedModel, bikeModel;
	private RendererGL mRenderer;
	private float xOld = 0.0f, yOld = 0.0f;
	private static float TOUCH_SCALE_FACTOR = 180.0f / 320.0f;
	private boolean toggleMoveLight = false;
	static volatile private boolean atSplashScreen = true;
	private Map<String, ?> recentList = null;
	private Bundle containsObjMap, containsTexMap;
	private static final int recentListLimit = 9;
	private int recursion_level = 0;
	
	private float screenDensity = 0.0f;
	private SparseArray<Drawable> actionBarList;

	SettingsFragment settingsFragment;
	AboutFragment aboutFrag;
	RenderFragment renderFrag;
	
	public enum ActionIcons {
			BALL_WIREFRAME(R.drawable.ball_wireframe_icon), 
			BALL(R.drawable.ball_icon), 
			BRIGHTNESS_SCALE(R.drawable.brightness_scale_icon),
			BRIGHTNESS_LOW(R.drawable.white_brightness_low),
			BRIGHTNESS_HIGH(R.drawable.white_brightness_high),
			PERSPECTIVE(R.drawable.perspective_view_icon_thick),
			ORTHO(R.drawable.ortho_view_icon_thick),
			FOLDER(R.drawable.folder_icon_custom_dark),
			OVERFLOW(R.drawable.menu_icon),
			RECENT_FILES(R.drawable.timer_icon),
			STATS(R.drawable.stats_icon),
			SETTINGS(R.drawable.settings_icon),
			ABOUT(R.drawable.question_icon);

			private int value;
			
			ActionIcons(int val){
				this.value = val;
			}
			
			public int getValue(){
				return value;
			}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		Resources res = getResources();
		renderFrag = new RenderFragment();

		// hide UI components for now
		getOverflowMenu();
		getActionBar().hide();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		//get and set density of screen to properly scale icons
		screenDensity = metrics.density;
//		actionBarList = new SparseArray<Drawable>();
//
//		for(ActionIcons abi : ActionIcons.values()){
//			actionBarList.put(abi.getValue(), 
//							resizeForActionBar(res.getDrawable(abi.getValue()), screenDensity)
//							);
//		}
		
		//scale splashscreen
		int winWidth = metrics.widthPixels;
		int winHeight = metrics.heightPixels;
		
		BitmapDrawable bitmap = (BitmapDrawable) res.getDrawable(R.drawable.splashscreen_alt);
		float bitmapWidth = bitmap.getBitmap().getWidth();
		float bitmapHeight = bitmap.getBitmap().getHeight();
		
		float ratio = winWidth / bitmapWidth;
		int newBitmapWidth = (int) (bitmapWidth * ratio);
		int newBitmapHeight = (int) (bitmapHeight * ratio);
		
		FrameLayout frameLayout = new FrameLayout(this);
		ImageView img = new ImageView(this);
		img.setImageResource(R.drawable.splashscreen_alt);
		
		
		frameLayout.addView(img, new FrameLayout.LayoutParams(newBitmapWidth, newBitmapHeight));
		
		setContentView(frameLayout);
//		enableFullscreen();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (atSplashScreen) {
					atSplashScreen = false;
					
					//copy model files into app folder
					File appDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getPackageName() + "/files/models");
					if(!appDirectory.mkdirs()){
						if (!appDirectory.isDirectory()) {
							try {
								throw new Exception(
										"Failed to create directory!");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					String appPath = appDirectory.getPath(); 
					copyAssetsToSD(appPath + "/");
					
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("default_directory", appPath);
					editor.commit();
					
					setContentView(R.layout.fragment_main);
					FragmentManager fm = getFragmentManager(); 
					FragmentTransaction ft = fm.beginTransaction();
					if(!renderFrag.isAdded()){
						ft.add(R.id.main_frag, renderFrag, "render_tag");
					}
					ft.show(renderFrag);
					ft.commit();
					getActionBar().show();
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			}
		}, 1000);
	}
	
	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	public void onStop(){
		super.onStop();
		atSplashScreen = true;
//		Log.closeLog();
	}
	
	/**
	 * This method is called from FileExplorerFragment to be run in this Activity
	 * @param bundle
	 */
	@Override
	public void sendToActivity(Bundle bundle){
		RenderFragment frag = (RenderFragment) getFragmentManager().findFragmentByTag("render_tag");
		frag.sendToRender(bundle);
	} 
	
	/**
	 * This method is called from SettingsFragment, and calls a method in FileExplorerFragment
	 */
	@Override
	public void clearRecentFiles() {
		FileExplorerFragment explorer = (FileExplorerFragment) getFragmentManager().findFragmentByTag("explorer_tag");
		explorer.clearFileList();
	}
	
	@Override
	public SparseArray<Drawable> getActionItemArray(){
		return actionBarList;
	}
	
	private void copyAssetsToSD(String copyToPath){
		AssetManager assets = getAssets();
		String[] files = null;
		try{
			files = assets.list("models");
		} catch(IOException io){
			io.printStackTrace();
		}
		
		for(int i = 0; i < files.length; i++){
						
			if(files[i].endsWith(".obj") || files[i].endsWith(".OBJ")){
				InputStream input = null;
				OutputStream output = null;
				
				try {
					input = assets.open("models/" + files[i]);
					output = new FileOutputStream(copyToPath + files[i]);
					copyFile(input, output);
					input.close();
					input = null;
					output.flush();
					output.close();
					output = null;
				} catch (IOException ee){
					ee.printStackTrace();
				}
			}
		}
	}
	
	private void copyFile(InputStream input, OutputStream output) throws IOException{
		byte[] buffer = new byte[1024];
		int read;
		while((read = input.read(buffer)) != -1){
			output.write(buffer, 0, read);
		}
	}
	
	/*force device to show overflow menu on action bar, regardless
	 * of whether the device has a menu key or not*/
	private void getOverflowMenu(){
		try {
			ViewConfiguration viewConfig = ViewConfiguration.get(this);
			Field menuIcon = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuIcon != null){
				menuIcon.setAccessible(true);
				menuIcon.setBoolean(viewConfig, false);
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException ie){
			ie.printStackTrace();
		}
		
	}
	
	public void enableFullscreen() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	}

	public void disableFullscreen() {
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
	
	/**
	 * Resizes a drawable to fit in the Action Bar.
	 * @param image Drawable to be resized
	 * @param densityScale density scale of screen, where a 160dpi screen has a density scale of 1.0
	 * @return resized Drawable
	 */
	private Drawable resizeForActionBar(Drawable image, float densityScale){
		Bitmap b = ((BitmapDrawable)image).getBitmap();
		
		/*Scales drawable to 32dp by 32dp. Actual size in pixels depends on density of the screen,
		 * hence densityScale*/
		Bitmap resizedBmp = Bitmap.createScaledBitmap(b, (int)(32*densityScale), (int)(32*densityScale), false); 
		return new BitmapDrawable(getResources(), resizedBmp);
	}
	
/*	public class ActionBarItems {
		

		private Drawable[] drawableList;
		private int resValue;
		
		public ActionBarItems() {	}
		
		public int getValue(int value){
			return this.resValue;
		}
//		
//		public int getSize(){
//			return ActionBarItems.values().length;
//		}
		
		public void set(int index, int value){
			this.resValue = value;
		}
		
	};*/
	
/*	private boolean containsObjFiles(File file) throws NullPointerException {
		recursion_level++;
		if (file.isDirectory()) {
			File[] f = file.listFiles();
			for (int i = 0; i < f.length; i++)
				if (containsObjFiles(f[i]))
					containsObjMap.putBoolean(f[i].getPath(), true);
		} else {
			if (file.getName().endsWith(".obj")
					|| file.getName().endsWith(".OBJ")){
				recursion_level--;
				return true;
			}
			else {
				recursion_level--;
				return false;
			}
			
		}
		if(containsObjMap.isEmpty()){
			recursion_level--;
			return false;
		}
		else {
			recursion_level--;
			return true;
		}
	}*/

	
//	
//	private boolean containsTexFiles(File file) {
//		if (file.isDirectory()) {
//			File[] f = file.listFiles();
//			for (int i = 0; i < f.length; i++)
//				if (containsTexFiles(f[i]))
//					containsTexMap.putBoolean(f[i].getPath(), true);
//		} else {
//			if (file.getName().endsWith(".jpg")
//					|| file.getName().endsWith(".JPG")
//					|| file.getName().endsWith(".png")
//					|| file.getName().endsWith(".PNG")
//					|| file.getName().endsWith(".tga")
//					|| file.getName().endsWith(".TGA"))
//				return true;
//			else
//				return false;
//		}
//		if(containsTexMap.isEmpty())
//			return false;
//		else
//			return true;
//	}
	
}
