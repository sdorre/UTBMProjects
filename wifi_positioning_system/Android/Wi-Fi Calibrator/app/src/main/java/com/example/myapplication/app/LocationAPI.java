package com.example.myapplication.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.preference.PreferenceManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class LocationAPI {

    private Context mContext;

    public LocationAPI(Context context) {
        mContext = context;
    }

     public Location getLocation() throws IOException {

            HttpUriRequest request = new HttpGet(getServiceUri(Services.LOCATION));
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            if(statusLine.getStatusCode() == HttpStatus.SC_OK){

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                return decodeLocation(out.toString());

            } else{
                //Closes the connection.
                response.getEntity().getContent().close();

                throw new IOException(statusLine.getReasonPhrase());
            }
    }

    public HashMap<Integer, MapData> getMaps() throws IOException {

        HttpUriRequest request = new HttpGet(getServiceUri(Services.MAPS));
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

             ByteArrayOutputStream out = new ByteArrayOutputStream();
             response.getEntity().writeTo(out);
             out.close();

             return decodeMaps(out.toString());

        } else {
            //Closes the connection.
            response.getEntity().getContent().close();

            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    public List<CalibrationPoint> getCalibrationPoints(int mapID) throws IOException {

        HttpUriRequest request = new HttpGet(getServiceUri(Services.CALIBRATION_DATA)+ "?map_id=" + mapID);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();

            return decodeCalibrationPoints(out.toString());

        } else {
            //Closes the connection.
            response.getEntity().getContent().close();

            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    public Bitmap getBitmapOfMap(int mapID) throws IOException {

        HttpUriRequest request = new HttpGet(getServiceUri(Services.MAPS) + "?id=" + mapID);
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse response = httpClient.execute(request);

        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() == HttpStatus.SC_OK) {

            HttpEntity entity = response.getEntity();
            InputStream imageContentInputStream = entity.getContent();
            Bitmap imageBitmap =
                    BitmapFactory.decodeStream(
                            new BufferedInputStream(imageContentInputStream));

            return imageBitmap;

        } else {
            //Closes the connection.
            response.getEntity().getContent().close();

            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    public int sendCalibrationPoint(int mapID, PointF point) throws IOException {

        HttpUriRequest request = new HttpGet(getServiceUri(Services.CALIBRATION)+"?map_id="+String.valueOf(mapID)+"&x="+String.valueOf(point.x)+"&y="+String.valueOf(point.y));
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        StatusLine statusLine = response.getStatusLine();

        if(statusLine.getStatusCode() == HttpStatus.SC_OK){

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(String.valueOf(out));

            if(rootNode.has("calibration")) {
                int result = rootNode.get("calibration").getIntValue();
                return result;
            }

        } else{
            //Closes the connection.
            response.getEntity().getContent().close();

            throw new IOException(statusLine.getReasonPhrase());
        }

        return -1;
    }


    public Location decodeLocation(String json) {
        ObjectMapper mapper = new ObjectMapper();
        Location data = new Location();
        try {
            data = mapper.readValue(json, Location.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public List<CalibrationPoint> decodeCalibrationPoints(String json) {
        ObjectMapper mapper = new ObjectMapper();

        List<CalibrationPoint> calibrationPoints = null;

        try {
            calibrationPoints = mapper.readValue(json, new TypeReference<List<CalibrationPoint>>() { });
        } catch (JsonMappingException e1) {
            e1.printStackTrace();
        } catch (JsonParseException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        return calibrationPoints;
    }

    private HashMap<Integer, MapData> decodeMaps(String json) {
        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode rootNode = mapper.readTree(json);

            HashMap<Integer, MapData> mapList = new HashMap<Integer, MapData>();

            for(Iterator<Map.Entry<String, JsonNode>> iter = rootNode.getFields(); iter.hasNext();) {
                Map.Entry<String, JsonNode> node = iter.next();

                int mapID = Integer.valueOf(node.getKey());

                JsonNode dataList = node.getValue();

                if(dataList.size() == 5) {
                    String mapName = dataList.get(0).getTextValue();
                    double mapWidth = dataList.get(1).getDoubleValue();
                    double mapHeight = dataList.get(2).getDoubleValue();
                    double mapWidthInMeters = dataList.get(3).getDoubleValue();
                    double mapHeightInMeters = dataList.get(4).getDoubleValue();

                    MapData newMap = new MapData(mapID, mapName, mapWidth, mapHeight, mapWidthInMeters, mapHeightInMeters);

                    mapList.put(mapID, newMap);
                }
            }

            return mapList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getServiceUri(String service) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        String uri = settings.getString("preference_server_hostname", "0.0.0.0");
        String port = settings.getString("preference_server_port", "0");
        String api_path = settings.getString("preference_server_api_location", "");

        String serviceUri = "http://"+uri+":"+port+"/"+api_path+"/";
        if(service != null)
            serviceUri += service;

        return serviceUri;
    }

    public String resetCalibration(int mapID) throws IOException {

        HttpUriRequest request = new HttpGet(getServiceUri(Services.RESET)+"?map_id="+String.valueOf(mapID));
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(request);
        StatusLine statusLine = response.getStatusLine();

        if(statusLine.getStatusCode() == HttpStatus.SC_OK){

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();

            return out.toString();

        } else{
            //Closes the connection.
            response.getEntity().getContent().close();

            throw new IOException(statusLine.getReasonPhrase());
        }
    }

    public interface Services {
        String LOCATION = "location";
        String CALIBRATION = "calibration";
        String MAPS = "maps";
        String RESET = "deletecalibration";
        String CALIBRATION_DATA = "calibrationpoints";
    }

    public class MapData {

        private int mID;
        private String mName;
        private double mWidth;
        private double mHeight;
        private double mWidthInMeters;
        private double mHeightInMeters;

        private Bitmap mBitmap;

        public MapData(int id, String name, double width, double height, double widthInMeters, double heightInMeters) {
            mID = id;
            mName = name;
            mWidth = width;
            mHeight = height;
            mWidthInMeters = widthInMeters;
            mHeightInMeters = heightInMeters;
            mBitmap = null;
        }

        public int getID() {
            return mID;
        }

        public String getName() {
            return mName;
        }

        public double getHeight() {
            return mHeight;
        }

        public double getWidthInMeters() {
            return mWidthInMeters;
        }

        public double getHeightInMeters() {
            return mHeightInMeters;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public void setBitmap(Bitmap mBitmap) {
            this.mBitmap = mBitmap;
        }

        @Override
        public String toString() {
            return "MapData{" +
                    "mHeightInMeters=" + mHeightInMeters +
                    ", mID=" + mID +
                    ", mName='" + mName + '\'' +
                    ", mWidth=" + mWidth +
                    ", mHeight=" + mHeight +
                    ", mWidthInMeters=" + mWidthInMeters +
                    '}';
        }
    };

    public static class Location {

        @JsonProperty("id")
        public int mapID = -1;

        @JsonProperty("location")
        public int status = -1;

        @JsonProperty("x")
        public float x = 0.0f;

        @JsonProperty("y")
        public float y = 0.0f;

        public PointF getPoint() {
            return new PointF(x,y);
        }
    }

    public static class CalibrationPoint {

        @JsonProperty("id")
        public int id = -1;

        @JsonProperty("map_id")
        public int mapID = -1;

        @JsonProperty("x")
        public float x = 0.0f;

        @JsonProperty("y")
        public float y = 0.0f;

        public PointF getPoint() {
            return new PointF(x,y);
        }
    }
}
