package com.example.myapplication2.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements
        MapFragment.OnFragmentInteractionListener,
        NavigationDrawerFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_MAP_FRAGMENT = "map_fragment";

    private static int LOCATION_TRACKING_INTERVAL = 10000;

    private boolean mSettingsChanged = false;

    private LocationAPI mLocationAPI;

    private FetchCalibrationPointsTask mFetchCalibrationPointsTask;
    private LocationTrackingTask mLocationTrackingTask;
    private Timer mLocationTrackingTaskTimer;
    private Handler mLocationTrackingTaskHandler;
    private Runnable mLocationTrackingTaskRunnable;

    private DummyNetTrafficGenerator mDummyNetTrafficGenerator;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DrawerLayout mNavigationDrawer;

    private MapFragment mMapFragment;

    private TextView mStatusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the location API
        mLocationAPI = new LocationAPI(this);

        // get the navigation drawer
        mNavigationDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // get the navigation drawer's fragment
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // set up the drawer
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mNavigationDrawer);

        // setup the navigation drawer's button in the loading overlay
        ImageView navigationDrawerButton = (ImageView) findViewById(R.id.navigation_drawer_button);
        navigationDrawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNavigationDrawer();
            }
        });

        mStatusText = (TextView) findViewById(R.id.status_text);

        // get preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        // register this to listen for preference changes
        settings.registerOnSharedPreferenceChangeListener(this);

        // create traffic generator instance
        mDummyNetTrafficGenerator = new DummyNetTrafficGenerator(this);

        if(settings.getBoolean("preference_generate_dummy_traffic", false))
            // start the traffic generator
            mDummyNetTrafficGenerator.start();

        // initialize the location tracking
        mLocationTrackingTaskHandler = new Handler();
        mLocationTrackingTaskRunnable = new Runnable() {
            public void run() {
                doLocationTracking();
                // FIXME: Is this needed for a lambda user ?
                //doFetchCalibrationPoints();
            }
        };

        startTracking();

        if (savedInstanceState != null) {

            // restore the map fragment's instance
            mMapFragment = (MapFragment) getFragmentManager().findFragmentByTag(TAG_MAP_FRAGMENT);

            // if we were on the map fragment, show it
            if (mMapFragment != null) {

                getFragmentManager().beginTransaction()
                        .replace(R.id.container, mMapFragment, TAG_MAP_FRAGMENT)
                        .commit();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        stopTracking();

        // stop the traffic generator
        if(mDummyNetTrafficGenerator.isRunning())
            mDummyNetTrafficGenerator.stop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mSettingsChanged) {

            if(mMapFragment != null) {
                getFragmentManager().beginTransaction()
                        .remove(mMapFragment)
                        .commit();
                mMapFragment = null;
            }

            mSettingsChanged = false;
        }

        if(mMapFragment != null) {
            mMapFragment.invalidateOverlay();

            // update the map information in the drawer
            mNavigationDrawerFragment.updateMapInformations();
        }

        startTracking();

        if(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("preference_generate_dummy_traffic", false))
            // restart the traffic generator
            mDummyNetTrafficGenerator.start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // save the drawer state
        outState.putBoolean("drawerState", mNavigationDrawer.isDrawerOpen(mNavigationDrawerFragment.getView()));

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // restore the drawer state
        if (!savedInstanceState.getBoolean("drawerState"))
            closeNavigationDrawer();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSettingsChanged = true;
    }

    public void openNavigationDrawer() {
        mNavigationDrawer.openDrawer(mNavigationDrawerFragment.getView());
    }

    public void closeNavigationDrawer() {
        mNavigationDrawer.closeDrawer(mNavigationDrawerFragment.getView());
    }

    public void showSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public LocationAPI getLocationAPI() {
        return mLocationAPI;
    }

    public LocationAPI.MapData getMapData() {
        if(mMapFragment != null)
            return mMapFragment.getMapData();
        return null;
    }


    public void startTracking() {
        if(mLocationTrackingTaskTimer != null)
            mLocationTrackingTaskTimer.cancel();
        mLocationTrackingTaskTimer = new Timer();
        mLocationTrackingTaskTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mLocationTrackingTaskHandler.post(mLocationTrackingTaskRunnable);
            }
        }, 0, LOCATION_TRACKING_INTERVAL);
    }
    public void stopTracking() {
        if(mLocationTrackingTask != null && mLocationTrackingTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLocationTrackingTask.cancel(true);
        }
        mLocationTrackingTask = null;

        if (mLocationTrackingTaskTimer != null) {
            mLocationTrackingTaskTimer.cancel();
            mLocationTrackingTaskTimer = null;
        }
    }

    public void doLocationTracking() {
        if (mLocationTrackingTask == null || mLocationTrackingTask.getStatus() == AsyncTask.Status.FINISHED) {
            mLocationTrackingTask = new LocationTrackingTask();
            mLocationTrackingTask.execute();
        }
    }
    public void doFetchCalibrationPoints() {
        if (mFetchCalibrationPointsTask == null || mFetchCalibrationPointsTask.getStatus() == AsyncTask.Status.FINISHED) {
            mFetchCalibrationPointsTask = new FetchCalibrationPointsTask();
            mFetchCalibrationPointsTask.execute();
        }
    }

    private class FetchCalibrationPointsTask extends AsyncTask<Void, Integer, Void> {

        public FetchCalibrationPointsTask() {}

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if(mMapFragment != null && mMapFragment.getMapData() != null)
                    mMapFragment.setCalibrationPoints(mLocationAPI.getCalibrationPoints(mMapFragment.getMapData().getID()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if(mMapFragment != null)
                mMapFragment.invalidateOverlay();
        }
    }

    private class LocationTrackingTask extends AsyncTask<Void, Integer, Void> {

        private int mStatus = 0;

        public LocationTrackingTask() { }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                publishProgress(10);

                LocationAPI.Location location = mLocationAPI.getLocation();
                LocationAPI.MapData currentMapData = null;

                if(location.status == -1) {
                    mStatus = -2;
                    return null;
                }

                if(mMapFragment != null)
                    currentMapData = mMapFragment.getMapData();

                // TODO map changed, download and recreate a map fragment
                if(currentMapData == null || location.mapID != currentMapData.getID()) {

                    if(mMapFragment != null) {
                        getFragmentManager().beginTransaction()
                                .remove(mMapFragment)
                                .commit();
                        mMapFragment = null;
                    }

                    publishProgress(0);

                    HashMap<Integer, LocationAPI.MapData> data = mLocationAPI.getMaps();

                    if(data != null) {
                        LocationAPI.MapData mapData = data.get(location.mapID);

                        if (mapData != null) {

                            publishProgress(1);

                            Bitmap mapBitmap = mLocationAPI.getBitmapOfMap(location.mapID);
                            //Bitmap mapBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tutorial_leftarrow);
                            mapData.setBitmap(mapBitmap);

                            publishProgress(2);

                            // create a new map fragment with the map data
                            mMapFragment = MapFragment.newInstance(mapData);

                            // show the fragment
                            getFragmentManager().beginTransaction()
                                    .replace(R.id.container, mMapFragment, TAG_MAP_FRAGMENT)
                                    .commit();

                            publishProgress(3);

                            mStatus = 1;

                        } else { mStatus = -3; }
                    } else { mStatus = -3;  }


                // TODO same map, update the user location only
                } else {

                    if(location.status == 0) {
                        mMapFragment.setMapUserLocation(location.getPoint());
                        mMapFragment.setMapUserLocationInvalid(false);
                    } else {
                        mMapFragment.setMapUserLocationInvalid(true);
                    }

                    mStatus = 2;
                }

            } catch (IOException e) {
                e.printStackTrace();
                mStatus = -1;
            }

            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 10:
                    mStatusText.setText("Waiting for location");
                    break;
                case 0:
                    // update the map information in the drawer
                    mNavigationDrawerFragment.updateMapInformations();

                    mStatusText.setText("Fetching for map information");
                    break;
                case 1:
                    mStatusText.setText("Downloading the map image");
                    break;
                case 2:
                    mStatusText.setText("Done");
                    break;
                case 3:
                    closeNavigationDrawer();
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (mStatus) {
                case 0:
                    if (mMapFragment != null)
                        mMapFragment.updateUserLocationDot(true);
                    break;
                case -1:
                    Toast.makeText(getApplicationContext(), "Unable to contact the server", Toast.LENGTH_SHORT).show();
                    break;
                case -2:
                    Toast.makeText(getApplicationContext(), "Unable to locate at the moment", Toast.LENGTH_SHORT).show();
                    break;
                case -3:
                    Toast.makeText(getApplicationContext(), "Unable to fetch map information", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }

            mNavigationDrawerFragment.updateMapInformations();
        }
    }
}
