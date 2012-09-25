/* AroundMe - Social Network mobile basato sulla geolocalizzazione
 * Copyright (C) 2012 AroundMe Working Group
 *   
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.unisannio.aroundme.widgets;

import com.devxperiments.flaglivewallpaper.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * Widget composito, utilizzato nelle impostazioni dei filtri.
 * 
 * Esso &egrave; realizzato usando insieme una {@link SeekBar} e un {@link TextView} per visualizzare
 * una versione formattata dei valori selezionati sulla SeekBar. Il widget definisce alcuni attributi XML,
 * che permettono di utilizzarlo direttamente all'interno dei layout.
 * 
 * <p>Esempio di utilizzo:
 * <pre><code>
 * &lt;it.unisannio.aroundme.widgets.SliderView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aroundme="http://schemas.android.com/apk/res/it.unisannio.aroundme"
	    android:layout_width="match_parent"
	    android:layout_height="wrap_content" 
	    aroundme:multiplier="100"
	    aroundme:minValue="1"
	    aroundme:maxValue="50"
	    aroundme:conversion="0.001"
	    aroundme:format="{1,choice,0#Off|0&lt;{1,number} m|999&lt;{2,number,.1} Km}"
	    /&gt;
 * </code></pre>
 * </p>
 * 
 * <p>Il widget ha diversi parametri configurabili:
 * <ul>
 * 		<li><strong>Valore minimo</strong> ({@code aroundme:minValue}, {@link #setMinValue(int)}): valore che assume il widget quando l'indicatore
 * della barra &egrave; sullo zero.</li>
 * 		<li><strong>Valore massimo</strong> ({@code aroundme:maxValue}, {@link #setMaxValue(int)}): valore che assume il widget quando l'indicatore
 * della barra &egrave; a fine corsa.</li>
 * 		<li><strong>Valore iniziale</strong> ({@code aroundme:value}, {@link #setValue(int)}): valore assunto dal widget prima di essere manipolato
 * dall'utente.</li>
 * 		<li><strong>Moltiplicatore</strong> ({@code aroundme:multiplier}, {@link #setMultiplier(int)}: valore di ogni incremento per gli spostamenti 
 * della barra</li>
 * 		<li><strong>Fattore di conversione</strong> ({@code aroundme:conversion}, {@link #setConversion(float)}): un coefficiente che pu&ograve;
 * essere usato per convertire il valore misurato dalla barra in un altro formato.</li>
 * 		<li><strong>Formato testo</strong> ({@code aroundme:format}, {@link #setFormat(String)}): una stringa di formattazione secondo la 
 * sintassi di {@link java.text.MessageFormat}. I parametri disponibili sono, nell'ordine: valore non moltiplicato ({@link #getValue()}), 
 * valore moltiplicato ({@link #getMultipliedValue()}) e valore convertito ({@link #getConvertedValue()}).</li> 
 * </ul>
 * </p>
 *
 * @author Michele Piccirillo <michele.piccirillo@gmail.com>
 */
public class SliderView extends LinearLayout implements OnSeekBarChangeListener {
	
	/**
	 * Listener che viene notificato qualora il valore indicato dalla barra inclusa nel widget cambi.
	 * 
	 * @see SliderView#setOnChangeListener(OnChangeListener)
	 * 
	 * @author Michele Piccirillo <michele.piccirillo@gmail.com>
	 */
	public static interface OnChangeListener {
		
		/**
		 * Metodo che viene notificato nel caso in cui cambi il valore indicato dal widget.
		 * 
		 * @param view il widget su cui &egrave; avvenuto il cambiamento
		 */
		void onSliderChanged(SliderView view);
	}
	
	private int minValue = 0;
	private int maxValue = 0;
	
	private float multiplier = 1;
	private float conversion = 1.0f;
	private String format = "{0}";
	
	private SeekBar seekBar;
	private TextView textView;
	
	private OnChangeListener listener;

	public SliderView(Context context) {
		super(context);
		initialize(null);
	}

	public SliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(attrs);
	}
	
	private void initialize(AttributeSet attrs) {
		LayoutInflater inflater = (LayoutInflater)   getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.slider_view, this);
		
		this.seekBar = (SeekBar) findViewById(R.id.slider_view_seekbar);
		this.textView = (TextView) findViewById(R.id.slider_view_text);
		seekBar.setOnSeekBarChangeListener(this);
		
		
		if(attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.SliderView);
			
			multiplier = a.getFloat(R.styleable.SliderView_multiplier, 1);
			conversion = a.getFloat(R.styleable.SliderView_conversion, 1.0f);
			
			format = a.getString(R.styleable.SliderView_format);
			
			if(format == null)
				format = "{0}";
			
			minValue = a.getInt(R.styleable.SliderView_minValue, 0);
			setMaxValue(a.getInt(R.styleable.SliderView_maxValue, 100));
			setValue(a.getInt(R.styleable.SliderView_value, 0));
			
			a.recycle();
		}
	}
	
	private void updateText() {
//		textView.setText(MessageFormat.format(format, getValue(), getMultipliedValue(), getConvertedValue()));
		textView.setText((getValue()==0)?"Off":(getValue()*100)/maxValue+" %");
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		updateText();
			
		if(listener != null)
			listener.onSliderChanged(this);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {	
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	/**
	 * Restituisce il valore assunto dallo slider (senza fattori di moltiplicazione applicati).
	 * 
	 * @return il valore assunto dallo slider
	 */
	public int getValue() {
		return seekBar.getProgress() + minValue;
	}
	
	/**
	 * Imposta il valore assunto dallo slider (senza fattori di moltiplicazione applicati).
	 * 
	 * @param value il valore che lo slider deve assumere
	 */
	public void setValue(int value) {
		seekBar.setProgress(value - minValue);
		updateText();
	}
	
	/**
	 * Restituisce il fattore che viene applicato agli incrementi della barra per ottenere il valore mostrato nel testo.
	 * 
	 * @return il fattore di moltiplicazione in uso
	 * @see #getMultipliedValue()
	 */
	public float getMultiplier() {
		return multiplier;
	}
	
	/**
	 * Imposta il fattore che viene applicato agli incrementi della barra per ottenere il valore mostrato nel testo.
	 * 
	 * @param value il fattore di moltiplicazione da applicare
	 * @see #getMultipliedValue()
	 */
	public void setMultiplier(float value) {
		multiplier = value;
		updateText();
	}
	
	/**
	 * Restituisce il fattore di conversione applicato per ottenere i valori in un'opportuna unit&agrave; di misura.
	 * 
	 * @return il fattore di conversione applicato
	 * @see #getConvertedValue()
	 */
	public float getConversion() {
		return conversion;
	}
	
	/**
	 * Imposta il fattore di conversione applicato per ottenere i valori in un'opportuna unit&agrave; di misura.
	 * 
	 * @param value il fattore di conversione da applicare
	 * @see #getConvertedValue()
	 */
	public void setConversion(float value) {
		conversion = value;
		updateText();
	}
	
	/**
	 * Restituisce il valore assunto dallo slider (con il fattore di moltiplicazione applicato).
	 * 
	 * @return il valore assunto dallo slider, con il fattore di moltiplicazione applicato
	 */
	public float getMultipliedValue() {
		return getValue() * multiplier;
	}
	
	/**
	 * Imposta il valore assunto dallo slider (con il fattore di moltiplicazione applicato).
	 * 
	 * @param value il valore che lo slider deve assumere
	 */
	public void setMultipliedValue(float value) {
		setValue((int) (value / multiplier));
	}
	
	/**
	 * Restituisce il valore assunto dallo slider (con il fattore di conversione applicato).
	 * 
	 * @return il valore assunto dallo slider, con il fattore di conversione applicato
	 */
	public float getConvertedValue() {
		return getMultipliedValue() * conversion;
	}
	
	/**
	 * Imposta il valore assunto dallo slider (con il fattore di conversione applicato).
	 * 
	 * @param value il valore che lo slider deve assumere
	 */
	public void setConvertedValue(float value) {
		setMultipliedValue(Math.round(value / conversion));
	}
	
	/**
	 * Restituisce il valore massimo che questo slider pu&ograve; assumere.
	 * 
	 * @return il valore massimo di questo slider
	 */
	public int getMaxValue() {
		return maxValue;
	}
	
	/**
	 * Imposta il valore massimo che questo slider pu&ograve; assumere.
	 * 
	 * @param maxValue il valore massimo di questo slider
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		seekBar.setMax(maxValue - minValue);
	}
	
	/**
	 * Restituisce il valore minimo che questo slider pu&ograve; assumere.
	 * 
	 * @return il valore minimo di questo slider
	 */
	public int getMinValue() {
		return minValue;
	}
	
	/**
	 * Imposta il valore minimo che questo slider pu&ograve; assumere.
	 * 
	 * @param minValue il valore minimo di questo slider
	 */
	public void setMinValue(int minValue) {
		int value = getValue();
		this.minValue = minValue;
		setValue(value);
	}
	
	/**
	 * Restituisce la stringa di formattazione usata per mostrare i valori assunti dallo slider.
	 * 
	 * @return la stringa di formattazione usata, nel formato supportato da {@link java.text.MessageFormat}
	 */
	public String getFormat() {
		return format;
	}
	
	/**
	 * Imposta la stringa di formattazione da usare per mostrare i valori assunti dallo slider.
	 * 
	 * @param format la stringa di formattazione da usare, nel formato supportato da {@link java.text.MessageFormat}
	 */
	public void setFormat(String format) {
		this.format = format;
	}
	
	/**
	 * Imposta il listener che ricever&agrave; le notifiche dei cambiamenti apportati a questo slider.
	 * 
	 * @param listener il listener da impostare
	 * @see OnChangeListener
	 */
	public void setOnChangeListener(OnChangeListener listener) {
		this.listener = listener;
	}
}
