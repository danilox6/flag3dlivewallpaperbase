package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlagManager {
	private static HashMap<String, Integer> flagIds = null;
	private static String defaultFlag = null;
	
	public static final String LANDSCAPE = "_landscape";
	public static final String DEFAULT = "default_";
	
	
	@SuppressWarnings("rawtypes")
	public static void inizialize(){
		flagIds = new HashMap<String, Integer>();
		Class resources = R.drawable.class;
		Field[] fields = resources.getFields();
		defaultFlag = fields[0].getName();
		for (Field field: fields ) {
			if(!field.getName().equals("ic_launcher") && !field.getName().startsWith("list_")){
				if(field.getName().startsWith(DEFAULT))
					defaultFlag = field.getName();
				Integer id = flagIds.get(field.getName());
				if (id == null){
					id = FlagWallpaperService.context.getResources().getIdentifier(field.getName(), "drawable", "com.devxperiments.flaglivewallpaper");
					flagIds.put(field.getName(), id);
				}
			}
		}
	}
	
	
	public static int getFlagId(String flagName){
		if(isReleased())
			inizialize();
		return flagIds.get(flagName);
	}

	public static String toPortrait(String flagName){
		if(isReleased())
			inizialize();
		if(flagName.endsWith(LANDSCAPE))
			return flagName.substring(0, flagName.indexOf(LANDSCAPE));
		return flagName;
	}
	
	public static int toPortrait(int flagId){
		String flag = getFlagNameById(flagId);
		return flagIds.get(toPortrait(flag));
	}
	
	public static String toLandscape(String flagName){
		if(isReleased())
			inizialize();
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
		if(defaultFlag == null)
			inizialize();
		return defaultFlag;
	}
	
	public static List<Integer> getPortraitFlagIds(){
		if(isReleased())
			inizialize();
		List<Integer> portraitIds = new ArrayList<Integer>();
		for(String flagName: flagIds.keySet()){
			if(!flagName.endsWith(LANDSCAPE))
				portraitIds.add(flagIds.get(flagName));
		}
		return portraitIds;
	}
	
	public static String getFlagNameById(int id){
		if(isReleased())
			inizialize();
		for(String key: flagIds.keySet())
			if (flagIds.get(key) == id)
				return key;
		return null;
	}
	
	public static void release(){
		if(flagIds!=null){
			flagIds.clear();
			flagIds = null;
			System.gc();
		}
	}
	
	public static boolean isReleased(){
		return flagIds == null? true: false; 
	}
}
