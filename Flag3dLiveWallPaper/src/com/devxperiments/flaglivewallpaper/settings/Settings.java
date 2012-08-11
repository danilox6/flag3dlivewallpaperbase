package com.devxperiments.flaglivewallpaper.settings;


import com.devxperiments.flaglivewallpaper.R;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

	public static final String ANIMATIOM_SPEED = "speed";
	public static final String CAMERA_ZOOM = "zoom";
	public static final String BACKGROUND_COLOR = "background";
	public static final String AMBIENT_LIGHT = "light";
	public static final String ORIENTATION = "orientation";
	public static final String FLAG_IMAGE = "flag";
	public static final String PREFS_NAME = "setting";
	
	
	
	private Preference colorPickerDialog;
	private Preference flagImagePicker;
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
//        getPreferenceManager().setSharedPreferencesName(Settings.PREFS_NAME);
        addPreferencesFromResource(R.xml.settings);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        flagImagePicker = findPreference(FLAG_IMAGE);
        flagImagePicker.setOnPreferenceClickListener(this);
        colorPickerDialog = findPreference(BACKGROUND_COLOR);
       	colorPickerDialog.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,	String key) {
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Settings.this);
		Log.w(FLAG_IMAGE, "settings");
		String key = preference.getKey();
		if (key.equals(BACKGROUND_COLOR)) {

			final ColorPickerDialog d = new ColorPickerDialog(this, prefs.getInt(Settings.BACKGROUND_COLOR, 0xff000000));
			d.setAlphaSliderVisible(false);
			d.setButton("Ok", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					SharedPreferences.Editor editor = prefs.edit();
					editor.putInt(Settings.BACKGROUND_COLOR, d.getColor());
					editor.commit();

				}
			});

			d.setButton2("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});

			d.show();

			return true;
		}
		if(key.equals(FLAG_IMAGE)){
			Log.w(FLAG_IMAGE, "flag");
			Intent intent = new Intent(this, GalleryView.class);
			startActivity(intent);
			
			return true;
		}
		return false;
	}

}
