package com.koncle.imagemanagement.activity;

/**
 * Created by 10976 on 2018/1/8.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.fragment.MultipleImageDialogFragment;
import com.koncle.imagemanagement.fragment.SingleImageDIalogFragment;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.List;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

public class MultiColumnImagesActivity extends AppCompatActivity implements ImageAdaptor.ModeOperator {

    private static final int ROW = 4;

    private RecyclerView recyclerView;
    private List<Image> images;
    private ImageAdaptor imageAdaptor;
    private Toolbar toolbar;
    private Toolbar hidedToolbar;
    private LinearLayout operatoins;
    private TextView title;
    private TextView complete;
    private ImageButton back;
    private RadioButton share;
    private RadioButton delete;
    private RadioButton move;
    private RadioButton tag;
    private BottomSheetBehavior bottomSheetBehavior;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.multiple_image_outer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        images = this.getIntent().getExtras().getParcelableArrayList("images");
        ImageService.recoverDaoSession(images);

        findViews();
        initMode();
        initRecyclerView();
    }

    private void findViews() {
        hidedToolbar = findViewById(R.id.hide_toolbar);
        title = findViewById(R.id.select_msg);
        complete = findViewById(R.id.select_complete);
        back = findViewById(R.id.select_back);

        recyclerView = findViewById(R.id.recyclerView);

        operatoins = findViewById(R.id.operations);
        share = findViewById(R.id.share);
        delete = findViewById(R.id.event_delete);
        move = findViewById(R.id.move);
        tag = findViewById(R.id.tag);

        bottomSheetBehavior = BottomSheetBehavior.from(operatoins);
    }

    private void initMode() {
        // init toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Show");
        setSupportActionBar(toolbar);

        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.selectAll();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.exitSelectMode();
            }
        });

        // init bottom tools
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.deleteSelectedImages();
                imageAdaptor.exitSelectMode();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.shareImages(MultiColumnImagesActivity.this, imageAdaptor.getSelections());
                imageAdaptor.exitSelectMode();
            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = imageAdaptor.getSelections();
                //  ImageUtils.moveImages(paths);
                imageAdaptor.exitSelectMode();
            }
        });

        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = imageAdaptor.getSelections();
                //ImageService.addTags(images, );
                if (images.size() > 1) {
                    MultipleImageDialogFragment dialog = MultipleImageDialogFragment.newInstance(images);
                    dialog.show(getSupportFragmentManager(), "Multi");
                } else {
                    SingleImageDIalogFragment dialog = SingleImageDIalogFragment.newInstance(images.get(0));
                    dialog.show(getSupportFragmentManager());
                }
                imageAdaptor.exitSelectMode();
            }
        });
    }

    private void initRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdaptor = new ImageAdaptor(this, gridLayoutManager, images);
        imageAdaptor.setOperater(this);
        recyclerView.setAdapter(imageAdaptor);
        recyclerView.setItemViewCacheSize(0);
    }


    public void exitSelectMode() {
        // show nomal toolbar
        toolbar.setVisibility(View.VISIBLE);
        hidedToolbar.setVisibility(View.GONE);
        setSupportActionBar(toolbar);

        // hide tools
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        //toggleMode();
        /*
         * The recyclerView will retain at least 4 images to
         * have a perfect perfomance which leads to my failure
         * to recover from select mode to nomal mode,
         * Thus I have to use this method to refresh data
         * so that the view can be redrew.
         */
        imageAdaptor.notifyDataSetChanged();
    }

    public void enterSelectMode() {
        // show hidden toolbar
        toolbar.setVisibility(View.GONE);
        hidedToolbar.setVisibility(View.VISIBLE);
        setSupportActionBar(hidedToolbar);

        // show tools
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onBackPressed() {
        // if there is no selected mode, then close the activity
        if (!imageAdaptor.exitSelectMode()) {
            super.onBackPressed();
        }
    }

    @Override
    public void refreshData() {
        /*
         * The recyclerView will retain at least 4 images to
         * have a perfect perfomance which leads to my failure
         * to recover from select mode to nomal mode,
         * Thus I have to use this method to refresh data
         * so that the view can be redrew.
         */
        imageAdaptor.notifyDataSetChanged();
    }

    @Override
    public void showSelectedNum(int num) {
        this.title.setText("Select " + num + " Pictures");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            // the glide will use cache to show image
            // so that only when user click the image and enter the singleImage mode,
            // can the system know whether the image is valid.
            case (SingleImageActivity.IMAGE_VIWER_SCROLL): {
                Log.w("Share", "return");
                int pos = data.getIntExtra("pos", 0);
                recyclerView.scrollToPosition(pos); // //scroll to next 2 rows
                if (data.getBooleanExtra("delete", false)) {
                    List<Image> images = data.getExtras().getParcelableArrayList("deletes");
                    imageAdaptor.deleteInvalidImages(images);
                }
                break;
            }
            case (SingleImageActivity.IMAGE_VIEWER_DELETE): {
                if (data.getBooleanExtra(SingleImageActivity.RESULT_TAG, false)) {
                    Image image = data.getExtras().getParcelable(SingleImageActivity.DELETE_IMAGE);
                    imageAdaptor.deleteImageItem(image);
                }
                break;
            }
            default:
                break;
        }
    }
}

