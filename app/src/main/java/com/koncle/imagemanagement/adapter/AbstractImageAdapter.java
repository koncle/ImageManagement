package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
 * Created by Koncle on 2018/3/26.
 */

public abstract class AbstractImageAdapter extends RecyclerView.Adapter<AbstractImageAdapter.ImageViewHolder> {
    private static final String TAG = ImageAdaptor.class.getSimpleName();

    // In order to ensure the height of the image is the same as its width,
    // the manager has to be introduced to get the correct height
    private final GridLayoutManager gridLayoutManager;
    private int itemHeight = -1;

    protected final Context context;
    protected List<Image> images;
    protected ModeListener modeListener;

    protected HashMap<Integer, Image> selectedImages;

    private boolean selectMode = false;

    public AbstractImageAdapter(Context context, GridLayoutManager gridLayoutManager) {
        this.context = context;
        this.gridLayoutManager = gridLayoutManager;
        this.selectedImages = new HashMap<>();
    }

    public void setData(List<Image> imageList){
        this.images = imageList;
        selectedImages.clear();
        notifyDataSetChanged();
    }

    public void restoreSelectState(List<Image> values){
        for (Image image : values){
            int index = images.indexOf(image);
            if (-1 != index){
                selectedImages.put(index, image);
            }
        }
        notifyDataSetChangedWithoutFlash();
    }

    protected int getImageHeight(){
        // calculate the height of image
        if (itemHeight == -1) {
            itemHeight = (gridLayoutManager.getWidth() - 4) / gridLayoutManager.getSpanCount();
        }
        return itemHeight;
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        /*
        * Set the correct Height
        *
        * if itemHeight has been calculated, then assign it to the image,
        * else calculate it.
        */
        ViewGroup.LayoutParams params = holder.image.getLayoutParams();
        params.height = getImageHeight();

        // show the select mode or not
        if (selectMode) {
            holder.selects.setVisibility(View.VISIBLE);

            if (selectedImages.containsValue(images.get(position))) {
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
                // whether do default actions
                boolean notDoActions;
                if (selectMode){
                    notDoActions = imageLongClickInSelectMode(position, holder);
                }else{
                    notDoActions = imageLongClickNotInSelectMode(position, holder);
                    if (!notDoActions){
                        enterSelectMode();
                    }
                }
                if (!notDoActions){
                    toggleImageSelectionAndCheck(holder, position);
                    if (modeListener != null){
                        modeListener.showSelectedNum(selectedImages.size());
                    }
                }
                return true;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // whether do default actions
                boolean notDoActions;
                if (selectMode) {
                    // subclass would implement this method to do what it wants
                    notDoActions = imageClickInSelectMode(position, holder);
                    if (!notDoActions){
                        toggleImageSelectionAndCheck(holder, position);
                    }
                } else {
                    // subclass would implement this method to do what it wants
                    notDoActions = imageClickNotInSelectMode(position, holder);
                }
            }
        });

        holder.selects.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleImageSelectionWithoutCheck(holder, position);
            }
        });

        // make sure select mode works properly
        // then if the image has not changed,
        // following works are useless

        final String path = images.get(position).getPath();
        if (selectMode && path.equals(holder.frameLayout.getTag())) {
            return;
        }

        if (images.get(position).getType() == Image.TYPE_GIF) {
            // put singleImages
            Glide.with(context)
                    .load(path)
                    .asBitmap()
                    //  .diskCacheStrategy(DiskCacheStrategy.NONE)
                    // can't add simple target, cause it costs too much memory
                    .into(holder.image);
            holder.gifText.setVisibility(View.VISIBLE);
        } else {
            // put singleImages
            Glide.with(context)
                    .load(path)
                    //  .diskCacheStrategy(DiskCacheStrategy.NONE)
                    // can't add simple target, cause it costs too much memory
                    .into(holder.image);
            holder.gifText.setVisibility(View.GONE);
        }
        // add tag to decide whether the image needs being loaded again
        // can't add tag to image when Glide is load its picture
        // cause Glide is using the tag to make sure singleImages won't
        // loaded wrongly.
        holder.frameLayout.setTag(path);
    }

    public void enterSelectMode() {
        if (!selectMode) {
            // change main UI
            if (modeListener != null) {
                modeListener.enterSelectMode();
            }

            /*
             * The recyclerView will retain at least 4 singleImages to
             * have a perfect perfomance which leads to my failure
             * to recover from select mode to nomal mode,
             * Thus I have to use this method to refresh data
             * so that the view can be redrew.
             */
            notifyDataSetChangedWithoutFlash();
        }
        selectMode = true;
    }

    // this method will be called by other components?
    public boolean exitSelectMode() {
        if (selectMode) {
            selectMode = false;
            selectedImages.clear();

            // change main UI
            if (modeListener != null) {
                modeListener.exitSelectMode();
            }
            /*
             * The recyclerView will retain at least 4 singleImages to
             * have a perfect perfomance which leads to my failure
             * to recover from select mode to nomal mode,
             * Thus I have to use this method to refresh data
             * so that the view can be redrew.
             */
            notifyDataSetChangedWithoutFlash();

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
    private void toggleImageSelectionWithoutCheck(ImageViewHolder  holder, int position) {
        if (selectedImages.containsKey(position)) {
            selectedImages.remove(position);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);
        } else {
            selectedImages.put(position, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);
        }

        if (modeListener != null) {
            modeListener.showSelectedNum(selectedImages.size());
        }
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

        if (modeListener != null) {
            modeListener.showSelectedNum(selectedImages.size());
        }
    }

    public void selectImage(int pos){
        if (images == null) return;
        if (pos >= 0 && pos < images.size()){
            selectedImages.put(pos, images.get(pos));
        }
    }
    public void finishSelectImage(){
        notifyDataSetChanged();
    }

    protected void selectSomeImages(int num){
        if (selectedImages == null || images == null) return;
        if (selectedImages.size() == images.size()) return;

        for (int i = 0; i < num; ++i) {
            selectedImages.put(i, images.get(i));
        }
        //modeListener.refreshData();
        notifyDataSetChangedWithoutFlash();
        if (modeListener != null) {
            modeListener.showSelectedNum(selectedImages.size());
        }
    }

    public void selectAll() {
        if (images == null) return;
        selectSomeImages(images.size());
    }

    public void notifyDataSetChangedWithoutFlash() {
        if (images != null) {
            notifyItemRangeChanged(0, images.size());
        }
    }

    public List<Image> getSelections() {
        List<Image> images = new ArrayList<>();
        images.addAll(selectedImages.values());
        return images;
    }

    protected abstract boolean imageClickInSelectMode(int pos, ImageViewHolder holder);
    protected abstract boolean imageClickNotInSelectMode(int pos, ImageViewHolder holder);
    protected abstract boolean imageLongClickInSelectMode(int pos, ImageViewHolder holder);
    protected abstract boolean imageLongClickNotInSelectMode(int pos, ImageViewHolder holder);

    public Image getItem(int pos){return images.get(pos);}

    @Override
    public int getItemCount() {
        return images == null?0:images.size();
    }

    @Override
    public AbstractImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AbstractImageAdapter.ImageViewHolder(
                LayoutInflater.from(context)
                        .inflate(R.layout.multiple_image_inner_layout, parent, false));
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
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

    public void setListener(ModeListener modeListener) {
        this.modeListener = modeListener;
    }

    // to operate other components in activity
    public interface ModeListener {
        void exitSelectMode();

        void enterSelectMode();

        void refreshData();

        void showSelectedNum(int num);

        Parcelable getObj();
    }
}
