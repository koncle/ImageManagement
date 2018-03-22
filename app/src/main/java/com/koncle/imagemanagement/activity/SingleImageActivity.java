package com.koncle.imagemanagement.activity;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.adapter.SingleImageViewPagerAdapter;
import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.FolderSelectDialogFragment;
import com.koncle.imagemanagement.dialog.InfoDialog;
import com.koncle.imagemanagement.dialog.TagSelectDialog;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.view.TagAdapter;
import com.koncle.imagemanagement.view.TagViewLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.view.Window.FEATURE_CONTENT_TRANSITIONS;

/**
 * Created by 10976 on 2018/1/10.
 */

public class SingleImageActivity extends AppCompatActivity implements SingleImageViewPagerAdapter.ModeChange {
    public static final String DELETE_IMAGE = "image";
    public static final int IMAGE_VIEWER_MOVE = 4;
    public static final String MOVE_IMAGE = "move_image";
    public static final java.lang.String INTENT_DESC_STRING = "desc";
    public static final String RESULT_TAG = "result";
    public static final int IMAGE_VIEWER_DELETE = 2;
    public static final int IMAGE_VIWER_SCROLL = 3;
    private static final String TAG = SingleImageActivity.class.getSimpleName();
    boolean descShow = true;
    boolean tagShow = true;
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
    private List<Image> deleteImages;
    private RadioButton tag;
    private ViewGroup container;
    private boolean show;
    private TextView desc;
    private Parcelable obj;
    private TagViewLayout tagViewLayout;
    private TagAdapter<Tag> tagAdapter;
    private LinearLayout bottomLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(FEATURE_CONTENT_TRANSITIONS);

        setContentView(R.layout.single_view_layout);

        Bundle bundle = getIntent().getExtras();
        int position = 0;
        boolean descOrder = false;
        if (bundle != null) {
            position = (int) bundle.get("pos");
            boolean fromDatabase = bundle.getBoolean(ActivityUtil.DATA_TYPE);
            descOrder = bundle.getBoolean(ActivityUtil.NOT_REVERSE);
            if (fromDatabase) {
                obj = bundle.getParcelable(ActivityUtil.ACTIVITY_MUL_IMAGE_DATA);
                images = ImageService.getImagesFromParcelable(obj);
            } else {
                obj = null;
                images = bundle.getParcelableArrayList(ActivityUtil.ACTIVITY_MUL_IMAGE_DATA);
                // recover lost dao when serialization
                ImageService.recoverDaoSession(images);
            }
        }
        deleteImages = new ArrayList<>();
        if (!descOrder) {
            Collections.reverse(images);
        }

        findViews();
        initViewPager(position);
        initToolbar();
        initOperatoins();
        initTagLayout(getCurrentItem());
    }

    private void initTagLayout(Image image) {
        tagAdapter = new TagAdapter<Tag>(null) {
            @Override
            public View getView(ViewGroup parent, int position, Tag tag) {
                View root = LayoutInflater.from(getApplication()).inflate(R.layout.test_item, parent, false);
                ((TextView) root.findViewById(R.id.test_id)).setText(tag.getTag());
                return root;
            }
        };
        tagViewLayout.setAdapter(tagAdapter);
        tagViewLayout.setOnTagClickListener(new TagViewLayout.OnTagClickListener() {
            @Override
            public boolean onTagClick(View view, int position) {
                Tag tag = tagAdapter.getItem(position);
                ActivityUtil.showImageList(SingleImageActivity.this, tag, tag.getTag());
                return true;
            }
        });
        changeTags(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_more, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.single_item_bg:
                Image image = getCurrentItem();
                if (image.getType() == Image.TYPE_GIF) {
                    Toast.makeText(this, "GIF can't be set as wallpaper", Toast.LENGTH_SHORT).show();
                } else {
                    Glide.with(this)
                            .load(image.getPath())
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    try {
                                        WallpaperManager.getInstance(SingleImageActivity.this).setBitmap(bitmap);
                                        Toast.makeText(SingleImageActivity.this, "set wallpaper successfully", Toast.LENGTH_SHORT).show();
                                    } catch (IOException e) {
                                        Toast.makeText(SingleImageActivity.this, "can't set wallpaper", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                break;
            case R.id.single_item_info:
                InfoDialog dialog = InfoDialog.newInstance(getCurrentItem());
                dialog.show(getSupportFragmentManager(), "info");
                break;
        }
        return true;
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
        tagViewLayout = findViewById(R.id.tag_view_layout);
        bottomLayout = findViewById(R.id.single_view_bottom_layout);
    }

    private void initViewPager(int position) {
        pagerAdapter = new SingleImageViewPagerAdapter(this, images, position);
        // interface for adapter
        pagerAdapter.setOperator(this);
        imageViewPager.setAdapter(pagerAdapter);
        imageViewPager.setCurrentItem(position);
        imageViewPager.setOffscreenPageLimit(2);

        imageViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Image image = images.get(position);
                changeTitle(image.getName());
                changeDesc(image.getDesc());
                changeTags(image);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        changeDesc(getCurrentItem().getDesc());
        changeTitle(getCurrentItem().getName());
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
                    public void selectFinished(Folder folder) {
                        if (folder.getId().equals(getCurrentItem().getFolder_id())) {
                            return;
                        }
                        Image image = images.get(imageViewPager.getCurrentItem());
                        moveImage(image, folder);
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
                dialog.setOnTagSelectFinished(new TagSelectDialog.OnTagSelectFinished() {
                    @Override
                    public void onTagSelectFinished(List<Tag> tags) {
                        //tagAdapter.setTags(tags);
                        changeTags(tags);
                    }
                });
                dialog.show(getSupportFragmentManager(), "Single");
            }
        });
    }

    private Image getCurrentItem() {
        return images.get(imageViewPager.getCurrentItem());
    }

    private void moveImage(Image image, Folder folder) {
        Image preImage = new Image();
        preImage.setPath(image.getPath());
        preImage.setFolder(image.getFolder());

        boolean success = ImageService.moveFileAndSendMsg(getApplicationContext(), image, folder);

        if (success) {
            Intent intent = new Intent();
            intent.putExtra(RESULT_TAG, true);
            Bundle bundle = new Bundle();
            bundle.putParcelable(DELETE_IMAGE, preImage);
            bundle.putParcelable(MOVE_IMAGE, image);
            intent.putExtras(bundle);
            setResult(IMAGE_VIEWER_MOVE, intent);

            Toast.makeText(getApplicationContext(), "move to folder : " + folder.getPath(), Toast.LENGTH_SHORT).show();
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
    }

    public void changeTitle(String s) {
        this.toolbar.setTitle(s);
    }

    public void changeDesc(String s) {
        desc.setText(s);
        if (toolMode) {
            if (s != null && !"".equals(s)) {
                desc.setVisibility(View.VISIBLE);
            } else {
                desc.setVisibility(View.GONE);
            }
        } else {
            desc.setVisibility(View.GONE);
        }

        descShow = s != null && !"".equals(s);
    }

    private void changeTags(List<Tag> tags) {
        tagAdapter.setTags(tags);

        if (toolMode) {
            if (tags.size() > 0) {
                tagViewLayout.setVisibility(View.VISIBLE);
            } else {
                tagViewLayout.setVisibility(View.GONE);
            }
        } else {
            tagViewLayout.setVisibility(View.GONE);
        }

        tagShow = tags.size() > 0;
    }

    private void changeTags(Image image) {
        image.resetTags();
        List<Tag> tags = image.getTags();
        changeTags(tags);
    }

    public void showTools() {
        Animation down = AnimationUtils.loadAnimation(this, R.anim.toolbar_enter);
        Animation up = AnimationUtils.loadAnimation(this, R.anim.operations_enter);
        //Animator toWhite = AnimatorInflater.loadAnimator(this, R.animator.bg_to_white);

        toolbar.setVisibility(View.VISIBLE);
        toolbar.startAnimation(down);

        if (tagShow) {
            tagViewLayout.setVisibility(View.VISIBLE);
        }

        if (descShow) {
            desc.setVisibility(View.VISIBLE);
        }

        bottomLayout.startAnimation(up);
        bottomLayout.setVisibility(View.VISIBLE);
        Log.w(TAG, "show tools");
    }

    public void hideTools() {
        Animation up = AnimationUtils.loadAnimation(this, R.anim.toolbar_exit);
        Animation down = AnimationUtils.loadAnimation(this, R.anim.operations_exit);
        //Animator toBlack = AnimatorInflater.loadAnimator(this, R.animator.bg_to_black);
        toolbar.startAnimation(up);
        toolbar.setVisibility(View.GONE);

        bottomLayout.startAnimation(down);
        bottomLayout.setVisibility(View.GONE);
        Log.w(TAG, "hide tools");
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
        if (toolMode)
            hideTools();

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
