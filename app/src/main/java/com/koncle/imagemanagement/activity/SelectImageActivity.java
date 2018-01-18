package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.FolderSpinnerAdapter;
import com.koncle.imagemanagement.adapter.ImageAdaptor;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

/**
 * Created by 10976 on 2018/1/18.
 */

public class SelectImageActivity extends AppCompatActivity implements ImageAdaptor.ModeOperator {

    public static final int RESULT_CODE = -3;
    public static final String IMAGES = "images";
    private RecyclerView recyclerView;
    private ImageAdaptor imageAdaptor;
    private Toolbar toolbar;
    private Button complete;
    private Button show;
    private Spinner spinner;
    private Map<String, List<String>> folderStringMap;
    private Map<String, List<Image>> folderImageMap;
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
        initRecyclerView();
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
                finish();
            }
        });

        toolbar.setNavigationIcon(R.drawable.back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void returnImages() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(IMAGES, (ArrayList<? extends Parcelable>) imageAdaptor.getSelections());
        intent.putExtras(bundle);
        setResult(RESULT_CODE, intent);
    }

    private void initData() {
        folderStringMap = new HashMap<>();
        folderImageMap = new HashMap<>();
        List<Image> folderImages = ImageService.getFolders();
        for (Image image : folderImages) {
            // get image paths
            List<Image> images = ImageService.getImagesFromSameFolders(image.getFolder());
            List<String> imagePaths = new ArrayList<>();
            for (Image image1 : images) {
                imagePaths.add(image1.getPath());
            }

            // add
            folderStringMap.put(image.getFolder(), imagePaths);
            folderImageMap.put(image.getFolder(), images);
        }
    }

    private void initSpinner() {
        spinner = findViewById(R.id.image_select_spinner);
        final FolderSpinnerAdapter fsa = new FolderSpinnerAdapter(this, folderStringMap);
        folderList = fsa.getFloderList();
        spinner.setAdapter(fsa);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                imageAdaptor.setData(folderImageMap.get(folderList.get(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.image_select_recycler_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdaptor = new ImageAdaptor(this, gridLayoutManager, folderImageMap.get(folderList.get(0)));
        imageAdaptor.setOperater(this);
        imageAdaptor.enterSelectMode();
        recyclerView.setAdapter(imageAdaptor);
        recyclerView.setItemViewCacheSize(0);
    }

    @Override
    public void exitSelectMode() {
    }

    @Override
    public void enterSelectMode() {
    }

    @Override
    public void refreshData() {
    }

    @Override
    public void showSelectedNum(int num) {
    }
}
