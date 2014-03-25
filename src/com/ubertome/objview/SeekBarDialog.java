package com.ubertome.objview;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ubertome.objview.RenderFragment.RenderConfig;

public class SeekBarDialog extends DialogPreference implements SeekBar.OnSeekBarChangeListener{
	
	SeekBar seekBar;
	Context mContext;
	RenderConfig renderConfig;

	public SeekBarDialog(Context context, AttributeSet attrs, RenderConfig config) {
		super(context, attrs);
		mContext = context;
		renderConfig = config;
	}
	
	@Override
	protected View onCreateDialogView(){
		LinearLayout layout = new LinearLayout(mContext);
		layout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		
		TextView title = new TextView(mContext);
		title.setText("Background brightness");
		
		layout.addView(title, params);
		
		seekBar = new SeekBar(mContext);
		seekBar.setMax(100);
		
		layout.addView(seekBar, params);
		
		
		return layout;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {	
		renderConfig.bgBrightness = ((float)progress)/100.0f;
		renderConfig.refreshRender();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {}
	
	@Override
	public void showDialog(Bundle state){
		super.showDialog(state);
	}

}
