package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.devxperiments.flaglivewallpaper.settings.BitmapUtils;
import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

public class FlagManager {
	private static HashMap<String, Integer> flagIds = null;
	private static String defaultFlag = null;

	private static final String LANDSCAPE = "_landscape";
	private static final String DEFAULT = "default_";
	private static final String SKY = "sky_";
	private static final String SYSTEM_RES = "sys_";

	@SuppressWarnings("rawtypes")
	public static void inizialize(String defPackage){
		flagIds = new HashMap<String, Integer>();
		Class resources = R.drawable.class;
		Field[] fields = resources.getFields();
		defaultFlag = fields[0].getName();

		boolean loadDefault = false;
		boolean loadBackground = false;

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(FlagWallpaperService.context);
		String flagToLoad = prefs.getString(Settings.SINGLE_FLAG_IMAGE_SETTING, null);
		if(flagToLoad == null)
			loadDefault = true;

		String backgroundToLoad = null;
		if(prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_FULLSCREEN).equals(Settings.FLAG_MODE_SKY)){
			loadBackground = true;
			if (prefs.getBoolean(Settings.DAY_TIME_SKY_BACKGROUND, true)){
				backgroundToLoad = DayTimeAlarmManager.getAttualDayTimeString();
			}else{
				backgroundToLoad = prefs.getString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_day");
				if (backgroundToLoad.equals(Settings.SKY_USER_BACKGROUND))
					loadBackground = false;
			}
		}

		for (Field field: fields ) {
			String name = field.getName();

			if(!name.startsWith(SYSTEM_RES)){

				if(name.startsWith(DEFAULT))
					defaultFlag = name;

				int id = FlagWallpaperService.context.getResources().getIdentifier(name, "drawable", defPackage);


				Log.e("RESOURCE","Res: "+ name + " "+id);
				//				if(!name.startsWith(SKY))
				flagIds.put(name, id);

				if((loadBackground && name.startsWith(backgroundToLoad)) || (loadDefault && name.startsWith(DEFAULT)) || (flagToLoad!= null && name.startsWith(flagToLoad)))
					loadTexture(name, id);
			}
		}

		if(backgroundToLoad != null && !loadBackground){
			loadUserTexture();
		}

		TextureManager.getInstance().compress();
	}

	private static void loadUserTexture() {
		boolean portrait = FlagWallpaperService.context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
		Bitmap bitmap = BitmapUtils.getUserBitmap(FlagWallpaperService.context, portrait);
		loadTexture(Settings.SKY_USER_BACKGROUND, bitmap);
	}

	private static void loadTexture(String textureName, int id){
		int count = 0;
		OutOfMemoryError exc = null;
		do{
			try{
				Bitmap texture = BitmapHelper.convert(FlagWallpaperService.context.getResources().getDrawable(id));
				loadTexture(textureName, texture);
			}catch (OutOfMemoryError e) {
				Log.e("EXCEPTION", "OutOfMemory!!!");
				exc = e;
			}
		}while(exc!=null || ++count<5);

	}

	public static void loadTexture(String textureName){
		if(!TextureManager.getInstance().containsTexture(textureName)){
			if(textureName.startsWith(Settings.SKY_USER_BACKGROUND))
				loadUserTexture();
			else
				loadTexture(textureName, flagIds.get(textureName));
		}
	}

	private static void loadTexture(String textureName, Bitmap texture){
		if(!TextureManager.getInstance().containsTexture(textureName)){
			Log.e("TEXTURE", "Loaded texture: "+textureName);
			TextureManager.getInstance().addTexture(textureName, new Texture(texture));
		}
	}


	public static int getFlagId(String flagName){
		return flagIds.get(flagName);
	}

	public static String toPortrait(String flagName){
		if(flagName.endsWith(LANDSCAPE))
			return flagName.substring(0, flagName.indexOf(LANDSCAPE));
		return flagName;
	}

	public static int toPortrait(int flagId){
		String flag = getFlagNameById(flagId);
		return flagIds.get(toPortrait(flag));
	}

	public static String toLandscape(String flagName){
		if(!flagName.endsWith(LANDSCAPE))
			if (flagIds.containsKey(flagName + LANDSCAPE))
				return flagName + LANDSCAPE;
		return flagName;
	}

	public static int toLandscape(int flagId){
		String flag = getFlagNameById(flagId);
		return flagIds.get(toLandscape(flag));
	}

	public static String getDefaultFlag(){
		//		if(defaultFlag == null)
		//			inizialize();
		return defaultFlag;
	}

	public static List<Integer> getPortraitFlagIds(){
		List<Integer> portraitIds = new ArrayList<Integer>();
		portraitIds.add(flagIds.get(defaultFlag));
		for(String flagName: flagIds.keySet())
			if(!flagName.endsWith(LANDSCAPE) && !flagName.startsWith(SKY) &&!flagName.startsWith(DEFAULT))
				portraitIds.add(flagIds.get(flagName));
		return portraitIds;
	}

	public static List<Integer> getSkyBackgroundIds(){
		List<Integer> skyIds = new ArrayList<Integer>();
		for(String flagName: flagIds.keySet())
			if(flagName.startsWith(SKY))
				skyIds.add(flagIds.get(flagName));
		skyIds.add(R.drawable.sys_btn_add);
		return skyIds;
	}

	public static String getFlagNameById(int id){
		for(String key: flagIds.keySet())
			if (flagIds.get(key) == id)
				return key;
		return null;
	}

}
