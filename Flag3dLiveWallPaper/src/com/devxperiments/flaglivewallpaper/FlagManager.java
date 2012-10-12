package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;

import com.devxperiments.flaglivewallpaper.settings.BitmapUtils;
import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

public class FlagManager {
	private static String defaultFlag = null;
	private static String defaultPackage;
	private static final String LANDSCAPE = "_landscape";
	public static final String DEFAULT = "default_";
	public static final String FREE = "free_";
	private static final String SKY = "sky_";
	private static final String SYSTEM_RES = "sys_";

	@SuppressWarnings("rawtypes")
	public static void inizialize(String defPackage){
		defaultPackage = defPackage;
		FlagIdsMap.init();
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
			if (prefs.getBoolean(Settings.DAY_TIME_SKY_BACKGROUND, false)){
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


				Log.i("FlagManager","Resource loaded: "+ name + " "+id);
				//				if(!name.startsWith(SKY))
				FlagIdsMap.put(name, id);

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
		Bitmap bitmap = BitmapUtils.getUserBitmap(FlagWallpaperService.context);
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
				Log.e("FlagManger", "OutOfMemoryError!!!");
				exc = e;
			}
		}while(exc!=null || ++count<5);

	}

	public static void loadTexture(String textureName){
		
		if(!TextureManager.getInstance().containsTexture(textureName)){
			if(textureName.startsWith(Settings.SKY_USER_BACKGROUND))
				loadUserTexture();
			else
				loadTexture(textureName, FlagIdsMap.get(textureName));
		}
	}

	private static void loadTexture(String textureName, Bitmap texture){
		if(!TextureManager.getInstance().containsTexture(textureName)){
			Log.i("FlagManager", "Loaded texture: "+textureName +" "+texture.getHeight()+"x"+texture.getWidth());
			TextureManager.getInstance().addTexture(textureName, new Texture(texture));
		}
	}


	public static int getFlagId(String flagName){
		return FlagIdsMap.get(flagName);
	}

	public static String toPortrait(String flagName){
		if(flagName.endsWith(LANDSCAPE))
			return flagName.substring(0, flagName.indexOf(LANDSCAPE));
		return flagName;
	}

	public static int toPortrait(int flagId){
		String flag = getFlagNameById(flagId);
		return FlagIdsMap.get(toPortrait(flag));
	}

	public static String toLandscape(String flagName){
		if(!flagName.endsWith(LANDSCAPE))
			if (FlagIdsMap.containsKey(flagName + LANDSCAPE))
				return flagName + LANDSCAPE;
		return flagName;
	}

	public static int toLandscape(int flagId){
		String flag = getFlagNameById(flagId);
		return FlagIdsMap.get(toLandscape(flag));
	}

	public static String getDefaultFlag(){
		if(defaultFlag == null)
			inizialize(defaultPackage);
		return defaultFlag;
	}

	public static List<Integer> getPortraitFlagIds(){
		List<Integer> portraitIds = new ArrayList<Integer>();
		portraitIds.add(FlagIdsMap.get(defaultFlag));
		List<String> flags = FlagIdsMap.keyList();
		Collections.sort(flags, new Comparator<String>() {

			@Override
			public int compare(String lhs, String rhs) {
				if((lhs.startsWith(FREE)&&rhs.startsWith(FREE))||(!lhs.startsWith(FREE)&&!rhs.startsWith(FREE)))
					return 0;
				if(lhs.startsWith(FREE))
					return -1;
				return 1;
			}
		});
		for(String flagName: flags)
			if(!flagName.endsWith(LANDSCAPE) && !flagName.startsWith(SKY) &&!flagName.startsWith(DEFAULT))
				portraitIds.add(FlagIdsMap.get(flagName));
		
		return portraitIds;
	}

	public static List<Integer> getSkyBackgroundIds(){
		List<Integer> skyIds = new ArrayList<Integer>();
		for(String flagName: FlagIdsMap.keySet())
			if(flagName.startsWith(SKY))
				skyIds.add(FlagIdsMap.get(flagName));
		skyIds.add(R.drawable.sys_btn_add);
		return skyIds;
	}

	public static String getFlagNameById(int id){
		for(String key: FlagIdsMap.keySet())
			if (FlagIdsMap.get(key) == id)
				return key;
		return null;
	}

	private static class FlagIdsMap{
		private static HashMap<String, Integer> flagIds = null;
		
		public static void init(){
			flagIds = new HashMap<String, Integer>();
		}
		
		public static int get(String key){
			if(flagIds==null)
				inizialize(defaultPackage);
			return flagIds.get(key);
		}
		
		public static void put(String key, Integer value){
			if(flagIds==null)
				inizialize(defaultPackage);
			flagIds.put(key, value);
		}
		
		public static Set<String> keySet(){
			if(flagIds==null)
				inizialize(defaultPackage);
			return flagIds.keySet();
		}
		
		public static boolean containsKey(String string) {
			if(flagIds==null)
				inizialize(defaultPackage);
			return flagIds.containsKey(string);
		}
		
		public static List<String> keyList(){
			List<String> keys = new ArrayList<String>();
			for(String key: keySet())
				keys.add(key);
			return keys;
		}
	}
}
