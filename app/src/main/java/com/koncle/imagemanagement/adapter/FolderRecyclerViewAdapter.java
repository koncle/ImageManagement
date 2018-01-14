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

import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderHolder> {
    private List<Image> folders;
    private Context context;

    public FolderRecyclerViewAdapter(Context context, List<Image> imageMap) throws Exception {
        this.context = context;
        if (imageMap == null)
            throw new Exception("folders for folders should not be null");
        this.folders = imageMap;
    }

    @Override
    public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderHolder(LayoutInflater.from(context)
                .inflate(R.layout.folder_layout, parent, false));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @Override
    public void onBindViewHolder(FolderHolder holder, final int position) {
        final String folder = folders.get(position).getFolder();
        holder.textView.setText(folder);
        Glide.with(context)
                .load(folders.get(position).getPath())
                // .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = ImageService.getImagesFromSameFolders(folder);
                ActivityUtil.showImageList(FolderRecyclerViewAdapter.this.context, images);
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
