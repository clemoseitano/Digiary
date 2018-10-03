package net.tanozin.digiary.texttray;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.File;

import net.tanozin.digiary.Constants;
import net.tanozin.digiary.R;


/* EditPreferences
 * 		Simple activity that just displays the preferences
 * 		nothing really different here */
public class EditPreferences extends PreferenceActivity
{  	
	public void onCreate(Bundle savedInstanceState)
	{  
		super.onCreate(savedInstanceState);  

		// add preferences
		addPreferencesFromResource(R.xml.preferences);
		
		// default dir value to display
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String current = sharedPref.getString("defaultdir", Constants.NOTE_TEXT_DIR);

		Preference p = findPreference("defaultdir");
		p.setSummary(current);
	}
	
	/****************************************************************
	 * onActivityResult()
	 * 		results of a launched activity */
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		String location = data.getAction();
		
		File f = new File(location);
		if (f.toString().equals("/"))
			location = "/";
		else if (f.isDirectory())
			location = f.toString() + "/";	
		else if (f.getParent().equals("/"))
			location = "/";
		else
			location = f.getParent() + "/";
		
		// save the directory
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
		editor.putString("defaultdir", location);
		editor.apply();
		
		// update the display
		Preference p = findPreference("defaultdir");
		p.setSummary(location);		
	}
} // end class EditPreferences
