package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.devxperiments.flaglivewallpaper.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener{

	public static final String FLAG_IMAGE_SETTING = "flag";
	public static final String MULTIPLE_FLAG_IMAGE_SETTING = "multi_flag";
	public static final String FEEDBACK = "feedback";
	public static final String CREDITS = "credits";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		findPreference(FLAG_IMAGE_SETTING).setOnPreferenceClickListener(this);
		findPreference(FEEDBACK).setOnPreferenceClickListener(this);
		findPreference(CREDITS).setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(FLAG_IMAGE_SETTING)) {
			Intent intent = new Intent(this, WallpaperChooser.class);
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

	public static List<Integer> parseMultiFlagPreference(String flagPreference){

		if(flagPreference.equals("none"))
			return null;
		List<Integer> flagSequence = new ArrayList<Integer>();
		StringTokenizer tokenizer = new StringTokenizer(flagPreference, "-");
		while(tokenizer.hasMoreTokens())
			flagSequence.add(Integer.parseInt(tokenizer.nextToken()));
		return flagSequence;
	}

}
