package com.koncle.imagemanagement.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 10976 on 2018/1/12.
 */

public class FolderFragment extends Fragment implements HasName {
    private String name;
    private FolderRecyclerViewAdapter folderAdapter;
    private List<Image> folderCovers;
    private boolean selectMode = false;
    private Map<Integer, Image> selectedFolder = new HashMap<>();

    final int ORANGE = Color.rgb(255, 223, 0);
    final int WHITE = Color.WHITE;
    private Operater operater;
    private SwipeRefreshLayout refresh;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private LinearLayout operations;


    public static Fragment newInstance(String name, Operater operater) {
        FolderFragment f = new FolderFragment();
        f.setName(name);
        f.setOperater(operater);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.folder_fragment, null);
        RecyclerView recyclerView = view.findViewById(R.id.folder_recycler);

        folderCovers = ImageService.getFolders();
        folderAdapter = new FolderRecyclerViewAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        recyclerView.setAdapter(folderAdapter);

        refresh = view.findViewById(R.id.swipe_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                operater.refreshFolders();
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
                for (Image folder : selectedFolder.values()) {
                    pathes.add(folder.getPath());
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
                for (Image folder : selectedFolder.values()) {
                    images.addAll(ImageService.getImagesFromSameFolders(folder.getFolder()));
                    if (images.size() > 100) {
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

    public void showPopup(View view, final String folder, final Image image) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.folder_op, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.event_delete:
                        if (ImageUtils.deleteFile(folder))
                            Toast.makeText(getContext(), "delete...", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getContext(), "can't delete...", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.share:
                        List<Image> images = ImageService.getImagesFromSameFolders(folder);
                        ActivityUtil.shareImages(getContext(), images);
                        break;
                    case R.id.tag:
                        FolderDialogFragment dialog = FolderDialogFragment.newInstance(null, folder);
                        dialog.show(getFragmentManager(), "Folder");
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderHolder> {
        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FolderHolder(LayoutInflater.from(FolderFragment.this.getContext())
                    .inflate(R.layout.folder_image_layout, parent, false));
        }

        @Override
        public int getItemCount() {
            return folderCovers.size();
        }

        @Override
        public void onBindViewHolder(final FolderHolder holder, final int position) {
            final String folder = folderCovers.get(position).getFolder();

            holder.textView.setText(folder);
            if (selectMode && selectedFolder.containsKey(position)) {
                holder.cardView.setCardBackgroundColor(ORANGE);
            } else {
                holder.cardView.setCardBackgroundColor(WHITE);
            }

            Glide.with(FolderFragment.this.getContext())
                    .load(folderCovers.get(position).getPath())
                    .thumbnail(0.01f)
                    // .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.imageView);

            initListener(holder, folder, position);
        }

        private void initListener(final FolderHolder holder, final String folder, final int position) {
            final Image image = folderCovers.get(position);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectMode) {
                        toggleFolderSelection(holder.cardView, folderCovers.get(position), position);
                    } else {
                        List<Image> images = ImageService.getImagesFromSameFolders(folder);
                        ActivityUtil.showImageList(FolderFragment.this.getContext(), images);
                    }
                }
            });

            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    enterSelectMode(holder.cardView, image, position);
                    return true;
                }
            });

            holder.more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(holder.more, folder, folderCovers.get(position));
                }
            });
        }

        private void toggleFolderSelection(CardView card, Image image, int pos) {
            if (selectedFolder.containsKey(pos)) {
                unSelectFolder(card, pos);
            } else {
                selectFolder(card, image, pos);
            }
        }

        private void selectFolder(CardView card, Image image, int pos) {
            card.setCardBackgroundColor(ORANGE);
            selectedFolder.put(pos, image);
        }

        private void unSelectFolder(CardView card, int pos) {
            card.setCardBackgroundColor(WHITE);
            selectedFolder.remove(pos);
        }

        public void selectAll() {
            for (int i = 0; i < folderCovers.size(); ++i)
                selectedFolder.put(i, folderCovers.get(i));
            notifyDataSetChanged();
        }

        private void unSelectAll() {
            selectedFolder.clear();
            notifyDataSetChanged();
        }

        class FolderHolder extends RecyclerView.ViewHolder {
            public ImageView imageView;
            public TextView textView;
            public CardView cardView;
            public ImageButton more;

            public FolderHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.folder_image);
                textView = view.findViewById(R.id.folder_text);
                cardView = view.findViewById(R.id.folder_card);
                more = view.findViewById(R.id.folder_more);
            }
        }
    }

    public void enterSelectMode(CardView cardView, Image image, int position) {
        selectMode = true;
        folderAdapter.selectFolder(cardView, image, position);
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

    public void setFolderCovers(List<Image> folderCovers) {
        this.folderCovers = folderCovers;
        this.folderAdapter.notifyDataSetChanged();
        refresh.setRefreshing(false);
    }

    public void setOperater(Operater operater) {
        this.operater = operater;
    }

    @Override
    public void setName(String s) {
        this.name = s;
    }

    @Override
    public String getName() {
        return this.name;
    }

}
