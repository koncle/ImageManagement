package com.koncle.imagemanagement.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.SingleImageViewPagerAdapter;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.FolderSelectDialogFragment;
import com.koncle.imagemanagement.dialog.TagSelectDialog;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;
import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

/**
 * Created by 10976 on 2018/1/10.
 */

public class SingleImageActivity extends AppCompatActivity implements SingleImageViewPagerAdapter.ModeChange {
    public static final String DELETE_IMAGE = "image";
    public static final int IMAGE_VIEWER_MOVE = 4;
    public static final String MOVE_IMAGE = "move_image";
    public static final java.lang.String INTENT_DESC_STRING = "desc";
    private static final String TAG = SingleImageActivity.class.getSimpleName();
    private ViewPager imageViewPager;
    private List<Image> images;
    private View delete;
    private View share;
    private View mark;
    private View move;
    private Toolbar toolbar;
    private LinearLayout toolLayout;
    private SingleImageViewPagerAdapter pagerAdapter;

    private boolean toolMode = true;

    public static final String RESULT_TAG = "result";
    public static final int IMAGE_VIEWER_DELETE = 2;
    public static final int IMAGE_VIWER_SCROLL = 3;
    private List<Image> deleteImages;
    private RadioButton tag;
    private ViewGroup container;
    private boolean show;
    private TextView desc;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.single_view_layout);

        Bundle bundle = getIntent().getExtras();
        int position = (int) bundle.get("pos");

        this.images = (List<Image>) bundle.get("singleImages");
        //this.images = WeakReference.getSingleImages();

        ImageService.recoverDaoSession(images);  // its daoSession can't not be parsed

        deleteImages = new ArrayList<>();

        findViews();

        initViewPager(position);
        initToolbar();
        initOperatoins();
    }

    private void initToolbar() {
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.setTitle(images.get(imageViewPager.getCurrentItem()).getName());
        //hideTools();
        changeTitle(images.get(0).getName());
    }

    private void hideStatusBarAndActionBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
    }

    private void findViews() {
        toolbar = findViewById(R.id.single_toolbar);
        imageViewPager = findViewById(R.id.image_view_pager);
        delete = findViewById(R.id.event_delete);
        share = findViewById(R.id.share);
        move = findViewById(R.id.move);
        mark = findViewById(R.id.mark);
        tag = findViewById(R.id.single_tag);
        toolLayout = findViewById(R.id.tool_layout);
        container = findViewById(R.id.single_view_transition_container);
        desc = findViewById(R.id.single_image_desc);
    }

    private void initViewPager(int position) {
        pagerAdapter = new SingleImageViewPagerAdapter(this, images, position);
        // interface for adapter
        pagerAdapter.setOperator(this);
        imageViewPager.setAdapter(pagerAdapter);
        imageViewPager.setCurrentItem(position);

        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                changeTitle(images.get(position).getName());
                changeDesc(images.get(position).getDesc());
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        changeDesc(images.get(position).getDesc());
    }

    private void initOperatoins() {
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Image currentImage = images.get(imageViewPager.getCurrentItem());
                deleteImage(currentImage);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.shareImage(SingleImageActivity.this, images.get(imageViewPager.getCurrentItem()));
            }
        });

        move.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderSelectDialogFragment dialogFragment = FolderSelectDialogFragment.newInstance(new FolderSelectDialogFragment.OnSelectFinishedListener() {
                    @Override
                    public void selectFinished(String folderPath) {
                        if (folderPath.equals(getCurrentItem().getFolder())) {
                            return;
                        }
                        Image image = images.get(imageViewPager.getCurrentItem());
                        moveImage(image, folderPath);
                    }
                });
                dialogFragment.show(getSupportFragmentManager(), "folder dialog");
            }
        });

        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RemarkActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(INTENT_DESC_STRING, getCurrentItem().getDesc());
                intent.putExtras(bundle);
                SingleImageActivity.this.startActivityForResult(intent, 1);
            }
        });


        tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagSelectDialog dialog = TagSelectDialog.newInstance(images.get(imageViewPager.getCurrentItem()));
                dialog.show(getSupportFragmentManager(), "Single");
            }
        });
    }

    private Image getCurrentItem() {
        return images.get(imageViewPager.getCurrentItem());
    }

    private void moveImage(Image image, String folderPath) {
        Image preImage = new Image();
        preImage.setPath(image.getPath());
        preImage.setFolder(image.getFolder());

        boolean success = ImageService.moveFileAndSendMsg(getApplicationContext(), image, folderPath);

        if (success) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_TAG, true);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DELETE_IMAGE, preImage);
            bundle.putParcelable(MOVE_IMAGE, image);
            intent.putExtras(bundle);
            setResult(IMAGE_VIEWER_MOVE, intent);

            Toast.makeText(getApplicationContext(), "move to folder : " + folderPath, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    public void deleteImage(Image currentImage) {
        // delete from sd card and send msg
        ImageService.deleteImageInFileSystemAndBroadcast(this, currentImage, true);

        // notify mul
        Intent intent = new Intent();
        intent.putExtra(RESULT_TAG, true);
        Bundle bundle = new Bundle();
        bundle.putParcelable(DELETE_IMAGE, currentImage);
        intent.putExtras(bundle);
        setResult(IMAGE_VIEWER_DELETE, intent);


        List<Image> deletedImages = new ArrayList<>();
        deletedImages.add(currentImage);
        MsgCenter.notifyDataDeletedInner(deletedImages);
        // notify multi activity to change its data set
        finish();
    }

    @Override
    public void addDeleteImage(Image image) {
        if (!deleteImages.contains(image))
            this.deleteImages.add(image);
    }

    public void toggleMode() {
        if (toolMode) {
            hideTools();
        } else {
            showTools();
        }
        toolMode = !toolMode;
        changeDesc(getCurrentItem().getDesc());
    }

    public void changeTitle(String s) {
        this.toolbar.setTitle(s);
    }

    public void changeDesc(String s) {
        if (s != null && !"".equals(s) && toolMode) {
            desc.setVisibility(View.VISIBLE);
            desc.setText(s);
        } else {
            desc.setText("");
            desc.setVisibility(GONE);
        }
    }

    public void showTools() {
        show = true;
        toolLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        toolLayout.setClickable(true);

        Animation down = AnimationUtils.loadAnimation(this, R.anim.toolbar_enter);
        Animation up = AnimationUtils.loadAnimation(this, R.anim.operations_enter);
        //Animator toWhite = AnimatorInflater.loadAnimator(this, R.animator.bg_to_white);
        toolLayout.startAnimation(up);
        toolbar.startAnimation(down);

        //toWhite.setTarget(imageViewPager);
        //toWhite.start();
    }

    public void hideTools() {
        show = false;
        Animation up = AnimationUtils.loadAnimation(this, R.anim.toolbar_exit);
        Animation down = AnimationUtils.loadAnimation(this, R.anim.operations_exit);
        //Animator toBlack = AnimatorInflater.loadAnimator(this, R.animator.bg_to_black);
        toolbar.startAnimation(up);
        toolLayout.startAnimation(down);

        toolLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        toolLayout.setClickable(false);

        //toBlack.setTarget(imageViewPager);
        //toBlack.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("pos", imageViewPager.getCurrentItem());

        /*
        * when user scroll singleImages, there may be many invalid singleImages
        * that some application didn't delete completely as what this app did,
        * thus invalid singleImages have to be deleted both in database and in MediaStore
        *
        * Of cause this work should be done by MultiActivity which should
        * show the change of a data set
        */
        if (deleteImages.size() > 0) {
            for (Image image : deleteImages) {
                ImageService.deleteImageInFileSystemAndBroadcast(getApplication(), image, false);
            }
            Log.w(TAG, "delete " + deleteImages.size() + "invalid files");

            intent.putExtra("delete", true);
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("deletes", (ArrayList<? extends Parcelable>) deleteImages);
            intent.putExtras(bundle);

        } else {
            intent.putExtra("delete", false);
        }
        setResult(IMAGE_VIWER_SCROLL, intent);

        // prevent toolbar show again when the activity finished
        if (show)
            hideTools();
        toolbar.setVisibility(GONE);
        toolLayout.setVisibility(GONE);

        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RemarkActivity.REMARK_OK) {
            String remark = data.getStringExtra(RemarkActivity.REMARK_INPUT).trim();
            ImageService.addImageDesc(images.get(imageViewPager.getCurrentItem()), remark);
            changeDesc(remark);
        }
    }
}
