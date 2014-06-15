package com.process;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.bdd.APInterface;
import com.bdd.MeasurementInterface;
import com.beans.*;


/**
 * 	Class in which we compute the location of a mobile device
 *  We need a interface with the measurements and with the access points
 *  in the database.
 *  It requests the RSSI samples from the AP with a request :
 *	“http://IPaddr Of the AP/?mac=XX:XX:XX:XX:XX:XX” 
 * 	where the XX are the MAC address bytes in hexadecimal form.

*/

public class LocationProcess extends Process{
	
	private MeasurementInterface measurement;
	private APInterface apInterface;
	private ArrayList<Router> list_ap;
	private int nb_ap;
	
	private String mac_mobile;
	private int map_id;

	private JSONObject response;
	private ArrayList<CalibrationPoint> listOfCalibPoints= null;
	private Hashtable<Integer, Double> measure = null;
	private Point point = null;
	
	public LocationProcess(HttpServletRequest request){
		
		measurement = new MeasurementInterface();
		apInterface = new APInterface();

		//get the list of all the Access Points
		list_ap = apInterface.getAP();
		nb_ap = list_ap.size();
		
		if(nb_ap==0){
			System.out.println("LocProcess - There are no Access Points in database.");
			return;
		}

		//To switch between Simulation and real situation, swicth between the two folliwing lines.
		//mac_mobile = getMacFromArpCache(request.getRemoteAddr());
		mac_mobile = "11:11:11:11:11:11";
		
		//map_id is based on he location of the access points
		map_id = Integer.valueOf(list_ap.get(0).getMap_id());
		
		//initialize the response
		response = new JSONObject();
		
		listOfCalibPoints = measurement.getCalibrationPoints(map_id);
		if(listOfCalibPoints.isEmpty()){
			System.out.println("LocProcess - list of Calib Point is empty.");
			return;
		}

		requestAP(list_ap, mac_mobile);
	
		//we have all data from AP in the temp rssi table
		//we need to take these values and apply calculation to find the location of the mobile
		measure = measurement.getMeasure(mac_mobile);
		if(measure.size()!=0){
			point = closestInRssi(measure, listOfCalibPoints);
			if(point!=null){
				System.out.println("LocProcess - Find a point : id_loc = "+point.getId()+" map_id = "+point.getMapId()+" x = "+point.getX()+" y = "+point.getY());
			}
		}
	}

		
	/**
	 * Analyse all the process performed before and send a response in a JSON Object
	 * @return a JSON Object with a response for the mobile location
	 */
	public JSONObject getResponse(){
		
		try {
			int nb = measurement.getNumberOfTempValues(mac_mobile);
			System.out.println("LocProcess - Compare nb of AP and nb of values obtained : nb_ap = "+nb_ap+" nb_measure = "+nb);
		
			if(nb_ap==0){
				response.put("location", -1); //location failed
				response.put("cause", "no AP in database");
			}else if (listOfCalibPoints.isEmpty()){
				response.put("location", -1); //location failed
				response.put("cause", "no calibration points");		
			}else if(measure.size()==0){
				response.put("location", -1); //location failed
				response.put("cause", "measurement failed");	
			}else if(nb == nb_ap&&measure.size()!=0&&point!=null){
				response.put("location", 0); // location ok
				response.put("id", point.getMapId());
				response.put("x", point.getX());
				response.put("y", point.getY());
			}else{
				response.put("location", -1); //location failed
			}
			measurement.deleteTempValues(mac_mobile);
			measurement.close();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * Compute the RSSI distance between the measure send by the Access Points and a Point of calibration
	 * @param measure table containing a measurement for each Router
	 * @param point table containing the calibration value for each Router
	 * @return the distance the measure and the calibration point
	 */
	public double rssiDistance(Hashtable<Integer, Double> measure, Hashtable<Integer, Double> point){
		
		double dist=0.0;
		int index;  
		Enumeration<Integer> listAP = point.keys();
		
		while(listAP.hasMoreElements()) {
	    	index = (int) listAP.nextElement();
	    	dist+=((measure.get(index)-point.get(index))*(measure.get(index)-point.get(index)));
	    }
	    return Math.sqrt(dist);
	}
	
	
	/**
	 * Compute the RSSI distance for all calibration points and return the closest point in RSSI distance.
	 * @param measure : the measurement of each Router for the mobile request
	 * @param listOfPoints : the list of all the calibration points
	 * @return the closest Point to the location of the mobile device
	 */
	public Point closestInRssi(Hashtable<Integer, Double> measure, ArrayList<CalibrationPoint> listOfPoints){
		CalibrationPoint p = null;
		double min_dist=9999999.0;
		double tmp_dist;
	
		for(CalibrationPoint calibPoint : listOfPoints){
			tmp_dist=rssiDistance(measure, calibPoint.getRssiValues());
			if(tmp_dist<min_dist){
				min_dist = tmp_dist;
				p = calibPoint;
			}
		}
		return p.getPoint();
	}
}
