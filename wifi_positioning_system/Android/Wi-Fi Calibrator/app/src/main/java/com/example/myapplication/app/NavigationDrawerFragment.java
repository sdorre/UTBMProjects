package com.example.myapplication.app;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NavigationDrawerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private LoadMapsTask mLoadMapsTask;
    private HashMap<Integer, LocationAPI.MapData> mMapDataList;
    private LinearLayout mMapList;

    private LoadMapTask mLoadMapTask;
    private FrameLayout mCurrentMapItem;
    private int mCurrentMapID = -1;

    private ImageView mGetMapsIcon;
    private TextView mGetMapsText;
    private ProgressBar mGetMapsProgessBar;

    private CalibrationResetTask mCalibrationResetTask;
    LinearLayout mCalibrationResetButton;
    private ImageView mCalibrationResetIcon;
    private TextView mCalibrationResetText;
    private ProgressBar mCalibrationResetProgessBar;

    private FrameLayout mCalibrationPointLayerItem;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retain this fragment across configuration changes
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        // get the map list
        mMapList = (LinearLayout) view.findViewById(R.id.map_list);

        // get the elements of the "calibration reset" button
        mCalibrationResetIcon = (ImageView) view.findViewById(R.id.calibration_reset_icon);
        mCalibrationResetText = (TextView) view.findViewById(R.id.calibration_reset_text);
        mCalibrationResetProgessBar = (ProgressBar) view.findViewById(R.id.calibration_reset_progess);
        mCalibrationResetButton = (LinearLayout) view.findViewById(R.id.calibration_reset_button);

        // initialize the "calibration reset" button to fetch the list of calibration points from the server
        mCalibrationResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCalibrationResetTask == null || mCalibrationResetTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mCalibrationResetTask = new CalibrationResetTask();
                    mCalibrationResetTask.execute();
                }
            }
        });

        // get the elements of the "get maps" button
        mGetMapsIcon = (ImageView) view.findViewById(R.id.get_maps_icon);
        mGetMapsText = (TextView) view.findViewById(R.id.get_maps_text);
        mGetMapsProgessBar = (ProgressBar) view.findViewById(R.id.get_maps_progess);
        // initialize the "get maps" button to fetch the list of available maps from the server
        LinearLayout getMapsButton = (LinearLayout) view.findViewById(R.id.get_maps_button);
        getMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLoadMapsTask == null || mLoadMapsTask.getStatus() == AsyncTask.Status.FINISHED) {
                    mLoadMapsTask = new LoadMapsTask();
                    mLoadMapsTask.execute();
                }
            }
        });

        // initialize the settings button to start the settings intent
        LinearLayout settingsButton = (LinearLayout) view.findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    mListener.showSettings();
                }
            }
        });

        return view;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {

        // save variables locally
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    }

    @Override
    public void onResume() {
        super.onResume();

        // restore the map cursor and the footer state
        if(mMapDataList != null) {
            populateMapList();
        }
    }

    public interface OnFragmentInteractionListener {
        void showSettings();
        LocationAPI getLocationAPI();
        LocationAPI.MapData getMap();
        void setMap(LocationAPI.MapData map);
        void onResetCalibrationPoints();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void onMapListItemClick(View item, LocationAPI.MapData map) {

        // cancel previous running tasks to fetch map data
        if(mLoadMapTask != null && mLoadMapTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadMapTask.cancel(true);

            // reset the selected map element
            setCurrentMapItemProgressVisibility(View.INVISIBLE);
            resetCurrentMapItem();
        }

        // refresh UI to show the selected map
        setCurrentMapItem((FrameLayout) item);
        setCurrentMapItemProgressVisibility(View.VISIBLE);

        // start an async task to fetch map's data
        mLoadMapTask = new LoadMapTask(map);
        mLoadMapTask.execute();
    }

    public void cancelRunningTasks() {

        if(mLoadMapTask != null && mLoadMapTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadMapTask.cancel(true);
            mLoadMapTask = null;

            setCurrentMapItemProgressVisibility(View.INVISIBLE);
            resetCurrentMapItem();
        }

        if(mLoadMapsTask != null && mLoadMapsTask.getStatus() != AsyncTask.Status.FINISHED) {
            mLoadMapsTask.cancel(true);
            mLoadMapsTask = null;
        }

        if(mCalibrationResetTask != null && mCalibrationResetTask.getStatus() != AsyncTask.Status.FINISHED) {
            mCalibrationResetTask.cancel(true);
            mCalibrationResetTask = null;
        }
    }

    public void setCurrentMapItem(FrameLayout item) {
        if(mCurrentMapItem == item)
            return;

        // reset first
        if(mCurrentMapItem != null)
            resetCurrentMapItem();

        if(item != null) {
            mCurrentMapItem = item;

            // prevent click events
            mCurrentMapItem.setClickable(false);

            // show the selected element
            mCurrentMapItem.setBackgroundResource(R.drawable.button_draweritem_selected_selector);
        }
    }

    public void setCurrentMapItemProgressVisibility(int visibility) {
        if(mCurrentMapItem != null) {
            ProgressBar progressBar = (ProgressBar) mCurrentMapItem.findViewById(R.id.progress);
            progressBar.setVisibility(visibility);
        }
    }

    public void setCalibrationResetButtonVisibility(int visibility) {
        mCalibrationResetButton.setVisibility(visibility);
    }

    public void resetCurrentMapItem() {
        if(mCurrentMapItem != null) {

            // reset the element on the UI
            mCurrentMapItem.setClickable(true);
            mCurrentMapItem.setBackgroundResource(R.drawable.button_draweritem_selector);
            setCurrentMapItemProgressVisibility(View.INVISIBLE);

            mCurrentMapItem = null;
        }
    }

    public void resetMapList(boolean refresh_ui) {

        // reset variables
        mMapDataList = null;
        mCurrentMapItem = null;
        mCurrentMapID = -1;

        // reset the UI elements
        if(refresh_ui)
            populateMapList();
    }

    public void populateMapList() {

        // remove all children in the view
        mMapList.removeAllViews();

        // no map to display, show a dummy element
        if(mMapDataList == null || mMapDataList.size() == 0) {

            // create a dummy item and prevent click events
            FrameLayout item = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.item_map_list, null);
            item.setClickable(false);

            // set the dummy item's title
            TextView mapTitle = (TextView) item.findViewById(R.id.title);
            mapTitle.setText(R.string.no_maps_display);

            // add it in the list view
            mMapList.addView(item);

            // reset the map list variables only
            resetMapList(false);

        } else {

            // instantiate each item in the list
            for(final LocationAPI.MapData mapData : mMapDataList.values()) {

                // create an item and get the title element
                final FrameLayout item = (FrameLayout) getActivity().getLayoutInflater().inflate(R.layout.item_map_list, null);
                TextView mapTitle = (TextView) item.findViewById(R.id.title);

                // set it's title to the map's name
                mapTitle.setText(mapData.getName());

                // bind the click listener
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMapListItemClick(item, mapData);
                    }
                });

                // this item is the chosen one! help him conquer the realm.
                if(mapData.getID() == mCurrentMapID) {
                    setCurrentMapItem(item);
                }

                // add the item to the list view
                mMapList.addView(item);
            }
        }
    }

    class LoadMapsTask extends AsyncTask<Void, Integer, Void> {

        public LoadMapsTask() {}

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(0);

            try {
                mMapDataList = mListener.getLocationAPI().getMaps();
            } catch (IOException e) {
                publishProgress(1);
                mMapDataList = null;
            }

            publishProgress(2);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 0:
                    mGetMapsIcon.setVisibility(View.GONE);
                    mGetMapsProgessBar.setVisibility(View.VISIBLE);
                    mGetMapsText.setText(R.string.loading_maps);
                    break;
                case 1:
                    Toast.makeText(getActivity(), R.string.unable_get_maps, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    mGetMapsIcon.setVisibility(View.VISIBLE);
                    mGetMapsProgessBar.setVisibility(View.GONE);
                    mGetMapsText.setText(R.string.get_maps);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mGetMapsIcon.setVisibility(View.VISIBLE);
            mGetMapsProgessBar.setVisibility(View.GONE);
            mGetMapsText.setText(R.string.get_maps);
        }

        @Override
        protected void onPostExecute(Void result) {
            populateMapList();
        }
    }

    class LoadMapTask extends AsyncTask<Void, Integer, Void> {

        private int mStatus = 0;
        private LocationAPI.MapData mMapData;

        public LoadMapTask(LocationAPI.MapData mapData) {
            mMapData = mapData;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Bitmap mapBitmap = mListener.getLocationAPI().getBitmapOfMap(mMapData.getID());
                mMapData.setBitmap(mapBitmap);
                mStatus = 1;
            } catch (IOException e) {
                mStatus = -1;
            }

            publishProgress(1);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 1:
                    setCurrentMapItemProgressVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (mStatus) {
                case 1:
                    mCurrentMapID = mMapData.getID();
                    mListener.setMap(mMapData);
                    break;
                case -1:
                    mCurrentMapID = -1;
                    resetCurrentMapItem();
                    Toast.makeText(getActivity(), R.string.unable_get_map, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    class CalibrationResetTask extends AsyncTask<Void, Integer, Void> {

        private String mResult;
        private int mStatus = 0;

        public CalibrationResetTask() {}

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(0);

            try {
                if(mListener != null) {
                    LocationAPI.MapData mapData = mListener.getMap();
                    if(mapData != null) {
                        // TODO decode the status
                        mResult = mListener.getLocationAPI().resetCalibration(mapData.getID());
                        mStatus = 1;
                    } else {
                        mStatus = -2;
                    }
                } else {
                    mStatus= -3;
                }
            } catch (IOException e) {
                mStatus = -1;
            }

            publishProgress(1);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) {
            switch (progress[0]) {
                case 0:
                    mCalibrationResetIcon.setVisibility(View.GONE);
                    mCalibrationResetProgessBar.setVisibility(View.VISIBLE);
                    mCalibrationResetText.setText(R.string.loading_calibration_reset);
                    break;
                case 1:
                    mCalibrationResetIcon.setVisibility(View.VISIBLE);
                    mCalibrationResetProgessBar.setVisibility(View.GONE);
                    mCalibrationResetText.setText(R.string.calibration_reset);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            mCalibrationResetIcon.setVisibility(View.VISIBLE);
            mGetMapsProgessBar.setVisibility(View.GONE);
            mCalibrationResetText.setText(R.string.calibration_reset);
        }

        @Override
        protected void onPostExecute(Void result) {
            switch (mStatus) {
                case 1:
                    mListener.onResetCalibrationPoints();
                    break;
                case 0:
                    Toast.makeText(getActivity(), mResult, Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                case -2:
                case -3:
                    Toast.makeText(getActivity(), R.string.unable_calibration_reset, Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    }
}
