package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

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

	private final int CREDITS_DIALOG = 0;
	private final int FEEDBACK_DIALOG = 1;
	private final int DEFAULT_DIALOG = 3;
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

//			@SuppressWarnings("rawtypes")
//			Class clazz;
//			if (prefs.getString(FLAG_IMAGE_SETTING, SINGLE_FLAG_IMAGE_SETTING).equals(SINGLE_FLAG_IMAGE_SETTING))
//				clazz = WallpaperChooser.class;
//			else{
//				if(!FlagWallpaperService.PRO){
//					Toast.makeText(this, R.string.strOnlyInProVersion, Toast.LENGTH_LONG).show();
//					return true;
//				}
//				clazz  = MultipleWallpaperChooser.class;
//			}
//			Intent intent = new Intent(this, clazz);
			Intent intent = new Intent(this, WallpaperChooser.class);
			startActivity(intent);
			return true;
		}else if(key.equals(FEEDBACK)){
			this.showDialog(FEEDBACK_DIALOG);
			return true;
		}else if(key.equals(CREDITS)){
			this.showDialog(CREDITS_DIALOG);
			return true;
		}else if(key.equals(SKY_MODE_BACKGROUND_IMAGE)){
			Intent intent = new Intent(this, WallpaperChooser.class);
			intent.putExtra(SKY_MODE_BACKGROUND_IMAGE, true);
			startActivity(intent);
			return true;
		}else if(key.equals("default")){
			this.showDialog(DEFAULT_DIALOG);
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
			alphaPreference.setSummary(R.string.prefOnlySky);
			skyBackgroundPreference.setEnabled(false);
			skyBackgroundPreference.setSummary(R.string.prefOnlySky);
			
		}else{
			alphaPreference.setEnabled(true);
			alphaPreference.setSummary(R.string.prefTransparencySummary);
			skyBackgroundPreference.setEnabled(true);
			skyBackgroundPreference.setSummary(R.string.prefBackgroundSummary);
		}
		//		if(prefs.getString(FLAG_IMAGE_SETTING, SINGLE_FLAG_IMAGE_SETTING).equals(SINGLE_FLAG_IMAGE_SETTING)){
		//			flagTimePref.setEnabled(false);
		//			flagImagePreference.setTitle(R.string.strSelectFlagSng);
		//		}else{
		//			flagImagePreference.setTitle(R.string.strSelectFlagPlr);
		//			flagTimePref.setEnabled(true);
		//		}
	}

	private String getAppName(){
		final PackageManager pm = getApplicationContext().getPackageManager();
		ApplicationInfo ai;
		try {
			ai = pm.getApplicationInfo( this.getPackageName(), 0);
		} catch (final NameNotFoundException e) {
			ai = null;
		}
		return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch(id) {
		case CREDITS_DIALOG:
			TextView message = new TextView(this);
			SpannableString s = new SpannableString(getText(R.string.txtCredits));
			Linkify.addLinks(s, Linkify.WEB_URLS);
			message.setText(s);
			message.setMovementMethod(LinkMovementMethod.getInstance());

			builder.setTitle(R.string.prefCredits)
			//			.setMessage(R.string.txtCredits)
			.setView(message)
			.setCancelable(false)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			break;
		case FEEDBACK_DIALOG:
			builder.setTitle(R.string.prefFeedback)
			.setMessage(R.string.txtFeedback)
			.setCancelable(true)
			.setPositiveButton(R.string.strBtnFeedback, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent email = new Intent(Intent.ACTION_SEND);
					email.putExtra(Intent.EXTRA_EMAIL, new String[]{"daniloiannelli6@gmail.com"});		  
					email.putExtra(Intent.EXTRA_SUBJECT, "Feedback - App: "+getAppName());
					email.setType("message/rfc822");
					startActivity(email);
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			break;
		case DEFAULT_DIALOG:
			builder.setTitle(R.string.prefDefault)
			.setMessage(R.string.txtRestoreDefault)
			.setCancelable(true)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Editor editor = prefs.edit(); 
					editor.clear();
					editor.commit();
					Intent intent = getIntent();
					overridePendingTransition(0, 0);
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
					finish();
					overridePendingTransition(0, 0);
					startActivity(intent);
					dialog.dismiss();
				}
			})
			.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			break;
		}
		return builder.create();
	}
}
