package com.example.owner.sunshine.app;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private ArrayAdapter<String> mForcastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String[] forcastArray = {
                "Today - Sunny - 88/83",
                "Tomorrow - Foggy - 78/58",
                "Weds - Cloudy - 72/62",
                "Thursday - Astroids - 73/48",
                "Friday - Heavy Rain - 63/58",
                "saturday - Rain - 60/50",
                "Sunday - Sunny - 88/68"
        };
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> weekForcast = new ArrayList<String>(Arrays.asList(forcastArray));

        mForcastAdapter = new ArrayAdapter<String>(
                        //the curetn context
                        getActivity(),
                        //ID of the list item layout
                        R.layout.list_item_forcast,
                        //Id of the text view to populate
                        R.id.list_item_forcast_textview,
                        //Forcast data
                        weekForcast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forcast);
        listView.setAdapter(mForcastAdapter);

        return rootView;
    }
}
