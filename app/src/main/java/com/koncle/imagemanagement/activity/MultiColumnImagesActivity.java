package com.koncle.imagemanagement.activity;

/**
 * Created by 10976 on 2018/1/8.
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

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
    boolean selecting = false;
    private RecyclerView recyclerView;
    private List<Image> images;
    private ImageAdaptor imageAdaptor;
    private Toolbar toolbar;
    private Toolbar hidedToolbar;
    private LinearLayout operatoins;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_images, menu);
        return true;
    }

    // every time invalidateOptionsMenu() is called,
    // this function is called;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (selecting) {
            menu.findItem(R.id.all).setVisible(true);
        } else {
            menu.findItem(R.id.all).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.order:
                imageAdaptor.toggleImagesOrder();
                break;
            case R.id.all:
                imageAdaptor.selectAll();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        hidedToolbar = findViewById(R.id.hide_toolbar);
        //back = findViewById(R.id.select_back);

        recyclerView = findViewById(R.id.recyclerView);

        operatoins = findViewById(R.id.operations);
        share = findViewById(R.id.share);
        delete = findViewById(R.id.event_delete);
        move = findViewById(R.id.move);
        tag = findViewById(R.id.tag);

        //bottomSheetBehavior = BottomSheetBehavior.from(operatoins);
    }

    private void showToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true); //设置返回键可用
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(images.get(0).getFolder());
        }
    }

    private void initMode() {
        // init toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        hidedToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageAdaptor.exitSelectMode();
            }
        });
        showToolbar(toolbar);

        // init bottom tools
        //bottomSheetBehavior.setHideable(true);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        operatoins.setVisibility(View.GONE);

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

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getItemAnimator().setChangeDuration(0);
    }


    public void exitSelectMode() {
        // show nomal toolbar
        toolbar.setVisibility(View.VISIBLE);
        hidedToolbar.setVisibility(View.GONE);
        showToolbar(toolbar);

        // hide tools
        operatoins.setVisibility(View.GONE);

        // hide menu
        selecting = false;
        invalidateOptionsMenu();
    }

    public void enterSelectMode() {
        // show hidden toolbar
        toolbar.setVisibility(View.GONE);
        hidedToolbar.setVisibility(View.VISIBLE);
        showToolbar(hidedToolbar);

        // show tools
        operatoins.setVisibility(View.VISIBLE);

        // show menu
        selecting = true;
        invalidateOptionsMenu();
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
        this.hidedToolbar.setTitle(num + " Pictures Selected");
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

