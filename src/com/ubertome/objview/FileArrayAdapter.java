package com.ubertome.objview;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class FileArrayAdapter extends ArrayAdapter<String> {
	
	private static boolean itemSelected = false;
	
	Context context;
	List<String> fileObjects;
	Resources res;
	boolean selectingTextures;
	
	public FileArrayAdapter (Context context, List<String> objects, boolean selectingTextures) {
		super(context, R.layout.row, objects);
		this.context = context;
		this.fileObjects = objects;
		this.selectingTextures = selectingTextures;
		res = this.context.getResources();
	}
	
	/**
	 * This method is called for each row in the ArrayAdapter to display the view for 
	 * that row.
	 * @param position - index position of the current row
	 * @param convertView - View of the current row
	 * @param parent - the parent ViewGroup of this row
	 * @return the View for the current row
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
		String rowName = fileObjects.get(position);
		TextView row;
		
		if(selectingTextures)
			row = (TextView)layoutInflater.inflate(R.layout.row_alt, parent, false);
		else
			row = (TextView)layoutInflater.inflate(R.layout.row, parent, false);
		
		if(rowName.equals("Don't use a Texture"))
			row = (TextView)layoutInflater.inflate(R.layout.row_white, parent, false); 
		
		//if this row represents a folder, set view accordingly
		if(rowName.endsWith("/")){
			row.setText(rowName.substring(0, rowName.length()-1));
			if(selectingTextures)
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_red_icon,0,0,0);
			else
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_blue_icon,0,0,0);
		}
		
		//this row represents a file
		else {
			row.setText(rowName);
			if(rowName.endsWith("png") || rowName.endsWith("PNG")
					|| rowName.endsWith("jpg") || rowName.endsWith("JPG")
					|| rowName.endsWith("tga") || rowName.endsWith("TGA")){
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.image_file_icon, 0, 0, 0);
			}
			else if(rowName.endsWith("obj") || rowName.endsWith("OBJ")){
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mesh_icon_light, 0, 0, 0);
			}
			else if(rowName.equals("...")){
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_directory_up, 0, 0, 0);
			}
			else if(rowName.equals("Go To Root")){
				row.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tilde, 0, 0, 0);
			}
		}
		return row;
	}
	
//	public void setItemSelected(int position){
//		fileObjects.get(position);
//	}

}
