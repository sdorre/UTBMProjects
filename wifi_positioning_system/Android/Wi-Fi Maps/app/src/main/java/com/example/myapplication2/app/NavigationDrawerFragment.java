package com.example.myapplication2.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;


public class NavigationDrawerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private TextView mMapNameText;
    private TextView mMapDimensionsText;

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

        mMapNameText = (TextView) view.findViewById(R.id.map_name);
        mMapDimensionsText = (TextView) view.findViewById(R.id.map_dimensions);

        updateMapInformations();

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
    }

    public interface OnFragmentInteractionListener {
        void showSettings();
        LocationAPI.MapData getMapData();
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

    public void updateMapInformations() {
        if(mMapNameText == null || mMapDimensionsText == null)
            return;

        if(mListener != null) {
            LocationAPI.MapData mapData = mListener.getMapData();

            if (mapData != null) {
                mMapNameText.setText(mapData.getName());
                mMapDimensionsText.setText(mapData.getWidthInMeters() + "x" + mapData.getHeightInMeters() + "m");
                return;
            }
        }

        mMapNameText.setText("None");
        mMapDimensionsText.setText("Unknown");
    }

}
