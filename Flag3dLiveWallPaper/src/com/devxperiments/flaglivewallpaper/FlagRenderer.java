package com.devxperiments.flaglivewallpaper;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.jbrush.ae.Animator;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.LightData;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.BitmapHelper;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer, OnSharedPreferenceChangeListener{

	private FrameBuffer framebuffer = null;
	private World world = null;
	private Object3D flag = null;
	private Timer updateFlagTimer = new Timer(true);
	private TimerTask updateFlagTask = null;
	private String texture = null;
	private SharedPreferences prefs;
	private boolean isSingleFlagSet, imagePreferenceUpdated, modePreferenceUpdated;


	public FlagRenderer() {
		prefs = PreferenceManager.getDefaultSharedPreferences(FlagWallpaperService.context);
		prefs.registerOnSharedPreferenceChangeListener( this);
		imagePreferenceUpdated = false;
		modePreferenceUpdated = false;
		isSingleFlagSet = false;
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onDrawFrame(GL10 gl) {
		if(texture!=null){
			//			changeFlagTexture(texture);
			flag.setTexture(texture);
			texture = null;
		}
		framebuffer.clear();
		framebuffer.blit(TextureManager.getInstance().getTexture("sky"),0,0,0,0,512,512,FrameBuffer.OPAQUE_BLITTING);
		world.renderScene(framebuffer);
		Animator.EnableAnimations();
		world.draw(framebuffer);
		framebuffer.display();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, final int screenWidth, final int screenHeight) {
		if (framebuffer != null) {
			framebuffer.dispose();
		}

		framebuffer = new FrameBuffer(gl, screenWidth, screenHeight);
		if(world==null || modePreferenceUpdated){
			modePreferenceUpdated = false;
			draw(screenWidth, screenHeight);
			isSingleFlagSet = false;
		}

		String flagSetting = prefs.getString(Settings.FLAG_IMAGE_SETTING, Settings.SINGLE_FLAG_IMAGE_SETTING);

		if((flagSetting.equals(Settings.SINGLE_FLAG_IMAGE_SETTING) && !isSingleFlagSet) ||
				(flagSetting.equals(Settings.SINGLE_FLAG_IMAGE_SETTING) && imagePreferenceUpdated)){
			
			if(updateFlagTask != null)
				updateFlagTask.cancel();
			updateFlagTask = null;
			String texture = prefs.getString(Settings.SINGLE_FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
			flag.setTexture(getAppropriateFlag(texture, screenWidth, screenHeight));
			
			imagePreferenceUpdated = false;
			isSingleFlagSet = true;
			//			changeFlagTexture(texture, screenWidth, screenHeight);
		}else if(!isSingleFlagSet && (updateFlagTask == null || imagePreferenceUpdated)){

			isSingleFlagSet = false;
			imagePreferenceUpdated = false;
			
			if(updateFlagTask != null)
				updateFlagTask.cancel();

			final List<String> textures = Settings.parseMultiFlagPreference(prefs.getString(Settings.MULTIPLE_FLAG_IMAGE_SETTING, null));

			changeFlagTexture(textures.get(0), screenWidth, screenHeight);

			updateFlagTask = new TimerTask() {
				int i = 1;
				@Override
				public void run() {
					//					Log.e("TEXTURE", textures.get(i));
					//					if(i-2>=0)
					//						TextureManager.getInstance().removeAndUnload(textures.get(i-2), framebuffer);
					texture = getAppropriateFlag(textures.get(i), screenWidth, screenHeight);
					//					texture = loadFlagTexture(textures.get(i), screenWidth, screenHeight);
					i = ++i%textures.size();
					//					TextureManager.getInstance().flush();
				}
			};

			int timeMillis = prefs.getInt(Settings.MULTIPLE_FLAG_TIME_SEC, 5)*1000;
			//			updateFlagTimer.schedule(updateFlagTask, 5000);
			updateFlagTimer.schedule(updateFlagTask, timeMillis, timeMillis);
		}


	}

	public void release() {
		if (framebuffer != null) {
			framebuffer.dispose();
		}
		if(updateFlagTask!=null)
			updateFlagTask.cancel();
		updateFlagTimer.cancel();
	}

	private void draw(int screenWidth, int screenHeight){
		world = new World();

		Vector<EditorObject> objects = new Vector<EditorObject>();
		Vector<LightData> lights = new Vector<LightData>();

		
		String flagModeSetting = prefs.getString(Settings.FLAG_MODE_SETTING, Settings.FLAG_MODE_FULLSCREEN);
		
		AssetManager assetManager = FlagWallpaperService.context.getAssets();
		objects = Scene.loadSerializedLevel(flagModeSetting+".txt", objects, lights, null,null, world, assetManager);

		flag = Scene.findObject(flagModeSetting+"0", objects);
		Animator.Play(flag, "wave", objects);
		
		float[] bb = flag.getMesh().getBoundingBox();
		float width = Math.abs(bb[0]-bb[1]);
		Camera cam = world.getCamera();
		float moveout;
		if(flagModeSetting.equals(Settings.FLAG_MODE_FULLSCREEN)){
			moveout = 30; 
			cam.setPositionToCenter(flag);
			cam.moveCamera(Camera.CAMERA_MOVEOUT, moveout);
			//		cam.setYFOV(cam.convertRADAngleIntoFOV((float) Math.atan(height/(2*moveout))));
			cam.setFOV(cam.convertRADAngleIntoFOV((float) Math.atan(width/(2*moveout))));
			cam.lookAt(flag.getTransformedCenter());
		}else{
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

	public void changeFlagTexture(String texture, int screenWidth, int screenHeight){
		texture = getAppropriateFlag(texture, screenWidth, screenHeight);
		texture = loadFlagTexture(texture);
		flag.setTexture(texture);
	}

	public void changeFlagTexture(String texture){
		texture = loadFlagTexture(texture);
		flag.setTexture(texture);
	}

	public String loadFlagTexture(String texture){

		if(!TextureManager.getInstance().containsTexture(texture)){
			//			TextureManager.getInstance().flush();
			TextureManager.getInstance().addTexture(texture, new Texture(BitmapHelper.convert(FlagWallpaperService.context.getResources().getDrawable(FlagManager.getFlagId(texture)))));
			TextureManager.getInstance().compress();
		}
		FlagManager.release();
		return texture;
	}

	public String getAppropriateFlag(String texture, int screenWidth, int screenHeight){
		if (screenWidth > screenHeight)
			texture = FlagManager.toLandscape(texture);
		else
			texture = FlagManager.toPortrait(texture);
		return texture;
	}

	public String loadFlagTexture(String texture, int screenWidth, int screenHeight){
		texture = getAppropriateFlag(texture, screenWidth, screenHeight);
		return loadFlagTexture(texture);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,	String key) {
		if(key.equals(Settings.FLAG_MODE_SETTING))
			modePreferenceUpdated = true;
		else
			imagePreferenceUpdated = true;
	}

}
