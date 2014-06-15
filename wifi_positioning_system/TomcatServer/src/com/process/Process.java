package com.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import com.beans.Router;

/**
 * Class for providing generic functions for a procedure (like Calibration or Location Process).
 */
public abstract class Process {

	/**
	 * This function takes a list of Access Points and send a GET request for each of them.
	 *	Each request is done in a new Thread.
	 * @param list_ap : the List of Router 
	 */
	public void requestAP(ArrayList<Router> list_ap, String mac_mobile){
		
		ArrayList<Thread> thread_pool = new ArrayList<Thread>();
		for(Router ap : list_ap){
			System.out.println("LocProcess - new thread for ip : "+ap.getIp_addr()+ " id : "+ap.getId());
			
			//To switch between Simulation and real situation, switch between the two folliwing lines.
			//RequestThread thread = new RequestThread("http://" + ap.getIp_addr() + ":8080/?mac=" + mac_mobile, ap.getId(), mac_mobile);
			RequestThread thread = new RequestThread("http://localhost:8080/wifi/test", ap.getId(), mac_mobile);
			
			thread_pool.add(thread);
			thread.start();
		}
		
		try {
			Thread.sleep(500);
			for(Thread t: thread_pool){
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Take the IP address and search in the ARP table if there is a MAC address which correspond
	 * @param ip : IP address of the device
	 * @return MAC address of the device given in parameter
	 */
	public static String getMacFromArpCache(String ip) {
	    if (ip == null)
	        return null;
	    BufferedReader br = null;
	    try {
	        br = new BufferedReader(new FileReader("/proc/net/arp"));
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] splitted = line.split(" +");
	            if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
	                // Basic sanity check
	                String mac = splitted[3];
	                if (mac.matches("..:..:..:..:..:..")) {
	                    return mac;
	                } else {
	                    return null;
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            br.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return null;
	}
}
