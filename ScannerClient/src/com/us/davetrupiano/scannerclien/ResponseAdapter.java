package com.us.davetrupiano.scannerclien;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.us.davetrupiano.scannerclien.ScanServerCommander.ResponseBundle;

public class ResponseAdapter extends BaseAdapter implements ListAdapter {

	private ArrayList<ResponseBundle> rspBundleList;
	private Context context;
	private Vibrator v;
	private int errorCount;
	
	public ResponseAdapter(Context context){
		this.context = context;
		this.rspBundleList = new ArrayList<ResponseBundle>();
		this.v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		this.errorCount=0;
	}
	
	private void incrementError(){
		errorCount = errorCount + 1;
	}
	
	private void decrementError(){
		errorCount = errorCount - 1;
	}
	
	@Override
	public int getCount() {
		return rspBundleList.size();
	}

	@Override
	public ResponseBundle getItem(int index) {
		return rspBundleList.get(index);
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getResponseInt(Constants.JSON_SCAN_EVENT_ID, 0);
	}

	@Override
	public View getView(int itemIndex, View oldView, ViewGroup parentView) {
		// TODO Auto-generated method stub
		
		View responseRow = oldView;
		
		//recylce row if possible
		if (responseRow==null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			responseRow = inflater.inflate(R.layout.recent_scan_success, parentView, false);
		}
		
		TextView isMatchedText = (TextView) responseRow.findViewById(R.id.isMatchedText);
		ImageView sucessImage = (ImageView) responseRow.findViewById(R.id.sucessImage);
//		TextView matchedWithText = (TextView) responseRow.findViewById(R.id.matchedWithText);
		
		ResponseBundle rspBundle = getItem(itemIndex);
		
		if (rspBundle.getResponseBoolean(Constants.JSON_SCAN_SUCCESS, false)){
			sucessImage.setImageResource(R.drawable.green_ok);
		}else if (rspBundle.getResponseBoolean(Constants.JSON_SCAN_SUCCESS, false)==false){
			if (rspBundle.isMismatched()){
				sucessImage.setImageResource(R.drawable.red_failure);
			}else{
				sucessImage.setImageResource(R.drawable.addressed_failure);
			}
		}
		String scannedDrugsStrings = getStringFromScannedDrugs(rspBundle.getResponseArray(Constants.JSON_SCANNED_DRUGS));
		isMatchedText.setText(scannedDrugsStrings);
		
		return responseRow;
	}
	
	private String getStringFromScannedDrugs(JSONArray ary){
		StringBuilder sb = new StringBuilder();
		if (ary.length()==0){
			sb.append("Med Not Identified");
		}else{
			for (int i=0; i<ary.length();i++){
				try {
					sb.append(ary.getJSONObject(i).getString(Constants.JSON_DRUG_DISPLAY));
					//add ors where required
					if (i<ary.length()-1){
						sb.append(" OR ");
					}
				} catch (JSONException e) {
					sb.append("No Array");
				}
			}
		}
		return sb.toString();
	}
	
	public void addResponse(ResponseBundle rspBundle){
		rspBundleList.add(0, rspBundle);
		if (rspBundle.isMismatched()){
			incrementError();
		}
		refreshVibrate();		
		notifyDataSetChanged();
	}
	
	public void addressResponse(long id){
		int position = getPositionFromID((int) id);
		if (position != -1){
			getItem(position).addressMismatch();
			decrementError();
			refreshVibrate();
			notifyDataSetChanged();
		}
	}
	
	private int getPositionFromID(int id){
		for (int i=0;i<rspBundleList.size();i++){
			if (rspBundleList.get(i).getResponseInt(Constants.JSON_SCAN_EVENT_ID, 0)==id){
				return i;
			}
		}
		return -1;
	}
	
	private void refreshVibrate(){
		if (errorCount>0){
			//more than 0 errors
			long [] p = {500L,500L,500L,500L,500L};
			v.vibrate(p, 1);
		}else{
			v.cancel();
		}

		
		
	}

}
