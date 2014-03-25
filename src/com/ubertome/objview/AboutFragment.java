package com.ubertome.objview;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

public class AboutFragment extends DialogFragment {
	
	static AboutFragment instance = null;
	static int dialog_width, dialog_height;
	Point windowSize;

	public AboutFragment getInstance(){
		if(instance == null)
			return new AboutFragment();
		else
			return this;
	}
	
	public AboutFragment(){
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	//for a DialogFragment, don't bother overriding onCreateView,
	//onCreateDialog should take care of everything
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_about_page, null);
		
		builder.setView(view);
		return builder.create();
	}
	
//	@Override
//	public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState){
////		LinearLayout ll = (LinearLayout) getActivity().findViewById(R.id.command_menu_viewgroup);
//		
//		Window window = getDialog().getWindow();
//		window.getWindowManager().getDefaultDisplay().getSize(windowSize);
//		dialog_width = (int) (windowSize.x * 0.8f);
//		dialog_height = (int) (windowSize.y * 0.9f);
//		window.setLayout(dialog_width, dialog_height);
//		
//		//container should only be used for getting LayoutParams, 
//		//don't pass it in the inflater
//		
//		return layoutInflater.inflate(R.layout.fragment_about_page, null); 
//	}
	

	@Override
	public void onConfigurationChanged(Configuration config){
		super.onConfigurationChanged(config);
		
//		getDialog().getWindow().getWindowManager().getDefaultDisplay().getSize(windowSize);
//		dialog_width = (int) (windowSize.x * 0.8f);
//		dialog_height = (int) (windowSize.y * 0.9f);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		inflater.inflate(R.menu.about_page, menu);
		
//		return true;
	}

}
