package com.devxperiments.flaglivewallpaper;

import java.util.List;

import com.devxperiments.flaglivewallpaper.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;

public class WallpaperChooser extends Activity implements OnClickListener{
    private List<Integer> flags;
    private ImageView imageView;
    private Button btnOk;
    private SharedPreferences prefs;
    private int heigth;
    private Bitmap[] thumbCache;
    private static String selectedTexture;
    
    public static final String FLAG_IMAGE_SETTING = "flag";
    
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        flags = FlagManager.getPortraitFlagIds();
        
        setContentView(R.layout.wallpaper_chooser);
        
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new FlagAdapter(this));
        gallery.setCallbackDuringFling(false);

        gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

        	@Override
        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		int flagId = flags.get(position);

        		selectedTexture = FlagManager.getFlagNameById(flagId);

        		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        			flagId = FlagManager.toLandscape(flagId);

				imageView.setImageResource(flagId);
				imageView.setTag(flags.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

        
        imageView = (ImageView)findViewById(R.id.imgFlag);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        if(selectedTexture == null)
        	selectedTexture = prefs.getString(FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
        String texture = selectedTexture;
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        	texture = FlagManager.toLandscape(texture);
        imageView.setImageResource(FlagManager.getFlagId(texture));
        imageView.setTag(FlagManager.getFlagId(texture));
        
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        ((Button) findViewById(R.id.btnCancel)).setOnClickListener(this);
        
        Display display = getWindowManager().getDefaultDisplay();
        heigth = display.getHeight();
        
    }
    
    public class FlagAdapter extends BaseAdapter {

    	private Context context;
    	int imageBackground;
    	
    	public FlagAdapter(Context c) {
    		thumbCache = new Bitmap[flags.size()];
			context = c;
			TypedArray array = obtainStyledAttributes(R.styleable.GalleryStyle);
			imageBackground = array.getResourceId(R.styleable.GalleryStyle_android_galleryItemBackground, 1);
			array.recycle();
		}

		@Override
    	public int getCount() {
    		return flags.size();
    	}

		@Override
		public Object getItem(int position) {
			return position;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		ImageView imageView;
    		if(convertView == null)
    			imageView = new ImageView(context);
    		else
    			imageView = (ImageView) convertView;
    		
    		if (thumbCache[position] == null){
    			int scaledHeight = (heigth*30/100) - 5;
    			thumbCache[position] = scaleCenterCrop(BitmapFactory.decodeResource(getResources(), flags.get(position)),scaledHeight,scaledHeight);
    		}
    		imageView.setImageBitmap(thumbCache[position]);
    		imageView.setAdjustViewBounds(true);
    		imageView.setBackgroundResource(imageBackground);
    		return imageView;
    	}
    	
    	private Bitmap scaleCenterCrop(Bitmap source, int newHeight, int newWidth) {
    	    int sourceWidth = source.getWidth();
    	    int sourceHeight = source.getHeight();

    	    // Compute the scaling factors to fit the new height and width, respectively.
    	    // To cover the final image, the final scaling will be the bigger 
    	    // of these two.
    	    float xScale = (float) newWidth / sourceWidth;
    	    float yScale = (float) newHeight / sourceHeight;
    	    float scale = Math.max(xScale, yScale);

    	    // Now get the size of the source bitmap when scaled
    	    float scaledWidth = scale * sourceWidth;
    	    float scaledHeight = scale * sourceHeight;

    	    // Let's find out the upper left coordinates if the scaled bitmap
    	    // should be centered in the new size give by the parameters
    	    float left = (newWidth - scaledWidth) / 2;
    	    float top = (newHeight - scaledHeight) / 2;

    	    // The target rectangle for the new, scaled version of the source bitmap will now
    	    // be
    	    RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

    	    // Finally, we create a new bitmap of the specified size and draw our new,
    	    // scaled bitmap onto it.
    	    Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, source.getConfig());
    	    Canvas canvas = new Canvas(dest);
    	    canvas.drawBitmap(source, null, targetRect, null);

    	    return dest;
    	}
    	
    }

	@Override
	public void onClick(View v) {
		if(((Button) v).equals(btnOk)){
			String texture = FlagManager.getFlagNameById((Integer) imageView.getTag());
			prefs.edit().putString(FLAG_IMAGE_SETTING, texture).commit();
		}else
			FlagManager.release();
		selectedTexture = null;
		finish();
	}
}