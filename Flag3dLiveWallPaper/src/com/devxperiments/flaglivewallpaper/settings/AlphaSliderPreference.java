package com.devxperiments.flaglivewallpaper.settings;

import com.devxperiments.flaglivewallpaper.R;

import it.unisannio.aroundme.widgets.SliderView;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

public class AlphaSliderPreference extends DialogPreference{

	public static float DEFAULT_ALPHA = 3f;
	private SliderView slider;
	private float value = DEFAULT_ALPHA;

	public AlphaSliderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlphaSliderPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
	}

	@Override
	protected View onCreateDialogView() {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		slider = (SliderView) inflater.inflate(R.layout.slider_view_alpha, null);
		return slider;
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		slider.setConvertedValue(value);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {

		super.onSetInitialValue(restorePersistedValue, defaultValue);
		if (restorePersistedValue && shouldPersist()) 
			value = getPersistedFloat(DEFAULT_ALPHA);

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		super.onClick(dialog, which);
		if(which == Dialog.BUTTON_POSITIVE) {
			value = slider.getConvertedValue();
			callChangeListener(value);

			if(shouldPersist())
				persistFloat(value);
		}

	}
}
