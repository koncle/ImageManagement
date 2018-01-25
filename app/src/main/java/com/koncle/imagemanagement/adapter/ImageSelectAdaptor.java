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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageSelectAdaptor extends RecyclerView.Adapter<ImageSelectAdaptor.ImageViewHolder> {
    private static final String TAG = ImageSelectAdaptor.class.getSimpleName();
    // In order to ensure the height of the image is the same as its width,
    // the manager has to be introduced to get the correct height
    private final GridLayoutManager gridLayoutManager;
    private int itemHeight = -1;

    private Context context;

    private List<Image> images;

    // save <positoin, url> into map
    private HashMap<String, Image> selectedImages;

    public ImageSelectAdaptor(Context context, GridLayoutManager gridLayoutManager, List<Image> images) {
        this.context = context;
        this.gridLayoutManager = gridLayoutManager;
        if (images == null) {
            this.images = new ArrayList<>();
        } else {
            this.images = images;
        }
        selectedImages = new HashMap<>();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.multiple_image_inner_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        Image image = images.get(position);
        final String path = image.getPath();
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
        holder.selects.setVisibility(View.VISIBLE);

        if (selectedImages.containsValue(image)) {
            holder.selects.setChecked(true);
            holder.frameLayout.setVisibility(View.VISIBLE);
        } else {
            holder.selects.setChecked(false);
            holder.frameLayout.setVisibility(View.GONE);
        }

        // set listeners
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                toggleImageSelectionAndCheck(holder, position);
                return true;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleImageSelectionAndCheck(holder, position);
            }
        });

        holder.selects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageSelectionWithoutCheck(holder, position);
            }
        });

        if (image.getType() == Image.TYPE_GIF) {
            // put singleImages
            Glide.with(context)
                    .load(path)
                    .asBitmap()
                    // can't add simple target, cause it costs too much memory
                    .into(holder.image);
            holder.gifText.setVisibility(View.VISIBLE);
        } else {
            // put singleImages
            Glide.with(context)
                    .load(path)
                    // can't add simple target, cause it costs too much memory
                    .into(holder.image);
            holder.gifText.setVisibility(View.GONE);
        }
    }

    /*
    * To prevent the situation when an image has been selected by the checkbox
    * and its click listener will be triggered, thus if set the checkbox again,
    * the checkbox will set back to its original value, which almost means
    * nothing has been done.
    */
    private void toggleImageSelectionWithoutCheck(ImageViewHolder holder, int position) {
        String path = images.get(position).getPath();
        if (selectedImages.containsKey(path)) {
            selectedImages.remove(path);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);
        } else {
            selectedImages.put(path, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);
        }
    }

    private void toggleImageSelectionAndCheck(ImageViewHolder holder, int position) {
        String path = images.get(position).getPath();
        if (selectedImages.containsKey(path)) {
            selectedImages.remove(path);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);

            holder.selects.setChecked(false);
        } else {
            selectedImages.put(path, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);

            holder.selects.setChecked(true);
        }
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
        Log.i("clear", "start + " + start + " end " + end);
    }

    // TODO:to compelte this method
    public void selectAll() {
        //if (selectedImages.size() == singleImages.size()) return;

        for (int i = 0; i < images.size(); ++i) {
            selectedImages.put(images.get(i).getPath(), images.get(i));
        }

        notifyDataSetChanged();
    }

    public List<Image> getSelections() {
        List<Image> images = new ArrayList<>();
        images.addAll(selectedImages.values());
        return images;
    }

    public void setData(List<Image> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public CheckBox selects;
        public FrameLayout frameLayout;
        TextView gifText;

        public ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            selects = itemView.findViewById(R.id.select_button);
            frameLayout = itemView.findViewById(R.id.background);
            gifText = itemView.findViewById(R.id.gif_text);
        }
    }
}
