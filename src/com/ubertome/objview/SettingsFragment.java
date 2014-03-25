package com.ubertome.objview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ubertome.objview.RenderFragment.RenderConfig;

public class SettingsFragment extends PreferenceFragment implements RenderFragment.OnSettingsLaunched {
	
	ClearList mCallback;
	RenderConfig renderConfig;
	
	SharedPreferences.OnSharedPreferenceChangeListener prefChangedListener;
	SharedPreferences sharedPref;
	
	public interface ClearList{
		public void clearRecentFiles();
	}
	
	public SettingsFragment(){
		
	}
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try{
		mCallback = (ClearList) activity;
		} catch (ClassCastException ce){
			throw new ClassCastException(activity.toString()
					+" must implement ClearList");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		prefChangedListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(
					SharedPreferences sharedPreferences, String key) {
				if(key.equals("pref_toggle_rotation")){
					renderConfig.updateRotationAxis();
				}
			}};
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPref.registerOnSharedPreferenceChangeListener(prefChangedListener);
		
		//get bundle args with RenderConfig
		addPreferencesFromResource(R.xml.settings_preference);
		EditTextPreference fovyPref = (EditTextPreference) findPreference("pref_fovy_key");
		fovyPref.getEditText().setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
		
		Preference clearRecentPref = findPreference("pref_clear_recent_key");
		clearRecentPref
		.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				
				new AlertDialog.Builder(getActivity()).setIcon(R.drawable.alert_small)
				.setMessage("Clear recent files?")
				.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SharedPreferences.Editor editor = getActivity().getSharedPreferences(
						"com.ubertome.modelviewer.recent_files", Context.MODE_PRIVATE).edit();
						editor.clear().commit();
						mCallback.clearRecentFiles();
						Toast.makeText(getActivity(), "Recent items cleared!", Toast.LENGTH_SHORT).show();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}
				}).show();

				return true;
			}
		});
		
		LinearLayout layout = new LinearLayout(getActivity());
		LinearLayout.LayoutParams params;
		layout.setOrientation(LinearLayout.VERTICAL);
		
		SeekBar seekBar = new SeekBar(getActivity());
		seekBar.setMax(100);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {	}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				renderConfig.bgBrightness = ((float)progress)/100.0f;
				renderConfig.refreshRender();
			}
		});
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_HORIZONTAL;
		params.setMargins(10, 10, 10, 10);

		layout.addView(seekBar, params);
		final LinearLayout finalLayout = layout;
		Preference bgColorPref = findPreference("pref_bg_color_key");		
		bgColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			AlertDialog slider;
			boolean sliderCreated = false;
			@Override
			public boolean onPreferenceClick(Preference preference) {
//				XmlPullParser xml = getResources().getXml(R.xml.settings_preference);
//				new SeekBarDialog(getActivity(), Xml.asAttributeSet(xml), renderConfig);
				
				if(sliderCreated){
					slider.show();
				}
				else {
					sliderCreated = true;
					slider = new AlertDialog.Builder(getActivity()).setView(finalLayout)
							.setTitle("Background Brightness").create();
					slider.setCanceledOnTouchOutside(false);
					slider.show();
				}
				
				return true;
			}
		});
		
	}
	
	

	@Override
	public View launchSettings(){
		return getView();
	}
	
	public void setRenderConfig(RenderConfig config){
		renderConfig = config;
	}

	
}
