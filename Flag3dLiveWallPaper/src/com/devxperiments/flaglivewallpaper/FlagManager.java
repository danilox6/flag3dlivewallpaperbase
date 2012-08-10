package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.HashMap;

import android.content.Context;

import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.util.BitmapHelper;

public class FlagManager {
	private static HashMap<String, Integer> flagIds = new HashMap<String, Integer>();
	private static String defaultFlag = null;
	
	@SuppressWarnings("rawtypes")
	public static void inizialize(Context context){
		Class resources = R.drawable.class;
		Field[] fields = resources.getFields();
		for (Field field: fields ) {
			if(!field.getName().equals("ic_launcher")){
				if(field.getName().startsWith("default_"))
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
		if(flagName.endsWith("_landscape"))
			return flagName.substring(0, flagName.indexOf("_landscape"));
		return flagName;
	}
	
	public static String toLandscape(String flagName){
		if(!flagName.endsWith("_landscape"))
			if (flagIds.containsKey(flagName + "_landscape"))
				return flagName + "_landscape";
		return flagName;
	}
	
	public static String getDefaultFlag(){
		return defaultFlag;
	}
	
	public static Integer[] getFlagIds(){
		Integer[] ids = new Integer[flagIds.size()];
		int i = 0;
		for(Integer id: flagIds.values()){
			ids[i++] = id;
		}
		return ids;
	}
	
	public static String getFlagNameById(int id){
		for(String key: flagIds.keySet())
			if (flagIds.get(key) == id)
				return key;
		return null;
	}
}
