package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener{

	public static final String MULTIPLE_FLAG_TIME_SEC = "multi_flag_time";
	public static final String FLAG_IMAGE_SETTING = "flag_image_setting";
	public static final String SINGLE_FLAG_IMAGE_SETTING = "single_flag";
	public static final String MULTIPLE_FLAG_IMAGE_SETTING = "multi_flag";
	
	public static final String FEEDBACK = "feedback";
	public static final String CREDITS = "credits";
	
	SharedPreferences prefs;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		findPreference(SINGLE_FLAG_IMAGE_SETTING).setOnPreferenceClickListener(this);
		findPreference(FEEDBACK).setOnPreferenceClickListener(this);
		findPreference(CREDITS).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(SINGLE_FLAG_IMAGE_SETTING)) {
			
			@SuppressWarnings("rawtypes")
			Class clazz;
			if (prefs.getString(FLAG_IMAGE_SETTING, SINGLE_FLAG_IMAGE_SETTING).equals(SINGLE_FLAG_IMAGE_SETTING))
				clazz = WallpaperChooser.class;
			else
				clazz  = MultipleWallpaperChooser.class;
			Intent intent = new Intent(this, clazz);
			startActivity(intent);
			return true;
		}else if(key.equals(FEEDBACK)){
			Intent intent = new Intent(this, FeedbackActivity.class);
			startActivity(intent);
			return true;
		}else if(key.equals(CREDITS)){
			Intent intent = new Intent(this, MultipleWallpaperChooser.class);
			startActivity(intent);
			return true;
		}
		return false;
	};

	public static List<Integer> parseMultiFlagPreferenceIDs(String flagPreference){

		if(flagPreference == null || flagPreference.equals("none"))
			return FlagManager.getPortraitFlagIds();
		List<Integer> flagSequence = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(flagPreference, "-");
		while(tokenizer.hasMoreTokens())
			flagSequence.add(Integer.parseInt(tokenizer.nextToken()));
		return flagSequence;
	}
	
	public static List<String> parseMultiFlagPreference(String flagPreference){
		List<String> flags = new ArrayList<String>();
		List<Integer> ids = parseMultiFlagPreferenceIDs(flagPreference);
		for(Integer id: ids)
			flags.add(FlagManager.getFlagNameById(id));
		return flags;
	}

}
