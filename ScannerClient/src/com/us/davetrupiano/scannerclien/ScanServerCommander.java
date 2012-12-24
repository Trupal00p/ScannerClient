package com.us.davetrupiano.scannerclien;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;




public class ScanServerCommander{
	
	private final String TAG = ScanServerCommander.class.getSimpleName();
	
	private final String host;
	private String sessionID;
	private String userFullName;
	private String currentBinDrugDisplay;
	private String currentBinDrugRXCUI;
	private String currentBinID;
	private Context context;
	
	private TextView currentBinDrugText;
	private TextView currentUserFullNameText;
	private ResponseAdapter scanHistoryAdapter;
	
	private int errorCount;
	

	
	public ScanServerCommander(String host, TextView currentBinDrugText,TextView currentUserFullNameText, ListView scanHistoryList, Context context){
		this.host = host;
		this.currentBinDrugText = currentBinDrugText;
		this.currentUserFullNameText = currentUserFullNameText;
		this.scanHistoryAdapter = (ResponseAdapter) scanHistoryList.getAdapter();
		this.context = context;
	}
	
	public void openSession(){//String userHash){
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(Constants.CMD_OPEN_SESSION);
		
		cmd.addParam(Constants.PARAM_USER_HASH, "123");
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void changeCurrentBin(String binID){
		//update current bin display text
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(Constants.CMD_GET_BIN_DISPLAY);
		
		cmd.addParam(Constants.PARAM_BIN_ID, binID);
		cmd.addParam(Constants.PARAM_SESSION_ID, sessionID);
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void checkScan(String rawBarcode){
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(Constants.CMD_CHECK_SCAN);
		
		cmd.addParam(Constants.PARAM_BARCODE, rawBarcode);
		cmd.addParam(Constants.PARAM_BIN_ID, currentBinID);
		cmd.addParam(Constants.PARAM_SESSION_ID, sessionID);
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void addressScanResponse(final int position, final long id){
		
		if (scanHistoryAdapter.getItem(position).isMismatched()){
			AlertDialog.Builder b = new AlertDialog.Builder(context);
			b.setTitle("Select Appropriate Response");
			b.setItems(R.array.scanMismatchResponses, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					sendScanFeedBack(which, id, position);
				}
			});
			b.create().show();
		}
		
		
	}
	
	private void sendScanFeedBack(int which, long id, int position){
		NetworkAsyncTasks nt = new NetworkAsyncTasks();
		CommandBundle cmd = new CommandBundle(Constants.CMD_ADDRESS_MISMATCH);
		
		cmd.addParam(Constants.PARAM_MISMATCH_RESPONSE_ID, String.valueOf(which));
		cmd.addParam(Constants.PARAM_SCAN_EVENT_ID, String.valueOf(id));
		cmd.addParam(Constants.PARAM_SESSION_ID, sessionID);
		cmd.addParam(Constants.PARAM_SCAN_HISTORY_POSITION, String.valueOf(position));
		
		nt.execute(new CommandBundle[]{cmd});
	}
	
	public void decrementErrors(){
		this.errorCount=errorCount-1;
	}	
	
	private class NetworkAsyncTasks extends AsyncTask<CommandBundle, Void, ArrayList<ResponseBundle>>{

		@Override
		protected ArrayList<ResponseBundle> doInBackground(CommandBundle... params) {
			
			ArrayList<ResponseBundle> retval = new ArrayList<ResponseBundle>();
			
			for (CommandBundle cmdBundle: params){
				
				//query server
				JSONObject jsonObj = sendCommand(cmdBundle);
				
				//return server response to UI thread
				//retval.put(cmdBundle, jsonObj);
				retval.add(new ResponseBundle(cmdBundle, jsonObj));
					
				}
			
			return retval;
			
		}
		
		@Override
		protected void onPostExecute(ArrayList<ResponseBundle> result) {
			
			for (ResponseBundle rspBundle: result){
				
				JSONObject jsonObj = rspBundle.getJSONRepsonse();
				
				if (jsonObj!=null){
					
					String cmd = rspBundle.cmdBundle.getCommand();
					
					if (cmd.equals(Constants.CMD_OPEN_SESSION)){
						
						onOpenSession(rspBundle);
						
					}else if (cmd.equals(Constants.CMD_CHECK_SCAN)){
						
						onCheckScan(rspBundle);
						
					}else if (cmd.equals(Constants.CMD_REASSIGN_BIN)){
						
						onReassignBin(rspBundle);
						
					}else if (cmd.equals(Constants.CMD_GET_BIN_DISPLAY)){
						
						onChangeBin(rspBundle);
						
					} else if (cmd.equals(Constants.CMD_ADDRESS_MISMATCH)){
						
						onAddressMismatch(rspBundle);
					}
				}
			}
			
			
		}
			
		
		private void onOpenSession(ResponseBundle rspBundle){
			sessionID = rspBundle.getResponseString(Constants.JSON_SESSION_ID);
			currentUserFullNameText.setText(sessionID);		
			
		}
		
		private void onCheckScan(ResponseBundle rspBundle){
			
			//update view adapter
			if (rspBundle.getCommandString().equals(Constants.CMD_CHECK_SCAN)){
				scanHistoryAdapter.addResponse(rspBundle);
			}
		}
		
		private void onReassignBin(ResponseBundle rspBundle){
			
			
		}
		
		private void onChangeBin(ResponseBundle rspBundle){
			//read JSON response
			currentBinDrugDisplay = rspBundle.getResponseString(Constants.JSON_DRUG_DISPLAY);
			currentBinDrugRXCUI = rspBundle.getResponseString(Constants.JSON_RXCUI);
			
			//update views
			currentBinDrugText.setText(currentBinDrugDisplay);
			
			//change current binID
			currentBinID = rspBundle.getCommandParam(Constants.PARAM_BIN_ID);			
			
		}
		
		private void onAddressMismatch(ResponseBundle rspBundle){
			int position = rspBundle.getResponseInt(Constants.JSON_SCAN_EVENT_ID, -1);
			if (position!=-1){
				scanHistoryAdapter.addressResponse(position);
			}
			
		}
		
		private JSONObject sendCommand(CommandBundle cmdBundle){
			//String cmd, HashMap<String,String> parameters
			//set local vars
			JSONObject jobj = null;
			URL url;
			
			//build url string
			String requestString = cmdBundle.getRequestURLString();
			
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
				Log.d(TAG,"Malformed URL");
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
		
		private String getRequestURLString(){
			StringBuilder sb = new StringBuilder();
			//build base url
			sb.append("http://").append(host).append("/").append(cmd);
			//check for empty parameters
			if (parameters != null){
				//append ? to begin adding parameters
				sb.append("?");
				for (Entry<String,String> pair : parameters.entrySet()){
					//append "key=value&" to end of string
					sb.append(pair.getKey()).append("=").append(pair.getValue()).append("&");
				}
				//delete last "&" from string
				sb.deleteCharAt(sb.length()-1);
			}
			//return URL
			
			return sb.toString();
		}
	}
	
	public class ResponseBundle{
		
		private CommandBundle cmdBundle;
		private JSONObject jsonObj;
		private int mismatchAddressed;
		
		public ResponseBundle(CommandBundle cmdBundle, JSONObject jsonObj){
			this.cmdBundle=cmdBundle;
			this.jsonObj = jsonObj;
			try{
				if (jsonObj.getBoolean(Constants.JSON_SCAN_SUCCESS)){
					mismatchAddressed  = Constants.MM_STATUS_ADDRESSED;
				}else{
					mismatchAddressed = Constants.MM_STATUS_NOT_ADDRESSED;
				}
					
			} catch (Exception e){
				mismatchAddressed = Constants.MM_STATUS_NA;
			}
		}
		
		public String getCommandString(){
			return cmdBundle.getCommand();
		}
		
		public String getCommandParam(String key){
			return cmdBundle.getParams().get(key);
		}
		
		public JSONObject getJSONRepsonse(){
			return jsonObj;
		}
		
		public void addressMismatch(){
			mismatchAddressed = Constants.MM_STATUS_ADDRESSED;
		}
		
		public boolean isMismatched(){
			if (mismatchAddressed==Constants.MM_STATUS_NOT_ADDRESSED){
				return true;
			}else{
				return false;
			}
		}
		
		public String getResponseString(String key){
			String retval;
			try{
				retval = jsonObj.getString(key);
			}catch (Exception e){
				try {
					retval = jsonObj.getString(Constants.JSON_ERROR_MESSAGE);
				} catch (JSONException e1) {
					retval = "No Server Response";
				}
			}
			return retval;
		}
		
		public boolean getResponseBoolean(String key, boolean onErrorValue){
			boolean retval;
			try{
				retval = jsonObj.getBoolean(key);
			}catch (Exception e){
				retval = onErrorValue;
			}
			return retval;
		}
		
		public int getResponseInt(String key, int onErrorValue){
			int retval;
			try{
				retval = jsonObj.getInt(key);
			}catch (Exception e){
				retval = onErrorValue;
			}
			return retval;
		}
		
		public JSONArray getResponseArray(String key){
			JSONArray retval;
			try{
				retval = jsonObj.getJSONArray(key);
			}catch (Exception e){
				retval = new JSONArray();
			}
			return retval;
		}
		
	}
	
}
		

	
