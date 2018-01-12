package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderHolder> {
    private final List<Image> images;
    private Context context;

    public FolderRecyclerViewAdapter(Context context, List<Image> images) throws Exception {
        this.context = context;
        if (images == null)
            throw new Exception("images for folders should not be null");
        this.images = images;
    }

    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderHolder(LayoutInflater.from(context)
                .inflate(R.layout.folder_layout, parent, false));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onBindViewHolder(FolderHolder holder, int position) {
        final Image image = images.get(position);
        holder.textView.setText(image.getFolder());
        Glide.with(context)
                .load(image.getPath())
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = ImageService.getImagesFromSameFolders(image.getFolder());
                List<String> paths = new ArrayList<>();
                for (Image image1 : images) {
                    paths.add(image1.getPath());
                }
                ActivityUtil.showImageList(FolderRecyclerViewAdapter.this.context, paths);
            }
        });
    }

    class FolderHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textView;

        public FolderHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.folder_image);
            textView = view.findViewById(R.id.folder_text);
        }
    }
}
