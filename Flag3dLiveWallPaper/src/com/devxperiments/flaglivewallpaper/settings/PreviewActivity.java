package com.devxperiments.flaglivewallpaper.settings;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class PreviewActivity extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.preview);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		ImageView preview = (ImageView) findViewById(R.id.imgPreviewFlag);
		int flagId =getIntent().getIntExtra("flagId", -1);
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
			flagId = FlagManager.toPortrait(flagId);
		else
			flagId = FlagManager.toLandscape(flagId);
		preview.setImageResource(flagId);
		
		preview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		Toast.makeText(this, R.string.strClosePreview , Toast.LENGTH_SHORT).show();
	}
}
