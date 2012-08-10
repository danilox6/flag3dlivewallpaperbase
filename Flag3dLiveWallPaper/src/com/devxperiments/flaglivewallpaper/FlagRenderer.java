package com.devxperiments.flaglivewallpaper;

import java.lang.reflect.Field;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.devxperiments.flaglivewallpaper.settings.Settings;
import com.jbrush.ae.Animator;
import com.jbrush.ae.EditorObject;
import com.jbrush.ae.LightData;
import com.jbrush.ae.Scene;
import com.threed.jpct.Camera;
import com.threed.jpct.Config;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import net.rbgrn.android.glwallpaperservice.GLWallpaperService;

public class FlagRenderer implements GLWallpaperService.Renderer, OnSharedPreferenceChangeListener{
 
	FrameBuffer fb;
	World world;
	RGBColor backgroundColor = RGBColor.BLACK;
	Vector<EditorObject> objects;
	Vector<LightData> lights; 
	Context master;
	Context context;
	
	Light sun;
	float speed;
	int zoom;
	
	static FlagRenderer instance;
	
	public static FlagRenderer getInstance(Context context){
		if (instance==null)
			instance = new FlagRenderer(context);
		return instance;
	}
	
	public static FlagRenderer getInstance(){
		return instance;
	}
	
	public static void setInstance(FlagRenderer renderer){
		instance = renderer;
	}
	
	private FlagRenderer(Context context) {
		this.context = context;
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		int color = prefs.getInt(Settings.BACKGROUND_COLOR, 0xff000000);
		
		zoom = 18;
		zoom = 100 - prefs.getInt(Settings.CAMERA_ZOOM, 80);
		backgroundColor = new RGBColor(Color.red(color), Color.green(color), Color.blue(color));
//		 if (master != null) {
//				copy(master);
//		}
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onDrawFrame(GL10 gl) {
		
		fb.clear();
		fb.blit(TextureManager.getInstance().getTexture("flagt"),0,0,0,0,512,1024,FrameBuffer.OPAQUE_BLITTING);
		
		world.renderScene(fb);
		
		Animator.EnableAnimations();

		world.draw(fb);
		fb.display();
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if (fb != null) {
			fb.dispose();
		}
		fb = new FrameBuffer(gl, width, height);
		
		
//		if (master == null) {

				draw(width, height);
//			if (master == null) {
//				Logger.log("Saving master Activity!");
//				master = context;
//			}
//		}

	}

	public void release() {
		
		// TODO Auto-generated method stub
	}
	
	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		zoom = 100 - prefs.getInt(Settings.CAMERA_ZOOM, 80);
		draw(0, 0);		
	}

	private void draw(int screenWidth, int screenHeight){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		world = new World();
	
		
		
		lights = new Vector<LightData>();
		
		int color = prefs.getInt(Settings.BACKGROUND_COLOR, 0xff000000);
		backgroundColor = new RGBColor(Color.red(color), Color.green(color), Color.blue(color));
        
//		world.setAmbientLight(Color.red(color), Color.green(color), Color.blue(color));
		
		AssetManager assetManager = context.getAssets();
		objects = Scene.loadSerializedLevel("flagh.txt", objects, lights, null,null, world, assetManager);
//		objects = Scene.loadSerializedLevel("flagh.txt", objects, world, assetManager);
        
		
        Object3D flag = Scene.findObject("flagh0", objects);
//        flag.scale(30);
//        flag.getMesh().
        
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        screenHeight =  metrics.heightPixels;
        screenWidth =   metrics.widthPixels;
        
        
//        Object3D o1 = Object3D.createDummyObj();
//        o1.align(flag);
//        o1.translate(height/2, 0, 0);
        
//        while( flag.rayIntersectsAABB(new SimpleVector(screenWidth/2, screenHeight/2, 0), new SimpleVector(1, 0, 0)) == Object3D.RAY_MISSES_BOX)
//        	flag.scale(5);	
        
//        flag.translate(0, 0, 0);
//        flag.scale(15);
        
        Animator.Play(flag, "wave", objects);
       
        
		flag.setTexture("flagt");
		
		float[] bb = flag.getMesh().getBoundingBox();
		
		float height = Math.abs(bb[2]-bb[3]);
		float width = Math.abs(bb[0]-bb[1]);
		
		sun = new Light(world);
//		sun.setIntensity(Color.red(color), Color.green(color), Color.blue(color) );
		
		
//		float xFOV = width/2;
//		float yFOV = (float) (2.0f * Math.atan( Math.tan(xFOV * 0.5f) * height/width ));
		
//		Config.autoMaintainAspectRatio = false;
		
		float moveout = 35; 
		Camera cam = world.getCamera();
		cam.moveCamera(Camera.CAMERA_MOVEOUT, moveout);
		cam.setFOV(cam.convertRADAngleIntoFOV((float) Math.atan(width/(2*moveout))));
//		cam.setYFOV(cam.convertRADAngleIntoFOV((float) Math.atan(height/(2*moveout))));
		cam.lookAt(flag.getTransformedCenter());
		
//		flag.align(cam);
		
	float fov =	cam.getFOV();
		float tfov = cam.getYFOV();
		
		
		SimpleVector sv = new SimpleVector();
		sv.set(flag.getTransformedCenter());
		sv.y += 100;
		sv.x -= 100;
		sv.z -= 5;
		sun.setPosition(sv);
//		sun.disable();
		
	
		
		MemoryHelper.compact();
	}
	
	public Context getContext() {
		return context;
	}

}
