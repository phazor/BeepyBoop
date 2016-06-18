package com.phazor.beepy.fragments;

import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;
import com.phazor.beepy.*;

public class ListFragment extends Fragment
{
	public ListFragment() {
		
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }
}
