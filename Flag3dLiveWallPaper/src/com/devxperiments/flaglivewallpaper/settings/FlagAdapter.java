package com.devxperiments.flaglivewallpaper.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;

import com.devxperiments.flaglivewallpaper.settings.FlagAdapter.FlagItem;
import com.devxperiments.flaglivewallpaper.FlagManager;
import com.devxperiments.flaglivewallpaper.R;

public class FlagAdapter extends ArrayAdapter<FlagItem> implements OnTouchListener{
	ImageView flagPreview;
	int thumbHeight;
	private SparseArray<Bitmap> thumbCache;
	
	public FlagAdapter(Context context, List<FlagItem> objects, ImageView flagPreview) {
		super(context, R.layout.flag_list_item, objects);
	    thumbHeight = context.getResources().getDrawable(R.drawable.list_tile_normal).getMinimumHeight()-6;
	    thumbCache = new SparseArray<Bitmap>();
	    this.flagPreview = flagPreview;
	}
	
	static class ViewHolder{
		CheckedTextView checkBox;
		ImageView imageView;
		ImageView drag;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if(convertView == null){
			LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
			view = inflater.inflate(R.layout.flag_list_item, null);

			final ViewHolder viewHolder = new ViewHolder();
			viewHolder.checkBox = (CheckedTextView) view.findViewById(R.id.checkBox);
			viewHolder.imageView = (ImageView) view.findViewById(R.id.imgListFlag);
			viewHolder.drag = (ImageView) view.findViewById(R.id.drag);

			viewHolder.checkBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CheckedTextView checkbox = (CheckedTextView) v;
					checkbox.toggle();
					FlagItem flagItem = (FlagItem)checkbox.getTag();
					flagItem.checked = checkbox.isChecked();
					remove(flagItem);
					insert(flagItem, getFirstUncheckedIndex());
				}
			});
			
			viewHolder.drag.setOnTouchListener(this);
			viewHolder.checkBox.setOnTouchListener(this);
			
			view.setTag(viewHolder);
			
			view.setOnTouchListener(this);
		}else{
			view = convertView;
		}
		
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		FlagItem flagItem = getItem(position);
		viewHolder.checkBox.setTag(flagItem);
		viewHolder.checkBox.setChecked(flagItem.checked);
		int flagId = flagItem.id;
		if (thumbCache.get(flagId) == null){
			thumbCache.put(flagId, WallpaperChooser.scaleCenterCrop(BitmapFactory.decodeResource(getContext().getResources(), flagId),thumbHeight,thumbHeight));
		}
		viewHolder.imageView.setImageBitmap(thumbCache.get(flagId));
		if(flagItem.checked)
			viewHolder.drag.setEnabled(true);
		else
			viewHolder.drag.setEnabled(false);
		return view;
	}
	
	static class FlagItem{
		int id;
		boolean checked = false;

		public FlagItem(int id, boolean checked) {
			this.id = id;
			this.checked = checked;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof FlagItem) {
				FlagItem flagItem = (FlagItem) o;
				return flagItem.id == this.id;
			}
			return false;
		}
		
		
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int flagId;
		CheckedTextView checkbox;
		if (v instanceof CheckedTextView) 
			checkbox = (CheckedTextView) v;
		else{
			if (v instanceof ImageView)
				v = (View) v.getParent();
			checkbox = ((ViewHolder)v.getTag()).checkBox;
		}
		flagId = ((FlagItem) checkbox.getTag()).id;
		if(getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			flagId = FlagManager.toLandscape(flagId);
		flagPreview.setImageResource(flagId);
		return false;
	}
	
	public int getFirstUncheckedIndex(){
		int i = 0;
		while(i<getCount() && getItem(i).checked)
			i++;
		return i;
	}
	
	public List<Integer> getCheckedFlagIds(){
		List<Integer> list = new ArrayList<Integer>();
		int i = 0;
		while(i<getCount() && getItem(i).checked)
			list.add(getItem(i++).id);
		return list;
	}
	
	public String getCheckedFlagPref(){
		String pref = "";
		int i = 0;
		while(i<getCount() && getItem(i).checked)
			pref+=getItem(i++).id+"-";
		return pref;
	}
	
	
}