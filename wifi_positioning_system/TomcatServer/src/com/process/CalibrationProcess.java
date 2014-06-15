package com.process;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;

import com.bdd.APInterface;
import com.bdd.MeasurementInterface;
import com.beans.Point;
import com.beans.Router;

/**
 * Class in which we handle the calibration procedure. It analyses coordinates 
 * given in the request. Create a new Location in the database if needed. And 
 * store the measurement return by Access Points in a calibration point.
 * The calibration failed if Router can't find the device or if there are not
 * enough samples to perform the measurement.
 */
public class CalibrationProcess extends Process {
	
	private APInterface apInterface = null;
	private MeasurementInterface measurement = null;
	private Point locationPoint = null;
	private int point_id;
	private String mac_mobile;
	private JSONObject response = new JSONObject();
	
	public CalibrationProcess(HttpServletRequest request){
		
		apInterface = new APInterface();
		measurement = new MeasurementInterface();
		
		locationPoint = new Point(0, request.getParameter("x"), request.getParameter("y"), request.getParameter("map_id"));
		
		// coordinates are controlled if there is already a point in the Database,
		// the Point take his id if it exists, if not, id is 0
		point_id = measurement.locationExist(locationPoint);
		locationPoint.setId(point_id);
		System.out.println("point wanted : id ="+point_id);


		//To switch between Simulation and real situation, swicth between the two folliwing lines.
		//mac_mobile = getMacFromArpCache(request.getRemoteAddr());
		mac_mobile = "11:11:11:11:11:11";
		
	}
	
	
	/**
	 * Control the location created and insert a new Calibration point.
	 * @return if the calibration succeed or not.
	 */
	public JSONObject getResponse(){

		ArrayList<Router> list_ap = apInterface.getAP();
		int nb_ap = list_ap.size();
		
		if(point_id!=0){
			//the point already exists, we need to send the result ! (but see if there are enough values first!)
			System.out.println("CalibProcess - the point exist ! id = " + measurement.locationExist(locationPoint));
			if (measurement.getCountCalibrationPoints(point_id)==nb_ap){
				try {
					response.put("calibration", 0);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return response;
			}
		}else{
			point_id = measurement.insertLocation(locationPoint);
			System.out.println("CalibProcess - point inserted. result = "+ point_id);
		
			//// we need to send a request to each AP and store its rssi values
			// query DB for AP MAC and IP address
			//for each address, create a thread and send a request to the AP to get values for the given MAC
			try {
				requestAP(list_ap, mac_mobile);
				
				int nb = measurement.getNumberOfTempValues(mac_mobile);
				
				//Check if there is one measure for each Access Point
				if ( nb == nb_ap){
					measurement.insertCalibrationPoint(point_id, mac_mobile);
					response.put("calibration", 0);
				} else {
					measurement.deleteTempValues(mac_mobile);
					response.put("calibration", -1);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}	
		}
		return response;
	}
}
