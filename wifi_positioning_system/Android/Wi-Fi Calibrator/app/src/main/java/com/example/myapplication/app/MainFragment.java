package com.example.myapplication.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class MainFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        // initialize the drawer button to open the navigation drawer on click
        ImageView drawerButton = (ImageView) view.findViewById(R.id.navigation_drawer_button);
        drawerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.openNavigationDrawer();
            }
        });

        return view;
    }

    public interface OnFragmentInteractionListener {
        void openNavigationDrawer();
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
}
