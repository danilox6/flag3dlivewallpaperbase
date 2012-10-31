package com.devxperiments.flaglivewallpaper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.devxperiments.flaglivewallpaper.settings.BitmapUtils;
import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.jbrush.ae.Animator;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.LightData;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.Object3D;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer, OnSharedPreferenceChangeListener{

	private FrameBuffer framebuffer = null;
	private World world = null;
	private Object3D flag = null, pole;
	private Timer updateFlagTimer = new Timer(true);
	private TimerTask updateFlagTask = null;
	private String texture = null;
	private SharedPreferences prefs;
	private boolean isSingleFlagSet, userBackPrefUpdated, imagePreferenceUpdated, modePreferenceUpdated;
	private static boolean dayTimeUpdated;

	private int width, height;

	public FlagRenderer(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener( this);
		imagePreferenceUpdated = false;
		modePreferenceUpdated = false;
		isSingleFlagSet = false;
		userBackPrefUpdated = false;
		width = height = 512;

		com.threed.jpct.Logger.setLogLevel(com.threed.jpct.Logger.WARNING);
		//		if(prefs.getBoolean(Settings.DAY_TIME_SKY_BACKGROUND, true) && !DayTimeAlarmManager.isRunning())
		//			DayTimeAlarmManager.start(context);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onDrawFrame(GL10 gl) {
		try{
			if(texture!=null){
				Log.i("FlagRenderer", "Texture to show: "+texture);
				FlagManager.loadTexture(texture);
				String oldTexture = flag.getUserObject()==null? null:((String)flag.getUserObject())+"";
				flag.setTexture(texture);
				flag.setUserObject(texture);
				if(oldTexture!=null && TextureManager.getInstance().containsTexture(oldTexture))
					TextureManager.getInstance().removeTexture(oldTexture);
				texture = null;
			}
			framebuffer.clear();
			if(prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_FULLSCREEN).equals(Settings.FLAG_MODE_SKY)){
				String background = null;
//			if(prefs.getBoolean(Settings.DAY_TIME_SKY_BACKGROUND, true) && dayTimeUpdated)
//				background = DayTimeAlarmManager.getAttualDayTimeString();
//			else
				background = prefs.getString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_night");
				FlagManager.loadTexture(background); 
				framebuffer.blit(TextureManager.getInstance().getTexture(background),
						(BitmapUtils.getBestFittingScreenPow(width,height)-width)/2,
						(BitmapUtils.getBestFittingScreenPow(width,height)-height)/2,
						0,0,
						width,height,FrameBuffer.OPAQUE_BLITTING);
			}
			world.renderScene(framebuffer);
			Animator.EnableAnimations();
			world.draw(framebuffer);
			framebuffer.display();
		}catch(OutOfMemoryError e){
//			TextureManager.getInstance().removeAndUnload(Settings.SKY_USER_BACKGROUND, framebuffer);
			prefs.edit().putString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_night").commit();
			System.gc();
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, final int screenWidth, final int screenHeight) {
		width = screenWidth;
		height = screenHeight;

		if (framebuffer != null) 
			framebuffer.dispose();

		framebuffer = new FrameBuffer(gl, screenWidth, screenHeight);
		if(world==null || modePreferenceUpdated){
			modePreferenceUpdated = false;
			drawScene();
			isSingleFlagSet = false;
		}

		boolean userBackground = prefs.getString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_night").equals(Settings.SKY_USER_BACKGROUND);
		if((userBackPrefUpdated && userBackground) || (userBackground && !TextureManager.getInstance().containsTexture(Settings.SKY_USER_BACKGROUND))){
			try{
				loadUserBanckgroundTexture(Settings.SKY_USER_BACKGROUND, BitmapUtils.getUserBitmap(FlagWallpaperService.context));
			}catch (NullPointerException e) {
				prefs.edit().putString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_night");
			}
//			TextureManager.getInstance().compress();
			userBackPrefUpdated = false;
			BitmapUtils.freeBitmaps();
		}

		String flagSetting = prefs.getString(Settings.FLAG_IMAGE_SETTING, Settings.SINGLE_FLAG_IMAGE_SETTING);



		if((flagSetting.equals(Settings.SINGLE_FLAG_IMAGE_SETTING) && !isSingleFlagSet) ||
				(flagSetting.equals(Settings.SINGLE_FLAG_IMAGE_SETTING) && imagePreferenceUpdated)){

			if(updateFlagTask != null)
				updateFlagTask.cancel();
			updateFlagTask = null;
			String texture = prefs.getString(Settings.SINGLE_FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());

			this.texture = getAppropriateFlag(texture, screenWidth, screenHeight);
			imagePreferenceUpdated = false;
			isSingleFlagSet = true;
			//			changeFlagTexture(texture, screenWidth, screenHeight);
		}else if(!isSingleFlagSet && (updateFlagTask == null || imagePreferenceUpdated)){

			isSingleFlagSet = false;
			imagePreferenceUpdated = false;

			if(updateFlagTask != null)
				updateFlagTask.cancel();

			final List<String> textures = Settings.parseMultiFlagPreference(prefs.getString(Settings.MULTIPLE_FLAG_IMAGE_SETTING, null));

			texture = getAppropriateFlag(textures.get(0), screenWidth, screenHeight);

			updateFlagTask = new TimerTask() {
				int i = 1;
				@Override
				public void run() {
					texture = getAppropriateFlag(textures.get(i), screenWidth, screenHeight);
					i = ++i%textures.size();
				}
			};

			int timeMillis = Integer.parseInt(prefs.getString(Settings.MULTIPLE_FLAG_TIME_MIN, "10"))*60*1000;
			//			updateFlagTimer.schedule(updateFlagTask, 5000);
			updateFlagTimer.schedule(updateFlagTask, timeMillis, timeMillis);
		}

		if(prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_FULLSCREEN).equals(Settings.FLAG_MODE_SKY)){
			int transparency = 15 - Math.round(prefs.getFloat(Settings.ALPHA_SETTING, -1F));
			flag.setTransparency(transparency);
			if(pole!=null)
				pole.setTransparency(transparency);
		}else{
			flag.setTransparency(-1);
			if(pole!=null)
				pole.setTransparency(-1);
		}

	}
	
	private void loadUserBanckgroundTexture(String textname,Bitmap bitmap){
		if(TextureManager.getInstance().containsTexture(textname)){
			TextureManager.getInstance().unloadTexture(framebuffer, TextureManager.getInstance().getTexture(textname));
			TextureManager.getInstance().replaceTexture(textname, new Texture(bitmap,true));
		}else
			TextureManager.getInstance().addTexture(textname, new Texture(bitmap,true));
		bitmap.recycle();
	}

	public void release() {
		if (framebuffer != null) {
			framebuffer.dispose();
		}
		if(updateFlagTask!=null)
			updateFlagTask.cancel();
		updateFlagTimer.cancel();
	}

	private void drawScene(){
		world = new World();

		Vector<EditorObject> objects = new Vector<EditorObject>();
		Vector<LightData> lights = new Vector<LightData>();


		String flagModeSetting = prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_FULLSCREEN);

		AssetManager assetManager = FlagWallpaperService.context.getAssets();
		objects = Scene.loadSerializedLevel(flagModeSetting+".txt", objects, lights, null,null, world, assetManager);

		flag = (Scene.findObject(flagModeSetting+"0", objects));
		Animator.Play(flag, "wave", objects);

		float[] bb = flag.getMesh().getBoundingBox();
		float width = Math.abs(bb[0]-bb[1]);
		Camera cam = world.getCamera();
		float moveout;
		if(flagModeSetting.equals(Settings.FLAG_MODE_FULLSCREEN)){
			pole = null;
			moveout = 30; 
			cam.setPositionToCenter(flag);
			cam.moveCamera(Camera.CAMERA_MOVEOUT, moveout);
			//		cam.setYFOV(cam.convertRADAngleIntoFOV((float) Math.atan(height/(2*moveout))));
			cam.setFOV(cam.convertRADAngleIntoFOV((float) Math.atan(width/(2*moveout))));
			cam.lookAt(flag.getTransformedCenter());
		}else{
			pole = (Scene.findObject("pole", objects));
			float height = Math.abs(bb[2]-bb[3]);
			moveout = 35; 
			cam.setPosition(0, 0, 0);
			cam.moveCamera(Camera.CAMERA_MOVEOUT, moveout);
			cam.moveCamera(Camera.CAMERA_MOVEDOWN, height-5);
			cam.setFOV(cam.convertRADAngleIntoFOV((float) Math.atan(width/(2*moveout))));
			cam.lookAt(new SimpleVector(width/2, -height/2, 0));			
		}

		Light sun = new Light(world);
		SimpleVector sv = new SimpleVector();
		sv.set(flag.getTransformedCenter());
		sv.y += 100;
		sv.x -= 100;
		sv.z -= 30;
		sun.setPosition(sv);
		//		sun.disable();

		MemoryHelper.compact();

	}


	private String getAppropriateFlag(String texture, int screenWidth, int screenHeight){
		if (screenWidth > screenHeight || prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_SKY).equals(Settings.FLAG_MODE_SKY))
			texture = FlagManager.toLandscape(texture);
		else
			texture = FlagManager.toPortrait(texture);
		return texture;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs,	String key) {
		Log.i("FlagRenderer", "Preferece Changed: "+ key); 
		if(key.equals(Settings.SKY_MODE_BACKGROUND_IMAGE))
			if (prefs.getString(key, "sky_night").equals(Settings.SKY_USER_BACKGROUND))
				userBackPrefUpdated = true;
			else{
				if(TextureManager.getInstance().containsTexture(Settings.SKY_USER_BACKGROUND))
					TextureManager.getInstance().removeAndUnload(Settings.SKY_USER_BACKGROUND, framebuffer);
			}
		else if(key.equals(Settings.FLAG_MODE_SETTING)){
			modePreferenceUpdated = true;
			imagePreferenceUpdated = true;
		}
		else if(key.equals(Settings.DAY_TIME_SKY_BACKGROUND) && prefs.getBoolean(key, true))
			dayTimeUpdated = true;
		else if(key.equals(Settings.FLAG_IMAGE_SETTING)
				|| key.equals(Settings.SINGLE_FLAG_IMAGE_SETTING)
				|| key.equals(Settings.MULTIPLE_FLAG_IMAGE_SETTING)
				|| key.equals(Settings.MULTIPLE_FLAG_TIME_MIN))
			imagePreferenceUpdated = true;
	}

	public static void updateDayTimeBackground() {
		dayTimeUpdated = true;	
	}

}
