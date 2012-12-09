package com.us.davetrupiano.scannerclien;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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
	private final String host = "192.168.1.28";
	private final String port = "8080";
	
	private ScanServerCommander ssc;
	
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		currentBinDisplay = (TextView) findViewById(R.id.currentBinDrugText);
		currentUserDisplay = (TextView) findViewById(R.id.currentUserText);
		
		
		recentScansList = (ListView) findViewById(R.id.recentScanList);
//		recentScansList.setAdapter(adapter);
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
		
		ssc = new ScanServerCommander(host, port, currentBinDisplay,currentUserDisplay);
		
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
