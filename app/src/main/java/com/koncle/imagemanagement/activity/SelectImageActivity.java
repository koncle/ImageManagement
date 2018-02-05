package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.FolderSpinnerAdapter;
import com.koncle.imagemanagement.adapter.ImageSelectAdaptor;
import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

/**
 * Created by 10976 on 2018/1/18.
 */

public class SelectImageActivity extends AppCompatActivity {

    public static final int SELECTED_IMAGE_DATA = -3;
    public static final String SELECTED_IMAGES = "singleImages";
    private RecyclerView recyclerView;
    private ImageSelectAdaptor imageAdaptor;
    private Toolbar toolbar;
    private Button complete;
    private Button show;
    private Spinner spinner;
    private Map<String, Folder> folderMap;
    private List<String> folderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.image_select_outer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        findViews();
        initData();
        initSpinner();

        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setDuration(300);
        getWindow().setEnterTransition(slide);

        initRecyclerView();

        if (savedInstanceState != null) {
            List<Image> values = savedInstanceState.getParcelableArrayList("values");
            Log.w("Selected on", "get " + values.size());
            imageAdaptor.createSelctedImage(values);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Map<String, Image> selections = imageAdaptor.getSelectedImages();
        List<Image> values = new ArrayList<>();
        Log.w("Selected on", "put " + values.size());
        values.addAll(selections.values());
        outState.putParcelableArrayList("values", (ArrayList<? extends Parcelable>) values);
    }

    private void findViews() {
        toolbar = findViewById(R.id.select_image_toolbar);
        show = findViewById(R.id.image_select_show);
        complete = findViewById(R.id.image_select_finish);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnImages();
                onBackPressed();
            }
        });

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.selectAll();
            }
        });

        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void returnImages() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(SELECTED_IMAGES, (ArrayList<? extends Parcelable>) imageAdaptor.getSelections());
        intent.putExtras(bundle);
        setResult(SELECTED_IMAGE_DATA, intent);
    }

    private void initData() {
        folderMap = new HashMap<>();
        List<Folder> folderImages = ImageService.getNewAllFolders();
        for (int i = 0; i < folderImages.size(); i++) {
            Folder folder = folderImages.get(i);

            // get image paths
            List<Image> images;
            if (i == 0) {
                images = ImageService.getImages();
                folder.setImages(images);
            } else {
                images = folder.getImages();
            }

            List<String> imagePaths = new ArrayList<>();
            for (Image image1 : images) {
                imagePaths.add(image1.getPath());
            }

            // add
            folderMap.put(folder.getName(), folder);
        }
    }

    private void initSpinner() {
        spinner = findViewById(R.id.image_select_spinner);
        final FolderSpinnerAdapter fsa = new FolderSpinnerAdapter(this, folderMap);
        folderList = fsa.getFolderList();
        Collections.sort(folderList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int size1 = folderMap.get(o1).getImages().size();
                int size2 = folderMap.get(o2).getImages().size();
                if (size1 > size2) {
                    return -1;
                } else if (size1 == size2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
        spinner.setAdapter(fsa);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageAdaptor.setData(folderMap.get(folderList.get(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.image_select_recycler_view);
        GridLayoutManager gridLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            gridLayoutManager = new GridLayoutManager(this, 4);
        } else {
            gridLayoutManager = new GridLayoutManager(this, 6);
        }
        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdaptor = new ImageSelectAdaptor(this, gridLayoutManager, folderMap.get(0));
        recyclerView.setAdapter(imageAdaptor);

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getItemAnimator().setChangeDuration(0);
    }
}
