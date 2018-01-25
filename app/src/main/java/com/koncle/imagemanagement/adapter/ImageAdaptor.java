package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.os.Handler;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.MsgCenter;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageAdaptor extends RecyclerView.Adapter<ImageAdaptor.ImageViewHolder>{
    private static final String TAG = ImageAdaptor.class.getSimpleName();
    // In order to ensure the height of the image is the same as its width,
    // the manager has to be introduced to get the correct height
    private final GridLayoutManager gridLayoutManager;
    private final Handler handler;
    private int itemHeight = -1;

    private Context context;

    private List<Image> images;

    // save <positoin, url> into map
    private HashMap<Integer, Image> selectedImages;

    private boolean selectMode = false;
    private ModeOperator modeOperator;
    private boolean descOrder = true;

    public ImageAdaptor(Context context, Handler handler, GridLayoutManager gridLayoutManager, List<Image> images) {
        this.handler = handler;
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
                enterSelectMode();
                toggleImageSelectionAndCheck(holder, position);
                modeOperator.enterSelectMode();
                modeOperator.showSelectedNum(1);
                return true;
            }
        });

        holder.image.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (selectMode) {
                    toggleImageSelectionAndCheck(holder, position);
                } else {
                    ActivityUtil.showSingleImageWithPos(ImageAdaptor.this.context, ImageAdaptor.this.images, position, holder.image);
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
        Log.w("items", "Child count : " + gridLayoutManager.getChildCount());
        if (!selectMode) {
            // change main UI
            modeOperator.enterSelectMode();

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

    // this method will be called by MultiColumnImagesActivity activity
    public boolean exitSelectMode() {
        if (selectMode) {
            selectMode = false;
            selectedImages.clear();
            Log.w("items", "Child count : " + gridLayoutManager.getChildCount());

            // change main UI
            modeOperator.exitSelectMode();

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
    private void toggleImageSelectionWithoutCheck(ImageViewHolder holder, int position) {
        if (selectedImages.containsKey(position)) {
            selectedImages.remove(position);
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.GONE);
        } else {
            selectedImages.put(position, images.get(position));
            holder.frameLayout.findViewById(R.id.background).setVisibility(View.VISIBLE);
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
        Log.i("clear", "start + " + start + " end " + end);
    }

    // TODO:to compelte this method
    public void selectAll() {
        if (selectedImages.size() == images.size()) return;

        for (int i = 0; i < images.size(); ++i) {
            selectedImages.put(i, images.get(i));
        }

        //modeOperator.refreshData();
        notifyDataSetChangedWithoutFlash();
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
        Set keySet = selectedImages.keySet();
        Collection<Image> valueSet = selectedImages.values();

        int count = 0;

        Image image;
        for (Integer pos : selectedImages.keySet()) {
            image = selectedImages.get(pos);
            count += ImageService.deleteImageInFileSystemAndBroadcast(context, image, true) ? 1 : 0;
            // delete from memory
            images.remove(image);
            // prevent inconsistence in data
            notifyItemRemoved(pos);
            // delete from database,
            // replaced by service
            // ImageService.deleteImageInFileSystemAndBroadcast(image);
        }
        ///notifyItemRemoved(pos);
        notifyDataSetChangedWithoutFlash();

        final List<Image> deletedImages = new ArrayList<>();
        deletedImages.addAll(selectedImages.values());
        MsgCenter.notifyDataDeletedInner(deletedImages);
        Log.w(TAG, "delete " + count + " files");
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                // delete from sd card
                for(Image image1 : deletedImages) {
                    count += ImageService.deleteImageInFileSystemAndBroadcast(context, image1, true) ? 1 : 0;
                    MsgCenter.notifyDataDeletedInner(deletedImages);
                }

                Log.w(TAG, "delete " + count + " files");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // notify others

            }
        }).start();
*/
        Toast.makeText(context, "delete " + count + " files", Toast.LENGTH_SHORT).show();
    }

    // delete one item from single view
    public void deleteImageItem(Image image) {
        int i = images.indexOf(image);
        if (i == -1) return;
        images.remove(i);
        notifyItemRemoved(i);
        notifyItemRangeChanged(i, images.size() - i);
        Log.w(TAG, "delete image items");
    }

    // delete invalid item from view
    public void deleteInvalidImage(Image image) {
        // singleImages.remove(image);
        int i = images.indexOf(image);
        images.remove(i);
        notifyItemChanged(i);
        notifyItemRangeChanged(i, images.size() - i);
        //notifyDataSetChangedWithoutFlash();
        Log.w(TAG, "delete 1 invalid file " + image.getPath());
    }

    public void deleteInvalidImages(List<Image> imageList) {
        for (Image image : imageList) {
            deleteInvalidImage(image);
        }
        Log.w(TAG, "delete " + imageList.size() + "invalid files");
    }

    public void setData(List<Image> images) {
        this.images = images;
        notifyDataSetChangedWithoutFlash();
    }

    // image added from other apps
    public void addNewImage(Image image) {
        if (descOrder) {
            images.add(0, image);
            // prevent wrong position for views
            // because when call notifyItemInserted() method, the Items below this
            // item will be moved, thus position changed and the most important
            // point is the method : onBindView will not be called,
            // so that listener that were added to these items will not be
            // updated.
            // If use notifyItemRangeChanged, the animation will not displayed
            // but the position is correct.
            // There use both them to ensure the animation and position
            notifyItemInserted(0);
        } else {
            images.add(image);
            notifyItemInserted(images.size());
        }
        notifyItemRangeChanged(0, images.size());
    }

    public void addImage(Image image) {
        int size = images.size();
        // desc order
        if (descOrder) {
            // insert into the first place
            int i = 0;
            for (; i < images.size(); ++i)
                if (image.getTime().getTime() >= images.get(i).getTime().getTime())
                    break;
            images.add(i, image);

            notifyItemInserted(0);
            notifyItemRangeChanged(0, images.size());
        } else {
            int i = 0;
            for (; i < images.size(); ++i)
                if (image.getTime().getTime() <= images.get(i).getTime().getTime())
                    break;
            // append the list
            images.add(i, image);
            notifyItemInserted(size);
        }
    }

    public void removeItem(Image image) {
        int index = images.indexOf(image);
        if (index != -1) {
            images.remove(index);
            notifyItemRemoved(index);
            notifyItemRangeChanged(index, images.size() - index);
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
        return images.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder{
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

    public void toggleImagesOrder() {
        descOrder = !descOrder;
        Collections.reverse(images);
        notifyDataSetChangedWithoutFlash();
    }

    public void notifyDataSetChangedWithoutFlash() {
        notifyItemRangeChanged(0, images.size());
    }
}
