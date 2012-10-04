package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.FlagWallpaperService;
import com.devxperiments.flaglivewallpaper.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Settings extends PreferenceActivity implements OnPreferenceClickListener, OnSharedPreferenceChangeListener{
	
	public static final String FLAG_MODE_SETTING = "flag_mode_setting";
	public static final String FLAG_MODE_FULLSCREEN ="flag_fullscreen";
	public static final String FLAG_MODE_SKY ="flag_sky";
	public static final String SKY_MODE_BACKGROUND_IMAGE ="sky_image";
	public static final String SKY_USER_BACKGROUND = "sys_sky_user";
	public static final String DAY_TIME_SKY_BACKGROUND ="day_time_sky";
	
	public static final String FLAG_IMAGE_SETTING = "flag_image_setting";
	public static final String SINGLE_FLAG_IMAGE_SETTING = "single_flag";
	public static final String MULTIPLE_FLAG_IMAGE_SETTING = "multi_flag";
	public static final String MULTIPLE_FLAG_TIME_MIN = "multi_flag_time";
	
	public static final String ALPHA_SETTING = "alpha";
	
	public static final String FEEDBACK = "feedback";
	public static final String CREDITS = "credits";
	
	private SharedPreferences prefs;
//	private Preference flagTimePref;
	private Preference skyBackgroundPreference, alphaPreference, flagImagePreference;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		flagImagePreference = findPreference(SINGLE_FLAG_IMAGE_SETTING);
		flagImagePreference.setOnPreferenceClickListener(this);
		findPreference(FEEDBACK).setOnPreferenceClickListener(this);
		findPreference(CREDITS).setOnPreferenceClickListener(this);
		
		alphaPreference = findPreference(ALPHA_SETTING);
		skyBackgroundPreference = findPreference(SKY_MODE_BACKGROUND_IMAGE);
		skyBackgroundPreference.setOnPreferenceClickListener(this);
//		flagTimePref = findPreference(MULTIPLE_FLAG_TIME_MIN);
		findPreference("default").setOnPreferenceClickListener(this);
		
		changeEnability();
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals(SINGLE_FLAG_IMAGE_SETTING)) {
			
			@SuppressWarnings("rawtypes")
			Class clazz;
			if (prefs.getString(FLAG_IMAGE_SETTING, SINGLE_FLAG_IMAGE_SETTING).equals(SINGLE_FLAG_IMAGE_SETTING))
				clazz = WallpaperChooser.class;
			else{
				if(!FlagWallpaperService.PRO){
					Toast.makeText(this, R.string.strOnlyInProVersion, Toast.LENGTH_LONG).show();
					return true;
				}
				clazz  = MultipleWallpaperChooser.class;
			}
			Intent intent = new Intent(this, clazz);
			startActivity(intent);
			return true;
		}else if(key.equals(FEEDBACK)){
			Intent intent = new Intent(this, FeedbackActivity.class);
			startActivity(intent);
			return true;
		}else if(key.equals(CREDITS)){
			Intent intent = new Intent(this, CreditsActivity.class);
			startActivity(intent);
			return true;
		}else if(key.equals(SKY_MODE_BACKGROUND_IMAGE)){
			Intent intent = new Intent(this, WallpaperChooser.class);
			intent.putExtra(SKY_MODE_BACKGROUND_IMAGE, true);
			startActivity(intent);
			return true;
		}else if(key.equals("default")){
			Editor editor = prefs.edit(); 
			editor.clear();
			editor.commit();
			Intent intent = getIntent();
			overridePendingTransition(0, 0);
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			finish();
			overridePendingTransition(0, 0);
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
//		if(key.equals(DAY_TIME_SKY_BACKGROUND)){
//			if(prefs.getBoolean(DAY_TIME_SKY_BACKGROUND, true))
//				DayTimeAlarmManager.start(FlagWallpaperService.context);
//			else 
//				DayTimeAlarmManager.stop();
//		}
		changeEnability();
	}

	private void changeEnability(){
		if(prefs.getString(FLAG_MODE_SETTING, FLAG_MODE_SETTING).equals(FLAG_MODE_FULLSCREEN)){
			alphaPreference.setEnabled(false);
			skyBackgroundPreference.setEnabled(false);
		}else{
			alphaPreference.setEnabled(true);
			skyBackgroundPreference.setEnabled(true);
		}
//		if(prefs.getString(FLAG_IMAGE_SETTING, SINGLE_FLAG_IMAGE_SETTING).equals(SINGLE_FLAG_IMAGE_SETTING)){
//			flagTimePref.setEnabled(false);
//			flagImagePreference.setTitle(R.string.strSelectFlagSng);
//		}else{
//			flagImagePreference.setTitle(R.string.strSelectFlagPlr);
//			flagTimePref.setEnabled(true);
//		}
	}
	
}
