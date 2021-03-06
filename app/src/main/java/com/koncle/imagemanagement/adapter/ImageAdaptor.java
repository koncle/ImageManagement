package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageAdaptor extends RecyclerView.Adapter<ImageAdaptor.ImageViewHolder>{
    private static final String TAG = ImageAdaptor.class.getSimpleName();
    // In order to ensure the height of the image is the same as its width,
    // the manager has to be introduced to get the correct height
    private final GridLayoutManager gridLayoutManager;
    private int itemHeight = -1;

    private Context context;

    private final List<Image> images;

    // save <positoin, url> into map
    private HashMap<Integer, Image> selectedImages;

    private boolean selectMode = false;
    private ModeOperator modeOperator;

    public ImageAdaptor(Context context, GridLayoutManager gridLayoutManager, List<Image> images) {
        this.context = context;
        this.gridLayoutManager = gridLayoutManager;
        this.images = images;
        selectedImages = new HashMap<>();
        Log.w(this.getClass().getSimpleName(), "DATA : " + images.get(0).toString());
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.image_display, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        final String path = images.get(position).getPath();
        /*
        * Set the correct Height
        *
        * if itemHeight has been calculated, then assign it to the image,
        * else calculate it.
        */
        if (itemHeight == -1) {
            itemHeight = (gridLayoutManager.getWidth() - 4) / gridLayoutManager.getSpanCount();
        }
        ViewGroup.LayoutParams params = holder.image.getLayoutParams();
        params.height = itemHeight;

        // show the select mode or not
        if (selectMode) {
            holder.selects.setVisibility(View.VISIBLE);

            if (selectedImages.containsKey(position)) {
                holder.selects.setChecked(true);
                holder.frameLayout.setVisibility(View.VISIBLE);
            } else {
                holder.selects.setChecked(false);
                holder.frameLayout.setVisibility(View.GONE);
            }
        }else{
            holder.frameLayout.setVisibility(View.GONE);
            holder.selects.setVisibility(View.GONE);
            holder.selects.setChecked(false);
        }

        // set listeners
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                enterSelectMode();
                toggleImageSelectionAndCheck(holder, position);
                return true;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (selectMode) {
                    toggleImageSelectionAndCheck(holder, position);
                } else {
                    ActivityUtil.showSingleImageWithPos(ImageAdaptor.this.context, ImageAdaptor.this.images, position);
                }
            }
        });

        holder.selects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageSelectionWithoutCheck(holder, position);
            }
        });

        // put images
        Glide.with(context)
                .load(path)
                //  .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.image);
    }

    private void enterSelectMode() {
        Log.i("items", "Child count : " + gridLayoutManager.getChildCount());
        if (!selectMode) {
            setOrClearDisplayedImageSelection(false);
            modeOperator.enterSelectMode();
        }
        selectMode = true;
    }

    // this method will be called by MultiColumnImagesActivity activity
    public boolean exitSelectMode() {
        if (selectMode) {
            selectMode = false;
            selectedImages.clear();
            Log.i("items", "Child count : " + gridLayoutManager.getChildCount());
            modeOperator.exitSelectMode();
            return true;
        } else {
            return false;
        }
    }

    /*
    * To prevent the situation when an image has been selected by the checkbox
    * and its click listener will be triggered, thus if set the checkbox again,
    * the checkbox will set back to its original value, which almost means
    * nothing has been done.
    */
    private void toggleImageSelectionWithoutCheck(ImageViewHolder holder, int position) {
        if (selectedImages.containsKey(position)) {
            selectedImages.put(position, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);
        } else {
            selectedImages.remove(position);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);
        }

        modeOperator.showSelectedNum(selectedImages.size());
    }

    private void toggleImageSelectionAndCheck(ImageViewHolder holder, int position) {
        if (selectedImages.containsKey(position)) {
            selectedImages.remove(position);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);

            holder.selects.setChecked(false);
        } else {
            selectedImages.put(position, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);

            holder.selects.setChecked(true);
        }

        modeOperator.showSelectedNum(selectedImages.size());
    }

    private void setOrClearDisplayedImageSelection(boolean mode) {
        int start = gridLayoutManager.findFirstVisibleItemPosition();
        int end = gridLayoutManager.findLastVisibleItemPosition();
        View cur;
        int i = start;

        for (; i <= end; ++i) {
            cur = gridLayoutManager.findViewByPosition(i);
            if (cur == null) break;
            cur.findViewById(R.id.select_button).setVisibility(View.VISIBLE);
        }

        /*
        // clear
        if (mode) {
            for (; i <= end; ++i) {
                cur = gridLayoutManager.findViewByPosition(i);
                if (cur == null) break;
                cur.findViewById(R.id.background).setVisibility(View.GONE);
                cur = cur.findViewById(R.id.select_button);
                cur.setVisibility(View.GONE);
                ((CheckBox) cur).setChecked(false);
            }
        } else {
            // show
            for (; i <= end; ++i) {
                cur = gridLayoutManager.findViewByPosition(i);
                if (cur == null) break;
                cur.findViewById(R.id.select_button).setVisibility(View.VISIBLE);
            }
        }
        */

        Log.i("clear", "start + " + start + " end " + end);
    }

    // TODO:to compelte this method
    public void selectAll() {
        if (selectedImages.size() == images.size()) return;

        for (int i = 0; i < images.size(); ++i) {
            selectedImages.put(i, images.get(i));
        }
        modeOperator.refreshData();

        modeOperator.showSelectedNum(selectedImages.size());
    }

    public List<Image> getSelections() {
        List<Image> images = new ArrayList<>();
        for (Integer key : selectedImages.keySet()) {
            images.add(selectedImages.get(key));
        }
        return images;
    }

    public void deleteSelectedImages() {
        int count = 0;
        Image image;
        for (Integer pos : selectedImages.keySet()) {
            image = selectedImages.get(pos);

            // delete from sd card
            count += ImageUtils.deleteFile(image.getPath()) ? 1 : 0;
            // delete from memory
            images.remove(image);
            // delete from database,
            // replaced by service
            // ImageService.deleteImage(image);
        }

        if (count > 0)
            notifyDataSetChanged();
        Log.w(TAG, "delete " + count + " files");

        Toast.makeText(context, "delete " + count + " files", Toast.LENGTH_SHORT).show();
    }

    public boolean deleteImageItem(Image image) {
        int i = 0;
        while (i < images.size()) {
            if (image.getId().equals(images.get(i).getId())) {
                images.remove(images.get(i));
                notifyDataSetChanged();
                return true;
            }
            ++i;
        }
        return false;
    }

    // to operate other components in activity
    public interface ModeOperator {
        void exitSelectMode();

        void enterSelectMode();

        void refreshData();

        void showSelectedNum(int num);
    }

    public void setOperater(ModeOperator modeOperator) {
        this.modeOperator = modeOperator;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public CheckBox selects;
        public FrameLayout frameLayout;
        public ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            selects = itemView.findViewById(R.id.select_button);
            frameLayout = itemView.findViewById(R.id.background);
        }
    }
}
