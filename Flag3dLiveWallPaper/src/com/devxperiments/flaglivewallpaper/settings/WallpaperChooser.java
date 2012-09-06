package com.devxperiments.flaglivewallpaper.settings;

import java.util.List;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;
import com.droid4you.util.cropimage.CropImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
import android.widget.TextView;

public class WallpaperChooser extends Activity implements OnClickListener{
	
	
	private List<Integer> pics;
	private ImageView imageView;
	private Button btnOk;
	private TextView txtInfo;
	private SharedPreferences prefs;
	private int thumbHeight;
	private Bitmap[] thumbCache;
	private static String selectedTexture;
	private boolean skyBackground;
	private OnClickListener listener = null;
	private final int PICKED_IMAGE = 0;
	private final int CROPPED_IMAGE = 1;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wallpaper_chooser);
		
		
		skyBackground = getIntent().getBooleanExtra(Settings.SKY_MODE_BACKGROUND_IMAGE, false);

		if(skyBackground){
			pics = FlagManager.getSkyBackgroundIds();
			listener = new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
					intent.setType("image/*");
					startActivityForResult(intent, PICKED_IMAGE);
				}
			};
		}
		else
			pics = FlagManager.getPortraitFlagIds();

		txtInfo = (TextView) findViewById(R.id.txtImageInfo);

		Gallery gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(new FlagAdapter(this));
		gallery.setCallbackDuringFling(false);
		
		

		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				int flagId = pics.get(position);
				
				if(skyBackground && flagId == R.drawable.sys_btn_add){
					
//					if(prefs.getString(Settings.SKY_MODE_BACKGROUND_IMAGE, "sky_day").equals(Settings.SKY_USER_BACKGROUND)){
//						
//					}
					
					txtInfo.setVisibility(View.VISIBLE);
					txtInfo.setText("Clicca l'immagine per caricare una foto"); //FIXME externalizzare
					
					Bitmap bitmap = BitmapUtils.getUserBitmap(WallpaperChooser.this);
					if(bitmap == null)
						imageView.setImageResource(flagId);
					else
						imageView.setImageBitmap(bitmap);
					
					imageView.setOnClickListener(listener);
					
				}else{
					txtInfo.setVisibility(View.GONE);
				
					imageView.setOnClickListener(null);
					
					if(!skyBackground){
						selectedTexture = FlagManager.getFlagNameById(flagId);
						if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
							flagId = FlagManager.toLandscape(flagId);
					}
					imageView.setImageResource(flagId);
				}
				
				imageView.setTag(flagId);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});


		imageView = (ImageView)findViewById(R.id.imgFlag);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		if(selectedTexture == null)
			selectedTexture = prefs.getString(Settings.SINGLE_FLAG_IMAGE_SETTING, FlagManager.getDefaultFlag());
		String texture = selectedTexture;
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			texture = FlagManager.toLandscape(texture);
		Log.e("TEXTURE PAGLIACCIA", texture);
		imageView.setImageResource(FlagManager.getFlagId(texture));
		imageView.setTag(FlagManager.getFlagId(texture));

		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		((Button) findViewById(R.id.btnCancel)).setOnClickListener(this);

		Display display = getWindowManager().getDefaultDisplay();
		thumbHeight = (display.getHeight()*30/100) - 5;

	}


	@Override
	public void onClick(View v) {
		if(((Button) v).equals(btnOk)){
			int flagId = (Integer) imageView.getTag();
			String texture;
			if(flagId == R.drawable.sys_btn_add)
				texture = Settings.SKY_USER_BACKGROUND;
			else
				texture = FlagManager.getFlagNameById(flagId);
			prefs.edit().putString(skyBackground?Settings.SKY_MODE_BACKGROUND_IMAGE : Settings.SINGLE_FLAG_IMAGE_SETTING, texture).commit();
		}else
		selectedTexture = null;
		finish();
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try{
		if(resultCode == RESULT_OK){
			switch (requestCode) {
			case PICKED_IMAGE:
				Uri selectedImage = data.getData();
				Intent intent = new Intent(this, CropImage.class);
				intent.putExtra("image-path", selectedImage.toString());
				intent.putExtra("return-data", true);
				startActivityForResult(intent, CROPPED_IMAGE);
				break;

			case CROPPED_IMAGE:
				Bitmap bitmap = (Bitmap) data.getParcelableExtra("data");
				BitmapUtils.setUserBitmap(this, bitmap);
				imageView.setImageBitmap(bitmap);
				break;
			}
		}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

	public class FlagAdapter extends BaseAdapter {

		private Context context;
		int imageBackground;

		public FlagAdapter(Context c) {
			thumbCache = new Bitmap[pics.size()];
			context = c;
			TypedArray array = obtainStyledAttributes(R.styleable.GalleryStyle);
			imageBackground = array.getResourceId(R.styleable.GalleryStyle_android_galleryItemBackground, 1);
			array.recycle();
		}

		@Override
		public int getCount() {
			return pics.size();
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
				thumbCache[position] = BitmapUtils.scaleCenterCrop(BitmapFactory.decodeResource(getResources(), pics.get(position)),thumbHeight,thumbHeight);
			}
			imageView.setImageBitmap(thumbCache[position]);
			imageView.setAdjustViewBounds(true);
			imageView.setBackgroundResource(imageBackground);
			return imageView;
		}

	}

	
}