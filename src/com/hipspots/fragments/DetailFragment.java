package com.hipspots.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.hipspots.R;
import com.hipspots.activities.DetailsActivity;

public class DetailFragment extends LocalActivityManagerFragment {
    
    private TabHost mTabHost;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {        
        View view = inflater.inflate(R.layout.my_map_fragment, container, false);
        mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getLocalActivityManager());
        
        TabSpec tab = mTabHost.newTabSpec("tab")
                              .setIndicator("tab")
                              .setContent(new Intent(getActivity(), DetailsActivity.class));
        mTabHost.addTab(tab);        
        return view;
    }
}
