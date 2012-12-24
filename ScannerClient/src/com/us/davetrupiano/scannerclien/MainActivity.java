package com.us.davetrupiano.scannerclien;

import java.io.IOException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity {
	
	private String TAG = "dave";//MainActivity.class.getSimpleName();
	
	private EditText commandEntry;
	private TextView currentBinDisplay;
	private TextView currentUserDisplay;
	private ListView recentScansList;
	private int scanMode;
	private SharedPreferences prefs;
	private String host;
	private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
	
	private ScanServerCommander ssc;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
					String key) {
				// TODO Auto-generated method stub
				prefs = sharedPreferences;
				if (key.equals("host")){
					host = prefs.getString(key, "");
					restartScanServerCommander();
				}
			}
		}; 
				
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
		host = prefs.getString("host", "");
		
		currentBinDisplay = (TextView) findViewById(R.id.currentBinDrugText);
		currentUserDisplay = (TextView) findViewById(R.id.currentUserText);
		
		
		recentScansList = (ListView) findViewById(R.id.recentScanList);
		recentScansList.setAdapter(new ResponseAdapter(getBaseContext()));
		recentScansList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ssc.addressScanResponse(position, id);
			}
		});
		
		commandEntry = (EditText) findViewById(R.id.editText1);
		commandEntry.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				try {
					parseCommand(v.getText().toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				v.setText("");
				return true;
			}
		});
		
		restartScanServerCommander();
		
		
		
	}
	
	private void restartScanServerCommander(){
		ssc = null;
		ssc = new ScanServerCommander(host, currentBinDisplay,currentUserDisplay,recentScansList, MainActivity.this);
	}
	
	
	
	private void parseCommand(String command) throws IOException{
		String indicator = getIndicator(command);
		String commandNumbers = getCommandNumbers(command);
		if (indicator.equals("B")){
			
			//change bin here
			ssc.changeCurrentBin(commandNumbers);
			
		}else if (indicator.equals("U")){
			
			//open session here
			ssc.openSession();
			
		}else if (indicator.equals("R")){
			//TODO
		
			
		}else{
			
			//no indicator must be drug barcode
			ssc.checkScan(command);
			
		}
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_settings:
	    	startActivity(new Intent(getBaseContext(), PrefActivity.class));
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private String getIndicator(String text){
		String retval;
		if (text.length()>0){
			retval = text.substring(0, 1);
		}else{
			retval = text;
		}
		return retval;
		
	}
	
	private String getCommandNumbers(String text){
		String retval;
		if (text.length()>1){
			retval = text.substring(1);
		}else{
			retval = text;
		}
		return retval;
	}

}
