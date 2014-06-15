package com.example.myapplication.app;

import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapFragment extends Fragment implements MapView.OnMapEventListener {

    private OnFragmentInteractionListener mListener;

    private static int FOOTER_ANIMATION_DURATION = 200;
    private static int USER_LOCATION_DOT_ANIMATION_DURATION = 200;
    private static int LOCATION_TRACKING_INTERVAL = 10000;

    private LocationAPI.MapData mMapData;
    private List<LocationAPI.CalibrationPoint> mCalibrationPoints;

    private MapView mMapView;
    private MapOverlay mMapOverlay;

    private boolean mMapUserLocationInvalid = false;
    private PointF mMapUserLocation;
    private ImageView mMapUserLocationDot;

    private PointF mMapCursorPosition = null;
    private ImageView mMapCursor;

    private LinearLayout mFooter;
    private FrameLayout mFooterContainer;
    private ImageView mFooterShadow;

    private FetchCalibrationPointsTask mFetchCalibrationPointsTask;
    private LocationTrackingTask mLocationTrackingTask;
    private Timer mLocationTrackingTaskTimer;
    private Handler mLocationTrackingTaskHandler;
    private Runnable mLocationTrackingTaskRunnable;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance(LocationAPI.MapData mapData) {
        MapFragment mapFragment = new MapFragment();

        // set the map data on the new instance
        mapFragment.setMapData(mapData);

        return mapFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retain this fragment across configuration changes
        setRetainInstance(true);

        // initialize the location tracking
        mLocationTrackingTaskHandler = new Handler();
        mLocationTrackingTaskRunnable = new Runnable() {
            public void run() {
                doLocationTracking();
                doFetchCalibrationPoints();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // initialize the drawer button to open the navigation drawer on click
        ImageView drawerButton = (ImageView) view.findViewById(R.id.navigation_drawer_button);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openNavigationDrawer();
            }
        });

        // initialize the center button to reset the map matrix on click
        ImageView centerButton = (ImageView) view.findViewById(R.id.center_button);
        centerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMapView.moveToCenter();
            }
        });

        // get footer elements
        mFooter = (LinearLayout) view.findViewById(R.id.footer);
        mFooterContainer = (FrameLayout) view.findViewById(R.id.footer_container);
        mFooterShadow = (ImageView) view.findViewById(R.id.footer_shadow);

        // initialize the map view with the map data and register this to listen for events
        mMapView = (MapView) view.findViewById(R.id.map_view);
        mMapView.setMapImage(mMapData.getBitmap());
        mMapView.addListener(this);

        // get other elements
        mMapCursor = (ImageView) view.findViewById(R.id.map_cursor);
        mMapUserLocationDot =  (ImageView) view.findViewById(R.id.user_location_dot);
        mMapOverlay = (MapOverlay) view.findViewById(R.id.map_overlay);
        mMapOverlay.setMapView(mMapView);
        mMapOverlay.setCalibrationPoints(mCalibrationPoints);

        // hide the footer by default
        mFooter.setTranslationY(getResources().getDimension(R.dimen.footer_container_height));

        // FIXME Replace this when the custom view is done
        Button calibration_button = (Button) view.findViewById(R.id.calibration_button);
        calibration_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SendCalibrationTask().execute();
            }
        });

        // fetch the calibration points
        doFetchCalibrationPoints();

        // start the user tracking task
        if(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean("preference_locate_me", false))
            startTracking();

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        // stop the user tracking task
        stopTracking();
    }

    @Override
    public void onResume() {
        super.onResume();

        // restore the map cursor and the footer state
        if(mMapCursorPosition != null) {
            mFooter.setTranslationY(0);
            mFooterShadow.setVisibility(View.VISIBLE);
        }

        // restore the calibration points on the map
        mMapOverlay.setCalibrationPoints(mCalibrationPoints);
        mMapOverlay.postInvalidate();

        // restore the user location dot on the map
        updateUserLocationDot(false);

        // reset tracking variables
        resetTracking();

        // restore the user tracking task
        if(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean("preference_locate_me", false))
            startTracking();
    }

    public void onMapClick(double touchX, double touchY) {
        mMapCursorPosition = null;

        // refresh the map view
        mMapView.postInvalidate();

        // hide the footer
        setFooterVisiblity(View.INVISIBLE);
    }

    @Override
    public void onMapLongClick(PointF touchPoint) {
        mMapCursorPosition = touchPoint;

        // refresh the map view
        mMapView.postInvalidate();

        // show the footer
        setFooterVisiblity(View.VISIBLE);
    }

    @Override
    public void onMapMatrixUpdate(Matrix matrix) {

        // update the map cursor position on the screen
        updateMapCursor();

        // update the user location dot
        updateUserLocationDot(false);
    }


    public interface OnFragmentInteractionListener {
        void openNavigationDrawer();
        LocationAPI getLocationAPI();
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

    public void setFooterVisiblity(int visibility) {
        final int borderVisibility = visibility;

        // start a sliding animation
        mFooter.animate()
                .translationY(visibility == View.VISIBLE ? 0 : getResources().getDimension(R.dimen.footer_container_height))
                .setDuration(FOOTER_ANIMATION_DURATION)
                .setListener(new Animator.AnimatorListener() {

                    public void onAnimationStart(Animator animation) { }
                    public void onAnimationCancel(Animator animation) { }
                    public void onAnimationRepeat(Animator animation) { }

                    public void onAnimationEnd(Animator animation) {
                        // hide the layout's border
                        mFooterShadow.setVisibility(borderVisibility);
                    }

                });
    }

    private void updateMapCursor() {

        // get screen relative coordinates
        PointF cursorPoint = mMapView.mapPointFromMap(mMapCursorPosition, true);

        if(cursorPoint != null) {

            // show the map cursor
            mMapCursor.setVisibility(View.VISIBLE);

            // compute it's new coordinates on the screen
            float translationX = cursorPoint.x - mMapCursor.getWidth() / 2.0f;
            float translationY = cursorPoint.y - mMapCursor.getHeight();

            // apply the new coordinates
            mMapCursor.setTranslationX(translationX);
            mMapCursor.setTranslationY(translationY);

        } else {

            // hide the map cursor
            mMapCursor.setVisibility(View.INVISIBLE);
        }
    }

    private void updateUserLocationDot(boolean animate) {

        // get screen relative coordinates
        PointF cursorPoint = mMapView.mapPointFromMap(mMapUserLocation, true);

        if(cursorPoint != null) {

            // show the location dot
            mMapUserLocationDot.setVisibility(View.VISIBLE);

            // compute it's new coordinates on the screen
            float translationX = cursorPoint.x - mMapUserLocationDot.getWidth() / 2.0f;
            float translationY = cursorPoint.y - mMapUserLocationDot.getHeight() / 2.0f;

            // apply the new coordinates
            if(animate) {
                mMapUserLocationDot.animate()
                        .translationX(translationX)
                        .translationY(translationY)
                        .setDuration(USER_LOCATION_DOT_ANIMATION_DURATION);
            } else {
                mMapUserLocationDot.setTranslationX(translationX);
                mMapUserLocationDot.setTranslationY(translationY);
            }

            // change the user location dot graphics according to the location status
            if(mMapUserLocationInvalid) {
                mMapUserLocationDot.setImageResource(R.drawable.gray_dot);
                mMapUserLocationDot.setBackgroundDrawable(null);
            } else {
                mMapUserLocationDot.setImageResource(R.drawable.blue_dot);
                mMapUserLocationDot.setBackgroundResource(R.drawable.blue_dot_glow);
            }

        } else {

            // hide the location dot
            mMapUserLocationDot.setVisibility(View.INVISIBLE);
        }
    }


    public LocationAPI.MapData getMapData() {
        return mMapData;
    }

    public void setMapData(LocationAPI.MapData mapData) {
        mMapData = mapData;
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
        mLocationTrackingTaskHandler.removeCallbacks(mLocationTrackingTaskRunnable);
        if (mLocationTrackingTaskTimer != null) {
            mLocationTrackingTaskTimer.cancel();
            mLocationTrackingTaskTimer = null;
        }
    }

    public void resetTracking() {
        mMapUserLocationInvalid = false;
        mMapUserLocation = null;
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

    private class SendCalibrationTask extends AsyncTask<Void, Integer, Void> {

        PointF mPoint;
        private int mStatus = 0;

        public SendCalibrationTask() {}

        @Override
        protected Void doInBackground(Void... params) {
            publishProgress(0);

            try {
                mPoint = mMapCursorPosition;
                mStatus = mListener.getLocationAPI().sendCalibrationPoint(mMapData.getID(), mPoint);

            } catch (IOException e) {
                mStatus = -1;
            }

            publishProgress(1);
            return null;
        }

        protected void onProgressUpdate(Integer... progress) { }

        @Override
        protected void onPostExecute(Void result) {
            switch (mStatus) {
                case 0:
                    // refresh the calibration points on the map
                    doFetchCalibrationPoints();

                    // reset the map cursor position
                    mMapCursorPosition = null;
                    updateMapCursor();

                    // hide the footer
                    setFooterVisiblity(View.INVISIBLE);
                    break;

                case -1:
                    // display the error message
                    Toast.makeText(getActivity(), R.string.unable_send_calibration, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private class FetchCalibrationPointsTask extends AsyncTask<Void, Integer, Void> {

        public FetchCalibrationPointsTask() {}

        @Override
        protected Void doInBackground(Void... params) {

            try {
                if(mListener != null && mMapData != null)
                    mCalibrationPoints = mListener.getLocationAPI().getCalibrationPoints(mMapData.getID());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMapOverlay.setCalibrationPoints(mCalibrationPoints);
            mMapOverlay.postInvalidate();
        }
    }

    private class LocationTrackingTask extends AsyncTask<Void, Integer, Void> {

        public LocationTrackingTask() { }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                LocationAPI.Location location = mListener.getLocationAPI().getLocation();

                if (location.status == 0) {

                    // validate the response data
                    if(location.mapID == mMapData.getID()) {

                        // update the location coordinates
                        mMapUserLocation = location.getPoint();

                        // current location is valid
                        mMapUserLocationInvalid = false;

                    } else {
                        // reset the user location variables
                        mMapUserLocationInvalid = false;
                        mMapUserLocation = null;
                    }

                } else {

                    if(mMapUserLocation != null)
                        // error get location from server, last location is used
                        mMapUserLocationInvalid = true;
                }

            } catch (IOException e) { }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
             updateUserLocationDot(true);
        }
    }
}
