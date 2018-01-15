package com.koncle.imagemanagement.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koncle.imagemanagement.R;

/**
 * Created by 10976 on 2018/1/12.
 */

public class MapFragment extends Fragment implements HasName {
    private String name;

    public static Fragment newInstance(String name, Operater operater) {
        MapFragment f = new MapFragment();
        f.setName(name);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.map_fragment, null);

        return view;
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
