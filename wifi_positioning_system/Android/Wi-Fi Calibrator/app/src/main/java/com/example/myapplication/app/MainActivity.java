package com.example.myapplication.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;


public class MainActivity extends Activity implements
        MainFragment.OnFragmentInteractionListener,
        MapFragment.OnFragmentInteractionListener,
        NavigationDrawerFragment.OnFragmentInteractionListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG_MAP_FRAGMENT = "map_fragment";

    private boolean mSettingsChanged = false;

    private LocationAPI mLocationAPI;

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DrawerLayout mNavigationDrawer;

    private MainFragment mMainFragment = new MainFragment();
    private MapFragment mMapFragment;

    private DummyNetTrafficGenerator mDummyNetTrafficGenerator;

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

        // open the drawer by default
        openNavigationDrawer();

        // get preferences
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        // register this to listen for preference changes
        settings.registerOnSharedPreferenceChangeListener(this);

        // create traffic generator instance
        mDummyNetTrafficGenerator = new DummyNetTrafficGenerator(this);

        if(settings.getBoolean("preference_generate_dummy_traffic", false))
            // start the traffic generator
            mDummyNetTrafficGenerator.start();

        if (savedInstanceState != null) {

            // restore the map fragment's instance
            mMapFragment = (MapFragment) getFragmentManager().findFragmentByTag(TAG_MAP_FRAGMENT);

            // if we were on the map fragment, show it
            if (mMapFragment != null) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, mMapFragment, TAG_MAP_FRAGMENT)
                        .commit();

                // show elements related to the map fragment in the drawer
                mNavigationDrawerFragment.setCalibrationResetButtonVisibility(View.VISIBLE);
            }
        } else {

            // set the default fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mMainFragment)
                    .commit();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // cancel running tasks on the navigation drawer fragment
        mNavigationDrawerFragment.cancelRunningTasks();

        // stop the traffic generator
        if(mDummyNetTrafficGenerator.isRunning())
            mDummyNetTrafficGenerator.stop();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (mSettingsChanged) {
            // restore the default fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mMainFragment)
                    .commit();

            // hide elements related to the map fragment in the drawer
            mNavigationDrawerFragment.setCalibrationResetButtonVisibility(View.GONE);

            // cancel tasks in the navigation drawer
            mNavigationDrawerFragment.cancelRunningTasks();

            // reset the navigation drawer list
            mNavigationDrawerFragment.resetMapList(true);

            mSettingsChanged = false;
        }

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

    public LocationAPI.MapData getMap() {
        if(mMapFragment == null)
            return null;

        return mMapFragment.getMapData();
    }

    public void setMap(LocationAPI.MapData mapData) {
        if (mapData != null) {

            // create a new map fragment with the map data
            mMapFragment = MapFragment.newInstance(mapData);

            // show elements related to the map fragment in the drawer
            mNavigationDrawerFragment.setCalibrationResetButtonVisibility(View.VISIBLE);

            // show the fragment
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, mMapFragment, TAG_MAP_FRAGMENT)
                    .commit();

            // close the navigation drawer
            closeNavigationDrawer();
        }
    }

    @Override
    public void onResetCalibrationPoints() {
        if(mMapFragment != null) {
            mMapFragment.doFetchCalibrationPoints();
        }
    }
}
