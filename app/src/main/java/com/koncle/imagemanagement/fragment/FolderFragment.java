package com.koncle.imagemanagement.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.FolderRecyclerViewAdapter;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class FolderFragment extends Fragment implements HasName {
    private String name;

    public static Fragment newInstance(String name) {
        FolderFragment f = new FolderFragment();
        f.setName(name);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.folder_fragment, null);
        RecyclerView recyclerView = view.findViewById(R.id.folder_recycler);
        try {

            List<Image> images = ImageService.getFolders();

            FolderRecyclerViewAdapter folderAdapter = new FolderRecyclerViewAdapter(this.getContext(), images);
            recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
            recyclerView.setAdapter(folderAdapter);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
