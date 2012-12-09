package com.us.davetrupiano.scannerclien;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;




public class ScanServerCommander{
	
	private final String TAG = ScanServerCommander.class.getSimpleName();
	
	private final String host;
	private final String port;
	private String sessionID;
	private String userFullName;
	private String currentBinDrugDisplay;
	private String currentBinDrugRXCUI;
	private String currentBinID;
	
	
	private TextView currentBinDrugText;
	private TextView currentUserFullNameText;
	
	//commands sent to server
	private final String CMD_OPEN_SESSION = "openSession";
	private final String CMD_CHECK_SCAN = "scanCheck";
	private final String CMD_REASSIGN_BIN = "reassignBin";
	private final String CMD_GET_BIN_DISPLAY = "getBinDisplay";
	
	//parameters sent to server
	private final String PARAM_BIN_ID = "binID";
	private final String PARAM_SESSION_ID = "sessionID";
	private final String PARAM_BARCODE = "barcode";
	private final String PARAM_USER_HASH = "userHash";
	
	//parameters received from server
	private final String JSON_SESSION_ID = "sessionID";
	private final String JSON_DRUG_DISPLAY = "drugDisplay";
	private final String JSON_RXCUI = "RXCUI";
	private final String JSON_SCAN_SUCCESS = "matchSuccess";
	private final String JSON_ERROR_MESSAGE = "errorMessage";
	private final String JSON_RESPONSE_CODE = "responseCode";
	
	public ScanServerCommander(String host, String port, TextView currentBinDrugText,TextView currentUserFullNameText){
		this.host = host;
		this.port = port;
		this.currentBinDrugText = currentBinDrugText;
		this.currentUserFullNameText = currentUserFullNameText;
	}
	
	
	
	public void openSession(){//String userHash){
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(CMD_OPEN_SESSION);
		
//		cmd.addParam(PARAM_USER_HASH, userHash);
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void changeCurrentBin(String binID){
		//update current bin display text
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(CMD_GET_BIN_DISPLAY);
		
		cmd.addParam(PARAM_BIN_ID, binID);
		cmd.addParam(PARAM_SESSION_ID, sessionID);
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void checkScan(String rawBarcode){
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(CMD_CHECK_SCAN);
		
		cmd.addParam(PARAM_BARCODE, rawBarcode);
		cmd.addParam(PARAM_BIN_ID, currentBinID);
		cmd.addParam(PARAM_SESSION_ID, sessionID);
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	
	private class NetworkAsyncTasks extends AsyncTask<CommandBundle, Void, HashMap<CommandBundle,JSONObject>>{

		@Override
		protected HashMap<CommandBundle,JSONObject> doInBackground(CommandBundle... params) {
			
			HashMap<CommandBundle,JSONObject> retval = new HashMap<ScanServerCommander.CommandBundle, JSONObject>();
			
			for (CommandBundle cmdBundle: params){
				
				//query server
				JSONObject jsonObj = sendCommand(cmdBundle);
				
				//return server response to UI thread
				retval.put(cmdBundle, jsonObj);
					
				}
			
			return retval;
			
		}
		
		@Override
		protected void onPostExecute(HashMap<CommandBundle,JSONObject> result) {
			
			for (CommandBundle cmdBundle: result.keySet()){
				
				JSONObject jsonObj = result.get(cmdBundle);
				
				if (jsonObj!=null){
					
					String cmd = cmdBundle.getCommand();
					
					if (cmd.equals(CMD_OPEN_SESSION)){
						
						onOpenSession(jsonObj,cmdBundle);
						
					}else if (cmd.equals(CMD_CHECK_SCAN)){
						
						onCheckScan(jsonObj,cmdBundle);
						
					}else if (cmd.equals(CMD_REASSIGN_BIN)){
						
						onReassignBin(jsonObj,cmdBundle);
						
					}else if (cmd.equals(CMD_GET_BIN_DISPLAY)){
						
						onChangeBin(jsonObj,cmdBundle);
						
					}
				}
			}
			
			
		}
			
		
		private void onOpenSession(JSONObject jsonObj, CommandBundle cmdBundle){
			//read JSON response
			try {
				sessionID = jsonObj.getString(JSON_SESSION_ID);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			//update views
			currentUserFullNameText.setText(sessionID);
			
		}
		
		private void onCheckScan(JSONObject jsonObj, CommandBundle cmdBundle){
			//read JSON response
			String success = "False";
			try {
				//update arrayadapter here TODO
				success = jsonObj.getString(JSON_SCAN_SUCCESS);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			//update views
			currentUserFullNameText.setText(success);
			
			
		}
		
		private void onReassignBin(JSONObject jsonObj, CommandBundle cmdBundle){
			
		}
		
		private void onChangeBin(JSONObject jsonObj, CommandBundle cmdBundle){
			
			try {
				
				//read JSON response
				currentBinDrugDisplay = jsonObj.getString(JSON_DRUG_DISPLAY);
				currentBinDrugRXCUI = jsonObj.getString(JSON_RXCUI);
				
				//update views
				currentBinDrugText.setText(currentBinDrugDisplay);
				
				//change current binID
				currentBinID = cmdBundle.getParams().get(PARAM_BIN_ID);
				
			} catch (JSONException e) {
				Log.d(TAG, "Failed to change bin unreadable response");
				e.printStackTrace();
			}			
			
		}
		
		private String buildRequestURL(CommandBundle cmdBundle){
			StringBuilder sb = new StringBuilder();
			//build base url
			sb.append("http://").append(host).append(":").append(port).append("/").append(cmdBundle.getCommand());
			//check for empty parameters
			if (cmdBundle.getParams() != null){
				//append ? to begin adding parameters
				sb.append("?");
				for (Entry<String,String> pair : cmdBundle.getParams().entrySet()){
					//append "key=value&" to end of string
					sb.append(pair.getKey()).append("=").append(pair.getValue()).append("&");
				}
				//delete last "&" from string
				sb.deleteCharAt(sb.length()-1);
			}
			//return URL
			
			return sb.toString();
		}
		
		private JSONObject sendCommand(CommandBundle cmdBundle){
			//String cmd, HashMap<String,String> parameters
			//set local vars
			JSONObject jobj = null;
			URL url;
			
			//build url string
			String requestString = buildRequestURL(cmdBundle);
			
			//build,send and read request
			try {
				//build request
				url = new URL(requestString);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				//send request
				InputStream in = urlConnection.getInputStream();
				//read request
				jobj = readStream(in);
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//return JSON object for reading
			return jobj;
			
		}
		
		private JSONObject readStream(InputStream in){
			BufferedReader reader = null;
			StringBuilder sb = new StringBuilder();
			JSONObject jobj = null;
			
			//read response body
			try{
				reader = new BufferedReader(new InputStreamReader(in));
				String line = "";
				while ((line = reader.readLine())!= null){
					// do something with each line
					Log.d(TAG,line);
					sb.append(line);
				}
			}catch (IOException e){
				e.printStackTrace();
			}finally{
				if (reader != null){
					try{
						reader.close();
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}
			
			//parse JSON from response body
			try{
				jobj = new JSONObject(sb.toString());
			}catch (JSONException e){
				Log.d(TAG, "Error parsing JSON Data "+e.toString());
			}
			
			//return JSON object
			return jobj; 
		}
	
	}
	
	private class CommandBundle{
		
		private String cmd;
		private HashMap<String, String> parameters;
		
		public CommandBundle(String cmd){
			this.cmd = cmd;
			this.parameters = new HashMap<String,String>();
		}
		
		public void addParam(String key, String value){
			parameters.put(key, value);
		}
		
		public void removeParam(String key){
			parameters.remove(key);
		}
		
		public HashMap<String,String> getParams(){
			return parameters;
		}
		
		public String getCommand(){
			return cmd;
		}
	}
	
}
		

	
