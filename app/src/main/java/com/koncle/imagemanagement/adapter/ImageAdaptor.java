package com.koncle.imagemanagement.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.ImageViewer;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageAdaptor extends RecyclerView.Adapter<ImageAdaptor.ImageViewHolder>{
    // In order to ensure the height of the image is the same as its width,
    // the manager has to be introduced to get the correct height
    private final GridLayoutManager gridLayoutManager;
    private int itemHeight = -1;

    private Context context;

    private final List<String> data;

    // save <positoin, url> into map
    private HashMap<Integer, String> selectedImages;

    private boolean selectMode = false;
    private ModeOperator modeOperator;

    public ImageAdaptor(Context context, GridLayoutManager gridLayoutManager, List<String> data){
        this.context = context;
        this.gridLayoutManager = gridLayoutManager;
        this.data = data;
        selectedImages = new HashMap<>();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.image_display, parent, false));
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        final String path = data.get(position);
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
                    Intent intent = new Intent(ImageAdaptor.this.context, ImageViewer.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("paths", (ArrayList<String>) ImageAdaptor.this.data);
                    bundle.putInt("pos", position);
                    intent.putExtras(bundle);
                    ((Activity) ImageAdaptor.this.context).startActivityForResult(intent, 1);
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

    // this method will be called by ImageListViewer activity
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
            selectedImages.put(position, data.get(position));
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
            selectedImages.put(position, data.get(position));
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
        if (selectedImages.size() == data.size()) return;

        for (int i = 0; i < data.size(); ++i) {
            selectedImages.put(i, data.get(i));
        }
        modeOperator.refreshData();

        modeOperator.showSelectedNum(selectedImages.size());
    }

    public List<String> getSelections() {
        List<String> paths = new ArrayList<>();
        for (Integer key : selectedImages.keySet()) {
            paths.add(selectedImages.get(key));
        }
        return paths;
    }

    public void deleteSelectedImages() {
        String path;
        for (Integer pos : selectedImages.keySet()) {
            path = selectedImages.get(pos);
            ImageUtils.deleteFile(path);
            data.remove(path);
        }
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
        return data.size();
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
