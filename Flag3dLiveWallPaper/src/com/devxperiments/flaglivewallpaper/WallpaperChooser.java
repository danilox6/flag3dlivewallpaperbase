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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;

public class WallpaperChooser extends Activity implements OnClickListener{
    private List<Integer> flags;
    private ImageView imageView;
    private Button btnOk, btnCancel;
    private SharedPreferences prefs;
    private int heigth;
    
    public static final String FLAG_IMAGE_SETTING = "flag";
    
	/** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        flags = FlagManager.getPortraitFlagIds();
        
        setContentView(R.layout.wallpaper_chooser);
        
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));
        
        imageView = (ImageView)findViewById(R.id.imgFlag);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String texture = prefs.getString(FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
        imageView.setImageResource(FlagManager.getFlagId(texture));
        imageView.setTag(FlagManager.getFlagId(texture));
        
        gallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,	long arg3) {
				
				int flagId = flags.get(position);
				
				if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
					flagId = FlagManager.toLandscape(flagId);
				
				imageView.setImageResource(flagId);
				imageView.setTag(flags.get(position));
			}
        	
        });
        
        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
        
        Display display = getWindowManager().getDefaultDisplay();
        heigth = display.getHeight();
        
    }
    
    public class ImageAdapter extends BaseAdapter {

    	private Context ctx;
    	int imageBackground;
    	
    	public ImageAdapter(Context c) {
			ctx = c;
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
    		ImageView imageView = new ImageView(ctx);
    		int s = (heigth*30/100) - 5;
    		imageView.setImageBitmap(scaleCenterCrop(BitmapFactory.decodeResource(getResources(), flags.get(position)),s,s));
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
			Log.w("AAA", texture);
			prefs.edit().putString(FLAG_IMAGE_SETTING, texture).commit();
		}
		finish();
	}
}