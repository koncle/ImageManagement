package com.koncle.imagemanagement.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.DrawerActivity;
import com.koncle.imagemanagement.activity.ImageChangeListener;
import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.TagSelectDialog;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Koncle on 2018/1/12.
 */

public class FolderFragment extends Fragment implements HasName, ImageChangeListener {
    private final String TAG = getClass().getSimpleName();
    private final String name = DrawerActivity.FOLDER_FRAGMENT_NAME;
    private FolderRecyclerViewAdapter folderAdapter;
    private List<Folder> folders;
    //private List<Image> folderCovers;
    private boolean selectMode = false;
    private Map<Integer, Folder> selectedFolder = new HashMap<>();

    final int ORANGE = Color.rgb(255, 223, 0);
    final int WHITE = Color.WHITE;
    private Operator operator;
    private SwipeRefreshLayout refresh;
    private LinearLayout operations;


    public static Fragment newInstance() {
        return new FolderFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        operator = (Operator) context;
        Log.w(TAG, "onAttach");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.w(TAG, "onSaveInstanceState");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.folder_fragment, null);
        RecyclerView recyclerView = view.findViewById(R.id.folder_recycler);

        int spanCount;
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = 2;
        } else {
            spanCount = 4;
        }

        folders = ImageService.getNewAllFolders();
        //folderCovers = ImageService.getAllFolders();

        folderAdapter = new FolderRecyclerViewAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), spanCount));
        recyclerView.setAdapter(folderAdapter);

        // refresh data
        operator.refreshData();

        // prevent flash
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.getItemAnimator().setChangeDuration(0);

        refresh = view.findViewById(R.id.swipe_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                operator.refreshData();
            }
        });

        operations = view.findViewById(R.id.folder_operations);
        operations.setVisibility(View.GONE);
        //bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.folder_operations));
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        RadioButton delete = view.findViewById(R.id.event_delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> pathes = new ArrayList<>();
                for (Integer pos : selectedFolder.keySet()) {
                    pathes.add(selectedFolder.get(pos).getPath());
                    folderAdapter.notifyItemRemoved(pos);
                }
                int count = ImageUtils.deleteFiles(pathes);
                Toast.makeText(getContext(), "delete " + count + " files", Toast.LENGTH_SHORT).show();
            }
        });

        RadioButton share = view.findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> images = new ArrayList<>();
                for (Folder folder : selectedFolder.values()) {
                    //images.addAll(ImageService.getImagesFromFolder(folder.getFolder()));
                    images.addAll(folder.getImages());
                    if (images.size() > 50) {
                        Toast.makeText(getContext(), "the number of image should be less than 100", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (images.size() > 0)
                    ActivityUtil.shareImages(getContext(), images);
            }
        });
        return view;
    }

    public void showPopup(View view, final String folder) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.folder_op, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    /*
                    case R.id.event_delete:
                        if (ImageUtils.deleteFile(folder))
                            Toast.makeText(getContext(), "delete...", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), "can't delete...", Toast.LENGTH_SHORT).show();
                        break;
                        */
                    case R.id.share:
                        List<Image> images = ImageService.getImagesFromFolder(folder);
                        ActivityUtil.shareImages(getContext(), images);
                        break;
                    case R.id.tag:
                        TagSelectDialog dialog = TagSelectDialog.newInstance(ImageService.getImagesFromFolder(folder));
                        dialog.addNote("It will overwrite previous tags");
                        dialog.show(getFragmentManager(), "Folder");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void onTagAdded() {
        Toast.makeText(getContext(), "added tags", Toast.LENGTH_SHORT).show();
    }

    class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderHolder> {
        private static final int ALL = 0;
        private static final int OTHER = 1;

        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FolderHolder(LayoutInflater.from(FolderFragment.this.getContext())
                    .inflate(R.layout.folder_image_layout, parent, false));
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        public void notifyDataSetChangedWithoutFlash() {
            notifyItemRangeChanged(0, folders.size());
        }

        public void setFolderCover(int index, Folder folder) {
            folders.set(index, folder);
            notifyItemChanged(index);
        }

        public void refreshAll() {
            folders = ImageService.getNewAllFolders();
            notifyDataSetChangedWithoutFlash();
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? ALL : OTHER;
        }

        @Override
        public void onBindViewHolder(final FolderHolder holder, final int position) {
            final String folder = folders.get(position).getName();

            if (getItemViewType(position) == OTHER) {
                holder.num.setText(String.format("(%d)", ImageService.getImageCountByFolder(folder)));
                holder.more.setVisibility(View.VISIBLE);
            } else {
                holder.num.setText(String.format("(%d)", ImageService.getImagesCount()));
                holder.more.setVisibility(View.GONE);
            }

            holder.textView.setText(folder);

            if (selectMode && selectedFolder.containsKey(position)) {
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.colorAccent));
            } else {
                //holder.cardView.setCardBackgroundColor(WHITE);
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_light_blue_100));
            }

            Glide.with(FolderFragment.this.getContext())
                    .load(folders.get(position).getCoverPath())
                    .asBitmap()
                    .thumbnail(0.1f)
                    // .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.imageView);

            initListener(holder, position);
        }

        private void initListener(final FolderHolder holder, final int position) {
            final Folder folder = folders.get(position);
            final String folderName = folder.getName();
            if (getItemViewType(position) == OTHER) {
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectMode) {
                            toggleFolderSelection(holder.cardView, folder, position);
                        } else {
                            ActivityUtil.showImageList(FolderFragment.this.getContext(), folder, folderName);
                        }
                    }
                });

                holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        enterSelectMode(holder.cardView, folder, position);
                        return true;
                    }
                });

                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(holder.more, folderName);
                    }
                });

                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPopup(holder.more, folderName);
                    }
                });
            } else {
                // ALL
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectMode) {
                            Toast.makeText(getContext(), " can't be selected", Toast.LENGTH_SHORT).show();
                        } else {
                            ActivityUtil.showImageList(FolderFragment.this.getContext(), folder, folderName);
                        }
                    }
                });
            }
        }

        private void toggleFolderSelection(CardView card, Folder folder, int pos) {
            if (selectedFolder.containsKey(pos)) {
                unSelectFolder(card, pos);
            } else {
                selectFolder(card, folder, pos);
            }
        }

        private void selectFolder(CardView card, Folder folder, int pos) {
            card.setCardBackgroundColor(ORANGE);
            selectedFolder.put(pos, folder);
        }

        private void unSelectFolder(CardView card, int pos) {
            card.setCardBackgroundColor(WHITE);
            selectedFolder.remove(pos);
        }

        public void selectAll() {
            for (int i = 0; i < folders.size(); ++i)
                selectedFolder.put(i, folders.get(i));
            notifyDataSetChangedWithoutFlash();
        }

        private void unSelectAll() {
            selectedFolder.clear();
            notifyDataSetChangedWithoutFlash();
        }

        class FolderHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            CardView cardView;
            ImageButton more;
            TextView num;
            LinearLayout linearLayout;

            FolderHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.folder_image);
                textView = view.findViewById(R.id.folder_text);
                cardView = view.findViewById(R.id.folder_card);
                more = view.findViewById(R.id.folder_more);
                num = view.findViewById(R.id.folder_number);
                linearLayout = view.findViewById(R.id.folder_item_text_area);
            }
        }
    }

    public void enterSelectMode(CardView cardView, Folder folder, int position) {
        selectMode = true;
        folderAdapter.selectFolder(cardView, folder, position);
        operations.setVisibility(View.VISIBLE);
        //bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public boolean exitSelectMode() {
        if (selectMode) {
            selectMode = false;
            folderAdapter.unSelectAll();
            operations.setVisibility(View.GONE);
            // bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        } else {
            return false;
        }
    }

    public void refreshAllFolderCovers() {
        // change data set
        if (folderAdapter == null) return;
        this.folderAdapter.refreshAll();

        // stop refresh ui
        refresh.setRefreshing(false);
    }

    public void refreshFirst() {
        folderAdapter.setFolderCover(0, ImageService.getAllNameFolder());
    }

    public void refreshCover(Image image) {
        if (image == null) return;
        String folderPath = ImageUtils.getFolderPathFromPath(image.getPath());
        int index = 1;
        for (; index < folders.size(); ++index) {
            if (folders.get(index).getPath().equals(folderPath)) {
                folderAdapter.setFolderCover(index, folders.get(index));
                break;
            }
        }
    }

    @Override
    public void onImageAdded(Image image) {
        refreshFirst();
        refreshCover(image);
    }

    @Override
    public void onImageMoved(Image oldImage, Image newImage) {
        folderAdapter.refreshAll();
        /*
        Image oldFolderCover = ImageService.getCoverFromFolder(oldImage.getFolder());
        Image newFolderCover = ImageService.getCoverFromFolder(newImage.getFolder());
        // get new cover for old folder
        refreshCover(oldFolderCover);
        refreshCover(newFolderCover);
        */
    }

    @Override
    public void onImageDeleted(Image image) {
        if (image == null) return;
        Image newCover = ImageService.getCoverFromFolder(image.getFolder_id());
        refreshFirst();
        refreshCover(newCover);
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
