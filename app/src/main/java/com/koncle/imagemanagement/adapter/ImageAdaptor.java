package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;

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


    private void toggleImage(View view, int position){
        if (selectedImages.containsKey(position)) {
            selectedImages.remove(position);
            ((TextView)view).setText("N");
        }
        else {
            selectedImages.put(position, data.get(position));
            ((TextView)view).setText("S");
        }
    }

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

        // recover selected images
        if (selectMode == true) {
            holder.frameLayout.setVisibility(View.VISIBLE);
            holder.selects.setVisibility(View.VISIBLE);

            if(selectedImages.containsKey(position))
                holder.selects.setText("S");
            else
                holder.selects.setText("N");
        }else{
            holder.frameLayout.setVisibility(View.GONE);
            holder.selects.setVisibility(View.GONE);
        }

        // set listeners
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(selectMode == false) {
                    int start = gridLayoutManager.findFirstVisibleItemPosition();
                    View cur;
                    int i = 0;
                    while (true){
                        cur = gridLayoutManager.findViewByPosition(start + i++);
                        if (cur == null) break;
                        cur.findViewById(R.id.background).setVisibility(View.VISIBLE);
                        cur = cur.findViewById(R.id.select);
                        cur.setVisibility(View.VISIBLE);
                        ((TextView)cur).setText("N");
                    }
                }
                selectMode = true;
                toggleImage(holder.selects, position);
                return true;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (selectMode == true)
                    toggleImage(holder.selects, position);
            }
        });

        // put images
        Glide.with(context)
                .load(path)
                .into(holder.image);
    }

    public boolean closeSelectMode() {
        if (selectMode == true) {
            selectMode = false;
            int start = gridLayoutManager.findFirstVisibleItemPosition();
            View cur;
            int i = 0;
            while (true){
                cur = gridLayoutManager.findViewByPosition(start + i++);
                if (cur == null) break;
                cur.findViewById(R.id.select).setVisibility(View.GONE);
                cur.findViewById(R.id.background).setVisibility(View.GONE);
            }
            selectedImages.clear();
            return true;
        }else{
            return false;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{
        public ImageView image;
        public TextView selects;
        public FrameLayout frameLayout;
        public ImageViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            selects = itemView.findViewById(R.id.select);
            frameLayout = itemView.findViewById(R.id.background);
        }
    }
}
