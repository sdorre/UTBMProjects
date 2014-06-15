package com.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bdd.MeasurementInterface;

/**
 * Thread requesting one Access Point for signal strength value for a given MAC address  
 *
 *	URL is the address where it can find the Access Point, 
 * 	id_AP is the ID of the access Point in the database
 *	mac_Mobile is the MAC address for which we want to know the signal value
 *  interface is the connection to database
 */

public class RequestThread extends Thread{
	
	private String url = null;
	private int id_AP = 0;
	private String mac_Mobile = null;
	private MeasurementInterface mInterface= null;
	

	/**
	 * Ask APs via the given parameters
	 * @param url
	 * @param id_AP
	 * @param mac_Mobile 
	 */

	public RequestThread(String url, int id_AP, String mac_Mobile){
		this.url = url;
		this.id_AP = id_AP;
		this.mac_Mobile = mac_Mobile;
		this.mInterface = new MeasurementInterface();
	}
	
	public void run(){
		
		long start = System.currentTimeMillis();
		URL obj;
		try {
			
			// we start a HTTP connection to one Access Point and get the response
			obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			// optional default is GET and timeout is 500ms
			con.setRequestMethod("GET");
			con.setConnectTimeout(500);
			
			//get Json returned by router and put values in DB
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String out = reader.readLine();
	
			JSONObject data = new JSONObject(out);
			System.out.println("Thread - Json received : " +data.toString() );
	
			// we extract elements needed and store in the table temprssi of the database
			String accessPoint = data.getString("ap");
			JSONArray rssitab = data.getJSONArray("rssi");
			
			for(int i =0; i<rssitab.length();i++){
				JSONObject rssi = (JSONObject) rssitab.get(i);
				System.out.println("Thread - rssi - number of value :" + rssi.getString("samples") + ", average value  : "+ rssi.getString(mac_Mobile));
				
				if(rssi.getInt("samples")>=5){
					String value = rssi.getString(mac_Mobile);
					mInterface.insertTempMeasure(id_AP, mac_Mobile, value);
				}else{
					System.out.println("Thread - Not enough samples, value not stored");
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			mInterface.close();
		}
		System.out.println("Thread - END -	time of execution = " + (System.currentTimeMillis()-start));
	}
	
}
