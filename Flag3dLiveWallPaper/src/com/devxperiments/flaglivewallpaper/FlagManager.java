package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

public class FlagManager {
	private static HashMap<String, Integer> flagIds = new HashMap<String, Integer>();
	private static String defaultFlag = null;
	
	public static final String LANDSCAPE = "_landscape";
	public static final String DEFAULT = "default_";
	
	@SuppressWarnings("rawtypes")
	public static void inizialize(Context context){
		Class resources = R.drawable.class;
		Field[] fields = resources.getFields();
		for (Field field: fields ) {
			if(!field.getName().equals("ic_launcher")){
				if(field.getName().startsWith(DEFAULT))
					defaultFlag = field.getName();
				Integer id = flagIds.get(field.getName());
				if (id == null){
					id = context.getResources().getIdentifier(field.getName(), "drawable", "com.devxperiments.flaglivewallpaper");
					flagIds.put(field.getName(), id);
				}
				if(!TextureManager.getInstance().containsTexture(field.getName()))
					TextureManager.getInstance().addTexture(field.getName(), new Texture(BitmapHelper.convert(context.getResources().getDrawable(id))));
			}
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
		return defaultFlag;
	}
	
	public static List<Integer> getPortraitFlagIds(){
		List<Integer> portraitIds = new ArrayList<Integer>();
		for(String flagName: flagIds.keySet()){
			if(!flagName.endsWith(LANDSCAPE))
				portraitIds.add(flagIds.get(flagName));
		}
		return portraitIds;
	}
	
	public static String getFlagNameById(int id){
		for(String key: flagIds.keySet())
			if (flagIds.get(key) == id)
				return key;
		return null;
	}
}
