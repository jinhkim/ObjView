package com.ubertome.objview;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileExplorerFragment extends ListFragment implements
		RenderFragment.OnExplorerLaunched {

	private ArrayAdapter<String> fileArray;
	private List<String> path = null;
	private List<String> itemPath = null, itemNames = null;
	/**
	 * This list holds file type corresponding to each item in <code>itemPath</code>
	 */
	private List<FileType> fileTypeList;
	private String root = "/", currentPath, parentPath, workingDir;
	private TextView myPath, listTitle, fileBrowserTitle;
	private String modelFile, modelFileName, textureFile;
	private boolean selectingTextures = false, goToRecentList = false,
			enableOBJFileScan = false, enableTexFileScan = false;

	private ListView listView = null;
	private int listPosClicked = 0;

	public static String fileToParse = "com.ubertome.modelviewer.FileExplorer.file";
	public static String texToParse = "com.ubertome.modelviewer.FileExplorer.texToParse";
	public static String modelPathName = "com.ubertome.modelviewer.FileExplorer.modelPathName";

	private String[] recentFilesList = null;
	private int recentFilesLimit = 10;
	private Bundle containsObjMap, containsTexMap;

	FragToActivity mCallback;
	View currentView;
	
	enum FileType {
		OBJ,
		IMG,
		OTHER,
		FOLDER
	};
	
	FileType fileType;

	public interface FragToActivity {
		public void sendToActivity(Bundle bundle);
	}

	public FileExplorerFragment() {

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mCallback = (FragToActivity) activity;
		} catch (ClassCastException ce) {
			throw new ClassCastException(activity.toString()
					+ " must implement FragToActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// setContentView(R.layout.activity_file_explorer);

		// recentFilesList =
		// savedInstanceState.getStringArray(MenuThing.recentObj);
		// recentFilesLimit =
		// savedInstanceState.getInt(MenuThing.recentFilesLimit, 10);
		// containsObjMap = savedInstanceState.getBundle(MenuThing.objFilesMap);
		// containsTexMap = savedInstanceState.getBundle(MenuThing.texFilesMap);

		// goToRecentList = savedInstanceState.getBoolean(MenuThing.goToRecent,
		// false);
		setHasOptionsMenu(false);
	}

	@Override
	public View onCreateView(LayoutInflater layoutInflater,
			ViewGroup container, Bundle savedInstanceState) {
		currentView = layoutInflater.inflate(R.layout.fragment_file_explorer, null);

		// listTitle = (TextView) findViewById(R.id.list_title_id);
		fileBrowserTitle = (TextView) currentView
				.findViewById(R.id.file_explorer_title);
		myPath = (TextView) currentView.findViewById(R.id.path_id);

		root = Environment.getExternalStorageDirectory().getPath() + '/';
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		currentPath = sharedPref.getString("default_directory", root) + '/';
	

		if (goToRecentList) {
			getRecentDirectory(recentFilesList, recentFilesLimit);
			currentPath = root;
		} else {
			getDirectory(currentPath);
		}

		return currentView;
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);

		// listTitle = (TextView) findViewById(R.id.list_title_id);
		fileBrowserTitle = (TextView) getView().findViewById(
				R.id.file_explorer_title);
		myPath = (TextView) getView().findViewById(R.id.path_id);

		root = Environment.getExternalStorageDirectory().getPath() + '/';

		if (goToRecentList) {
			getRecentDirectory(recentFilesList, recentFilesLimit);
		} else
			getDirectory(currentPath);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (goToRecentList) {
			getRecentDirectory(recentFilesList, recentFilesLimit);
		} else
			getDirectory(currentPath);
	}

	@Override
	public View getExplorerView(boolean goToRecent) {
		selectingTextures = false;
		if (goToRecent) {
			goToRecentList = true;
			SharedPreferences sharedPref = getActivity().getSharedPreferences(
					"com.ubertome.modelviewer.recent_files",
					Context.MODE_PRIVATE);
			Collection<String> list = (Collection<String>) sharedPref.getAll()
					.values();
			recentFilesList = list.toArray(new String[list.size()]);

			getRecentDirectory(recentFilesList, recentFilesLimit);
		} else {
			goToRecentList = false;
			getDirectory(currentPath);
		}
		return getView();
	}

	private boolean containsObjFiles(File file) {
		if (file.isDirectory()) {
			File[] f = file.listFiles();
			for (int i = 0; i < f.length; i++)
				if (containsObjFiles(f[i]))
					return true;
		} else {
			if (file.getName().endsWith(".obj")
					|| file.getName().endsWith(".OBJ"))
				return true;
			else
				return false;
		}
		return false;
	}

	private boolean containsTexFiles(File file) {
		if (file.isDirectory()) {
			File[] f = file.listFiles();
			for (int i = 0; i < f.length; i++)
				if (containsTexFiles(f[i]))
					return true;
		} else {
			if (file.getName().endsWith(".jpg")
					|| file.getName().endsWith(".JPG")
					|| file.getName().endsWith(".png")
					|| file.getName().endsWith(".PNG")
					|| file.getName().endsWith(".tga")
					|| file.getName().endsWith(".TGA"))
				return true;
			else
				return false;
		}
		return false;
	}

	/**
	 * Sets the ListAdapter with a list of recently opened files. This method
	 * does not use any files or directories, so cannot be navigated through.
	 * 
	 * @param recentFiles
	 *            list of recently opened files, stored as paths
	 * @param recentLimit
	 */
	private void getRecentDirectory(String[] recentFiles, int recentLimit) {
		myPath.setText("Recent Files");

		itemPath = new LinkedList<String>();
		itemNames = new LinkedList<String>();
		fileTypeList = new LinkedList<FileType>();

		if (recentFiles != null) {
			for (int i = Math.max(recentFiles.length - recentLimit, 0); i < recentFiles.length; i++) {
				itemPath.add(recentFiles[i]);
				itemNames.add(recentFiles[i]);
			}

			Collections.sort(itemPath);
			Collections.sort(itemNames);
			for (int j = 0; j < itemNames.size(); j++) {
				String name = itemNames.get(j);
				itemNames.set(j, name.substring(name.lastIndexOf("/") + 1));
			}
		}

		fileBrowserTitle.setText(R.string.recent_files_title_string);
		fileBrowserTitle.setTextSize(16);
		ImageView iv = (ImageView) getView().findViewById(
				R.id.file_browser_divider);
		iv.setImageResource(R.drawable.view_divider);

		fileArray = new ArrayAdapter<String>(getActivity(), R.layout.row,
				itemNames);
		fileArray = new FileArrayAdapter(getActivity(), itemNames, selectingTextures);
		setListAdapter(fileArray);
	}

	private void getDirectory(String dirPath) {
		myPath.setText("Current Folder: " + dirPath);
		currentPath = dirPath;

		itemPath = new LinkedList<String>();
		fileTypeList = new LinkedList<FileType>();
		File file = new File(dirPath);
		File[] fileList = file.listFiles();

		parentPath = file.getParent() + '/';

		// for each file in the current directory,
		for (int w = 0; w < fileList.length; w++) {
			File f = fileList[w];

			if (!f.isHidden() && f.canRead()) {

				if (f.isDirectory()) {
					if (enableOBJFileScan) {
						if (enableTexFileScan) {
							if (containsTexFiles(f)){
								itemPath.add(f.getName() + "/");
								fileTypeList.add(FileType.FOLDER);
							}
						} else {
							if (containsObjFiles(f)){
								itemPath.add(f.getName() + "/");
								fileTypeList.add(FileType.FOLDER);
							}
						}

						// if(!selectingTextures){
						// if (containsObjMap != null &&
						// containsObjMap.getBoolean(f.getPath()))
						// itemPath.add(f.getName() + "/");
					} else {
						itemPath.add(f.getName() + "/");
						fileTypeList.add(FileType.FOLDER);
					}
				} else {
					// check if file is an OBJ file
					if (!selectingTextures) {
						if (f.getName().endsWith(".obj")
								|| f.getName().endsWith(".OBJ")) {
							itemPath.add(f.getName());
							fileTypeList.add(FileType.OBJ);
						}
					} else {
						if (f.getName().endsWith(".jpg")
								|| f.getName().endsWith(".JPG")
								|| f.getName().endsWith(".png")
								|| f.getName().endsWith(".PNG")
								|| f.getName().endsWith(".tga")
								|| f.getName().endsWith(".TGA")) {
							itemPath.add(f.getName());
							fileTypeList.add(FileType.IMG);
						}
					}
				}

			}
		}

		Collections.sort(itemPath);

		if (!dirPath.equals(root)) {

			itemPath.add(0, "Go To Root");
			itemPath.add(1, "...");
		}

		if (selectingTextures) {
			Resources res = getResources();
			fileBrowserTitle.setText(Html.fromHtml("Choose a texture for:<br>" + "<i>" + modelFileName + "</i>"));
			fileBrowserTitle.setTextSize(16);
			ImageView iv = (ImageView) currentView
					.findViewById(R.id.file_browser_divider);
			iv.setImageResource(R.drawable.view_divider_alt);
			// listTitle.setTextSize(25);
			// listTitle.setTextColor(res.getColor(R.color.black));
			// listTitle.setBackgroundColor(res.getColor(R.color.white));

			itemPath.add(0, "Don't use a Texture");
		} else {
			Resources res = getResources();
			fileBrowserTitle.setText("Choose model to load \n(OBJ only)");
			fileBrowserTitle.setTextSize(16);
			ImageView iv = (ImageView) currentView
					.findViewById(R.id.file_browser_divider);
			iv.setImageResource(R.drawable.view_divider);
			// listTitle.setTextSize(25);
			// listTitle.setTextColor(res.getColor(R.color.white));
			// listTitle.setBackgroundColor(res.getColor(R.color.darkgrey));
		}

//		fileArray = new ArrayAdapter<String>(getActivity(), (selectingTextures) ? R.layout.row_alt : R.layout.row,
//				itemPath);
		fileArray = new FileArrayAdapter(getActivity(), itemPath, selectingTextures);
		setListAdapter(fileArray);

	}
	
	public void clearFileList(){
		SharedPreferences.Editor editor = getActivity()
				.getSharedPreferences("com.ubertome.modelviewer.recent_files",
						Context.MODE_PRIVATE).edit();
		editor.clear().commit();
		Toast.makeText(getActivity(), "Recent items cleared!",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onListItemClick(ListView lv, View v, int position, long id) {

		String filePath = itemPath.get(position);

		if (filePath.equals("Go To Root"))
			filePath = root;
		else if (filePath.equals("..."))
			filePath = parentPath;
		else if (filePath.equals("Don't use a Texture")) {
		} else {

			// picked from the normal file browser
			if (!goToRecentList)
				filePath = currentPath + filePath;

			// picked from recent files list
			else if (selectingTextures)
				filePath = currentPath + filePath;
		}

		File file = new File(filePath);

		if (file.isDirectory()) {
			if (file.canRead()) {
				getDirectory(filePath);
			} else {
				// error: folder can't be read
				new AlertDialog.Builder(getActivity())
						.setIcon(R.drawable.alert_small)
						.setTitle("[" + file.getName() + "] cannot be read!")
						.setPositiveButton("OK", null).show();
			}
		} else if (file.getName().equals("Nothing to see here")) {
		} else {
			// this is where the file is chosen

			// first pass: user selects an OBJ file
			if (!selectingTextures) {
				if ((file.getName().indexOf(".obj", 0) == -1)
						&& (file.getName().indexOf(".OBJ", 0) == -1)) {
					new AlertDialog.Builder(getActivity())
							.setIcon(R.drawable.alert_small)
							.setTitle(
									"Can't load file: \r\nMust be an OBJ file.")
							.setPositiveButton("OK", null).show();
				} else {
					modelFile = file.getPath();
					modelFileName = file.getName();
					selectingTextures = true;
					enableTexFileScan = true;
					getDirectory(modelFile.substring(0,
							modelFile.lastIndexOf("/") + 1));
				}
			}

			// second pass: user has selected an OBJ file,
			// now prompt for a texture to use
			else {
				// launch RenderActivity with no textures
				if (file.getName().equals("Don't use a Texture")) {
					Bundle noTextureRender = new Bundle();
					noTextureRender.putString(fileToParse, modelFile);
					noTextureRender.putString(texToParse, "textures_disabled");
					// noTextureRender.putExtra(modelPathName, file.getPath());

					// save this file path to a list of recent files
					SharedPreferences sharedPref = getActivity()
							.getSharedPreferences(
									"com.ubertome.modelviewer.recent_files",
									Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(modelFile, modelFile);
					editor.commit();

					// if(goToRecentList)
					selectingTextures = false;
					mCallback.sendToActivity(noTextureRender);
				}

				// error: texture has to be one of the following formats
				else if ((file.getName().indexOf(".jpg", 0) == -1)
						&& (file.getName().indexOf(".JPG", 0) == -1)
						&& (file.getName().indexOf(".png", 0) == -1)
						&& (file.getName().indexOf(".PNG", 0) == -1)
						&& (file.getName().indexOf(".tga", 0) == -1)
						&& (file.getName().indexOf(".TGA", 0) == -1)) {
					new AlertDialog.Builder(getActivity())
							.setIcon(R.drawable.alert_small)
							.setTitle(
									"Can't load texture: \r\nMust be a jpg/png/tga file")
							.setPositiveButton("OK", null).show();
				}

				// texture selected, launch in RenderFragment
				else {

					// launch RenderFragment
					Bundle goToRender = new Bundle();
					goToRender.putString(fileToParse, modelFile);
					goToRender.putString(texToParse, file.getPath());
					// goToRender.putExtra(modelPathName, file.getPath());

					SharedPreferences sharedPref = getActivity()
							.getSharedPreferences(
									"com.ubertome.modelviewer.recent_files",
									Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(modelFile, modelFile);
					editor.commit();

					// if(goToRecentList)
					selectingTextures = false;
					mCallback.sendToActivity(goToRender);
				}
			}

		}
	}

	// @Override
	// public void onBackPressed() {
	// // if current path is not at root, go up one directory
	// if (!goToRecentList && !currentPath.equals(root)) {
	// getDirectory(parentPath);
	// } else {
	// super.onBackPressed();
	// }
	// }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if (goToRecentList)
			inflater.inflate(R.menu.recent_files_menu, menu);
		else if (!selectingTextures) {
			if (!enableOBJFileScan)
				inflater.inflate(R.menu.file_explorer, menu);
			else
				inflater.inflate(R.menu.file_explorer_show_all, menu);
		} else {
			if (!enableTexFileScan)
				inflater.inflate(R.menu.file_explorer_tex, menu);
			else
				inflater.inflate(R.menu.file_explorer_show_all, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_recent: {
			SharedPreferences.Editor editor = getActivity()
					.getSharedPreferences("com.ubertome.modelviewer.prefs",
							Context.MODE_PRIVATE).edit();
			editor.clear().commit();
			setListAdapter(new ArrayAdapter<String>(getActivity(),
					R.layout.row, new LinkedList<String>()));
			fileArray.notifyDataSetChanged();
			Toast.makeText(getActivity(), "Recent items cleared!",
					Toast.LENGTH_SHORT).show();

			// setContentView(R.layout.activity_file_explorer);
			return true;
		}
		case R.id.show_only_objs: {
			// enableOBJFileScan = !enableOBJFileScan;
			// getDirectory(root);
			// fileArray.notifyDataSetChanged();
			// invalidateOptionsMenu();
			return true;
		}
		// case R.id.show_only_tex:{
		// enableTexFileScan = !enableTexFileScan;
		// getDirectory(root);
		// fileArray.notifyDataSetChanged();
		// return true;
		// }
		case R.id.show_all_files: {
			// enableOBJFileScan = false;
			// enableTexFileScan = false;
			// getDirectory(root);
			// fileArray.notifyDataSetChanged();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

}
