package com.ubertome.objview;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;

public class NumPickerPreference extends DialogPreference {

	private Context mContext;
	private NumberPicker numPicker;
	private float initValue = 25f, value;
	
	public NumPickerPreference(Context context, AttributeSet attr) {
		super(context, attr);
		mContext = context;
	}

	@Override
	protected void onBindDialogView(View view){
		Activity activity = (Activity) mContext;
		LayoutInflater inflater = activity.getLayoutInflater();
		
		numPicker = (NumberPicker)view.findViewById(R.id.num_picker);
//		numPicker.
		
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		if(positiveResult){
			
		}
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which){
		super.onClick(dialog, which);
	}
	
	@Override
	protected void onSetInitialValue(boolean restorePersistedState, Object defaultValue){
		if(restorePersistedState){
			SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
			value = sharedPref.getFloat("pref_fov_key", initValue);
		}
		else {
			value = (Float) defaultValue;
		}
	}
}
