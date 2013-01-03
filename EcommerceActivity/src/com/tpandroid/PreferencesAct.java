package com.tpandroid;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class PreferencesAct extends Activity {
	
	CheckBox autoUpdate;
	Spinner updateFreqSpinner;
	EditText keyword;
	EditText price;
	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	public static final String PREF_KEYWORD = "PREF_KEYWORD";
	public static final String PREF_MAX_PRICE ="PREF_MAX_PRICE"; 
	
	SharedPreferences prefs;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);
		
		updateFreqSpinner = (Spinner)findViewById(R.id.spinner_update_freq);
		autoUpdate = (CheckBox)findViewById(R.id.checkbox_auto_update);
		keyword = (EditText)findViewById(R.id.keyword_preference);
		price = (EditText)findViewById(R.id.maximum_price_preference);
		populateSpinners();
		
		Context context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		updateUIFromPreferences();
		
		Button okButton = (Button)findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {
			
			
			public void onClick(View v) {
				savePreferences();
				PreferencesAct.this.setResult(RESULT_OK);
				finish();
			}
		});
		
		Button cancelButton = (Button)findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(
					new View.OnClickListener() {
						
						
						public void onClick(View v) {
							PreferencesAct.this.setResult(RESULT_CANCELED);
							finish();
						}
					}
				);
	}
	
	private void populateSpinners() {
		ArrayAdapter<CharSequence> freqAdapter;
		freqAdapter = ArrayAdapter.createFromResource(this, 
				R.array.update_freq_options, 
				android.R.layout.simple_spinner_item);
		int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
		freqAdapter.setDropDownViewResource(spinner_dd_item);
		updateFreqSpinner.setAdapter(freqAdapter);
		ArrayAdapter<CharSequence> magnAdapter;
		magnAdapter = ArrayAdapter.createFromResource(this, 
				R.array.magnitude_options, 
				android.R.layout.simple_spinner_item);
		magnAdapter.setDropDownViewResource(spinner_dd_item);
 	}
	
	private void updateUIFromPreferences() {
		boolean autoUpChecked = prefs.getBoolean(PREF_AUTO_UPDATE, false);
		int updateFreqIndex = prefs.getInt(PREF_UPDATE_FREQ, 2);
		String keyWordPref = prefs.getString(PREF_KEYWORD, "") ;
		String pricePref = prefs.getString(PREF_MAX_PRICE, "");
	 	System.out.println("KeeeeWord"+ keyWordPref);
	 	System.out.println("updateFreqIndex"+updateFreqIndex);

		keyword.setText(keyWordPref);
		price.setText(pricePref);
		
		updateFreqSpinner.setSelection(updateFreqIndex);
 	autoUpdate.setChecked(autoUpChecked);
	}
	
	private void savePreferences() {
		int updateIndex = updateFreqSpinner.getSelectedItemPosition();
 		boolean autoUpdateChecked = autoUpdate.isChecked();
		String key_Words_Pref  = keyword.getText().toString();
		String price_pref = price.getText().toString();
		System.out.println("Keyword is "+ key_Words_Pref);
		System.out.println("Price is "+ price_pref);
	 	Editor editor = prefs.edit();
		editor.putBoolean(PREF_AUTO_UPDATE, autoUpdateChecked);
		editor.putInt(PREF_UPDATE_FREQ, updateIndex);
		editor.putString(PREF_KEYWORD, key_Words_Pref);
		editor.putString(PREF_MAX_PRICE, price_pref);
 		editor.commit();
	}
}
