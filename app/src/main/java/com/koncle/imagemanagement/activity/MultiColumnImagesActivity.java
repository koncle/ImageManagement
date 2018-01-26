package com.koncle.imagemanagement.activity;

/**
 * Created by 10976 on 2018/1/8.
 */

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.ImageAdaptor;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.FolderSelectDialogFragment;
import com.koncle.imagemanagement.dialog.TagSelectDialog;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;
import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_ADDED;
import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_DELETED;

public class MultiColumnImagesActivity extends AppCompatActivity implements ImageAdaptor.ModeOperator {

    public static final String className = MultiColumnImagesActivity.class.getSimpleName();
    public static final String ALL_FOLDER_NAME = "ALL";
    private static final String TAG = MultiColumnImagesActivity.class.getSimpleName();

    boolean selecting = false;
    private RecyclerView recyclerView;
    private List<Image> images;
    private ImageAdaptor imageAdaptor;
    private Toolbar toolbar;
    private LinearLayout operatoins;
    private RadioButton share;
    private RadioButton delete;
    private RadioButton move;
    private RadioButton tag;
    private String Current_Folder_Name;

    private boolean deleteImage = false;
    public static final int RESULT_DELETE_IMAGE = -4;
    public static final String RESULT_DELETE_IMAGE_FOLDER = "folder";
    public static final String RESULT_DELETE_IMAGE_NUM = "num";

    private MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.multiple_image_outer_layout);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            images = bundle.getParcelableArrayList(ActivityUtil.ACTIVITY_MUL_IMAGE_TAG);
            //images = WeakReference.getMulImages();
            //WeakReference.removeMulImages();
            Current_Folder_Name = bundle.getString(ActivityUtil.ACTIVITY_MUL_IMAGE_TITLE_TAG);
            if (images == null) {
                images = new ArrayList<>();
            }
        } else {
            images = new ArrayList<>();
            Current_Folder_Name = "Error";
        }

        ImageService.recoverDaoSession(images);

        initHandler();
        findViews();
        initMode();
        initRecyclerView();

        getWindow().setExitTransition(new Explode());
        getWindow().setEnterTransition(new Explode());
    }

    public class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            Image image;
            switch (msg.what) {
                // notify folder to change singleImages
                case IMAGE_ADDED:
                    image = msg.getData().getParcelable("image");
                    if (image != null && (ALL_FOLDER_NAME.equals(Current_Folder_Name) || Current_Folder_Name.equals(image.getFolder()))) {
                        imageAdaptor.addNewImage(image);
                        recyclerView.scrollToPosition(0);
                    }
                    break;
                case IMAGE_DELETED:
                    break;
            }
        }
    }

    private void initHandler() {
        handler = new MyHandler();
        MsgCenter.addHandler(handler, className);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w(TAG, "on resume" + images.size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w(TAG, "on pause" + images.size());
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w(TAG, "on stop" + images.size());
    }

    @Override
    protected void onStart() {
        Log.w(TAG, "on start" + images.size());
        super.onStart();
    }

    @Override
    protected void onRestart() {
        Log.w(TAG, "on restart" + images.size());
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "on destroy" + images.size());
        super.onDestroy();
        MsgCenter.removeHandler(className);
        handler.removeCallbacksAndMessages(null);
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
        //hidedToolbar = findViewById(R.id.hide_toolbar);
        //back = findViewById(R.id.select_back);

        recyclerView = findViewById(R.id.recyclerView);

        operatoins = findViewById(R.id.operations);
        share = findViewById(R.id.share);
        delete = findViewById(R.id.event_delete);
        move = findViewById(R.id.move);
        tag = findViewById(R.id.tag);

        //bottomSheetBehavior = BottomSheetBehavior.from(operatoins);
    }

    private void initMode() {
        // init toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selecting) {
                    exitSelectMode();
                } else {
                    onBackPressed();
                }
            }
        });

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setTitle(Current_Folder_Name);
        }

        // init bottom tools
        //bottomSheetBehavior.setHideable(true);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        operatoins.setVisibility(View.GONE);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = imageAdaptor.deleteSelectedImages();
                Toast.makeText(getApplicationContext(), "delete " + count + " files", Toast.LENGTH_SHORT).show();
                deleteImage = true;
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
                final List<Image> selections = imageAdaptor.getSelections();
                if (selections.size() == 0) {
                    imageAdaptor.exitSelectMode();
                    return;
                }

                FolderSelectDialogFragment dialogFragment = FolderSelectDialogFragment.newInstance(new FolderSelectDialogFragment.OnSelectFinishedListener() {
                    @Override
                    public void selectFinished(String folderPath) {
                        // if the folder is ALL folder or the its name is the same as current folder
                        // then return
                        String folderName = ImageUtils.getFolderNameFromFolderPath(folderPath);
                        if (!MultiColumnImagesActivity.ALL_FOLDER_NAME.equals(folderName) && folderName.equals(Current_Folder_Name)) {
                            Toast.makeText(getApplicationContext(), "can't move to the same folder", Toast.LENGTH_SHORT).show();
                            imageAdaptor.exitSelectMode();
                            return;
                        }
                        int count = imageAdaptor.moveSelectedImages(folderPath);
                        imageAdaptor.exitSelectMode();
                        Toast.makeText(getApplicationContext(), "move " + count + " images to folder : " + folderPath, Toast.LENGTH_SHORT).show();
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "multi folder");
            }
        });

        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = imageAdaptor.getSelections();
                //ImageService.addTags(singleImages, );
                if (images.size() > 1) {
                    TagSelectDialog dialog = TagSelectDialog.newInstance(images);
                    dialog.addNote("It will overwrite previous tags");
                    dialog.show(getSupportFragmentManager(), "Multi");
                }
                imageAdaptor.exitSelectMode();
            }
        });
    }

    private void initRecyclerView() {
        int spanCount;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 4;
        } else {
            spanCount = 6;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdaptor = new ImageAdaptor(this, handler, gridLayoutManager, images);
        imageAdaptor.setOperater(this);
        recyclerView.setAdapter(imageAdaptor);
        recyclerView.setItemViewCacheSize(0);

        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getItemAnimator().setChangeDuration(0);
    }


    public void exitSelectMode() {
        // hide tools
        operatoins.setVisibility(View.GONE);
        // clear selections
        imageAdaptor.exitSelectMode();

        // hide menu
        selecting = false;
        invalidateOptionsMenu();
        toolbar.setTitle(Current_Folder_Name);
    }

    public void enterSelectMode() {
        selecting = true;

        // show tools
        operatoins.setVisibility(View.VISIBLE);

        // show menu
        invalidateOptionsMenu();
    }

    @Override
    public void onBackPressed() {
        // if there is no selected mode, then close the activity
        if (!imageAdaptor.exitSelectMode()) {
            if (deleteImage) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_DELETE_IMAGE_FOLDER, Current_Folder_Name);
                intent.putExtra(RESULT_DELETE_IMAGE_NUM, deleteImage);
                setResult(RESULT_DELETE_IMAGE, intent);

                handler.sendEmptyMessage(IMAGE_DELETED);
            }
            WeakReference.clear();
            super.onBackPressed();
        }
    }

    @Override
    public void refreshData() {
        /*
         * The recyclerView will retain at least 4 singleImages to
         * have a perfect perfomance which leads to my failure
         * to recover from select mode to nomal mode,
         * Thus I have to use this method to refresh data
         * so that the view can be redrew.
         */
        imageAdaptor.notifyDataSetChangedWithoutFlash();
    }

    @Override
    public void showSelectedNum(int num) {
        this.toolbar.setTitle(num + " Pictures Selected");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w(TAG, "on return");
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            // the glide will use cache to show image
            // so that only when user click the image and enter the singleImage mode,
            // can the system know whether the image is valid.
            case (SingleImageActivity.IMAGE_VIWER_SCROLL): {
                Log.w("Share", "return");
                // get scrol pos
                int pos = data.getIntExtra("pos", 0);
                recyclerView.scrollToPosition(pos); // //scroll to next 2 rows

                // delete invalid image item
                if (data.getBooleanExtra("delete", false)) {
                    List<Image> images = data.getExtras().getParcelableArrayList("deletes");
                    for (Image image : images) {
                        imageAdaptor.removeItem(image);
                    }
                    deleteImage = true;
                }
                break;
            }
            // user click the delete button in single view
            case (SingleImageActivity.IMAGE_VIEWER_DELETE): {
                if (data.getBooleanExtra(SingleImageActivity.RESULT_TAG, false)) {
                    Image image = data.getExtras().getParcelable(SingleImageActivity.DELETE_IMAGE);
                    imageAdaptor.removeItem(image);
                    deleteImage = true;
                }
                break;
            }
            case (SingleImageActivity.IMAGE_VIEWER_MOVE): {
                if (data.getBooleanExtra(SingleImageActivity.RESULT_TAG, false)) {
                    Image preImage = data.getExtras().getParcelable(SingleImageActivity.DELETE_IMAGE);
                    Image rearImage = data.getExtras().getParcelable(SingleImageActivity.MOVE_IMAGE);

                    imageAdaptor.removeItem(preImage);
                    deleteImage = true;
                }
                break;
            }
            default:
                break;
        }
    }
}

