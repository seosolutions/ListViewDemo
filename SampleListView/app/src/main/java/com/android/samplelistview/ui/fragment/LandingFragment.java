package com.android.samplelistview.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.samplelistview.R;
import com.android.samplelistview.adapter.CustomRecycleViewAdapter;
import com.android.samplelistview.model.Country;
import com.android.samplelistview.network.GetJSONDataHandler;
import com.android.samplelistview.network.ResponseHandler;
import com.android.samplelistview.ui.LandingActivity;
import com.android.samplelistview.util.Utility;

/**
 * Created by karthik_kulkarni01 on 5/20/2016.
 * Fragment to show the list of items from Country List
 */
public class LandingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerView;
    private Country countryDetails;
    private Activity parentActivity;
    private CustomRecycleViewAdapter customRecycleViewAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;


    // Initialise all the view components
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.landing_fragment_layout, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.landing_fragment);
        swipeRefreshLayout.setOnRefreshListener(this);

        return rootView;
    }

    //Make a webservice api call to get the data.
    @Override
    public void onStart() {
        super.onStart();
        getCountryDetails();
    }

    /**
     * get the context of parent activity as soon as fragment is attached to activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        parentActivity = (LandingActivity) context;

    }

    /**
     * make GetJSONDataHandler WS call to get country details.
     * Check for network connectivity before making WS call
     */
    public void getCountryDetails() {

        if (Utility.isConnectedToInternet()) {
            if (!swipeRefreshLayout.isRefreshing()) {
                showSpinner();
            }
            GetJSONDataHandler getJSONDataHandler = new GetJSONDataHandler(parentActivity,
                    countryDataResponseHandler);
            getJSONDataHandler.sendRequest();
        } else {
            Toast.makeText(parentActivity, "Unable to connect to Internet.Please check " +
                            "connectivity",
                    Toast
                            .LENGTH_LONG)
                    .show();
            hideSpinner();
        }
    }

    /**
     * ResponseHandler object to get back the data from GetJSONDataHandler.
     * Since Network calls are never executed on main(UI) thread, on return it'll added back to
     * UI Thread using parentActivity.runOnUiThread
     */
    ResponseHandler countryDataResponseHandler = new ResponseHandler() {
        @Override
        //On success response
        public void onSuccess(Object response) {
            countryDetails = (Country) response;
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideSpinner();
                    loadDataToList(countryDetails);
                }
            });
        }

        //on failure response
        @Override
        public void onError(final String msg) {
            parentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(parentActivity, msg, Toast.LENGTH_LONG).show();
                    hideSpinner();
                }
            });
        }
    };


    /**
     * On receiving data from WS, load the content on the listview using custom Adapter.
     * Update the title of the application.
     */
    private void loadDataToList(Country country) {
        parentActivity.setTitle(country.getTitle());
        if (customRecycleViewAdapter != null) {
            customRecycleViewAdapter.notifyDataSetChanged();
        } else {
            customRecycleViewAdapter = new CustomRecycleViewAdapter(getActivity(), country.getRows());
            recyclerView.setAdapter(customRecycleViewAdapter);

        }
    }

    /**
     * Show spinner
     */
    private void showSpinner() {
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    /**
     * Hide spinner
     */
    private void hideSpinner() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * On start of spinner, call getCountryDetails()
     */
    @Override
    public void onRefresh() {
        getCountryDetails();
    }


}
