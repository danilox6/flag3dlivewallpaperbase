package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;

import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;
import com.devxperiments.flaglivewallpaper.settings.FlagAdapter.FlagItem;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;
import com.mobeta.android.dslv.DragSortListView.RemoveListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MultipleWallpaperChooser extends Activity implements OnClickListener{

	private FlagAdapter adapter;
	private SharedPreferences prefs;
	private ImageView flagPreview;
	private Button btnOk;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.multiple_wallpaper_chooser);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		flagPreview = (ImageView) findViewById(R.id.imgPreview);

		DragSortListView flagList = (DragSortListView) findViewById(R.id.dragList);
		adapter = new  FlagAdapter(this, getFlagItemList(FlagManager.getPortraitFlagIds()), flagPreview);
		flagList.setAdapter(adapter);
		flagList.setDropListener(new DropListener() {

			@Override
			public void drop(int from, int to) {
				FlagItem item = adapter.getItem(from);
				adapter.remove(item);
				int i = adapter.getFirstUncheckedIndex();
				if(to > i)
					to = i;
				adapter.insert(item, to);
			}

		});

		flagList.setRemoveListener(new RemoveListener() {

			@Override
			public void remove(int which) {
				adapter.remove(adapter.getItem(which));
			}
		});

		flagList.setDivider(null);
		//		flagList.setSelector(R.drawable.list_selector_background);

		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		((Button) findViewById(R.id.btnCancel)).setOnClickListener(this);

	}

	private List<FlagItem> getFlagItemList(List<Integer> list){
		List<FlagItem> items = new ArrayList<FlagItem>();
		List<Integer> flagPref = Settings.parseMultiFlagPreference(prefs.getString(Settings.MULTIPLE_FLAG_IMAGE_SETTING, "none"));;
		for(Integer id : list)
			items.add(new FlagItem(id, flagPref==null || flagPref.contains(id)));
		return items;
	}

	@Override
	public void onClick(View v) {
		if(((Button) v).equals(btnOk)){
			prefs.edit().putString(Settings.MULTIPLE_FLAG_IMAGE_SETTING, adapter.getCheckedFlagPref()).commit();
		}
		finish();
	}


}
