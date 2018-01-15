package com.koncle.imagemanagement.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 10976 on 2018/1/12.
 */

public class FolderFragment extends Fragment implements HasName {
    private String name;
    private FolderRecyclerViewAdapter folderAdapter;
    private List<Image> folders;
    private boolean selectMode = false;
    private Map<Integer, Image> selectedFolder = new HashMap<>();
    ;

    final int ORANGE = Color.rgb(255, 223, 0);
    final int WHITE = Color.WHITE;
    private Operater operater;
    private SwipeRefreshLayout refresh;

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

        folders = ImageService.getFolders();
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
        return view;
    }

    public void setOperater(Operater operater) {
        this.operater = operater;
    }

    class FolderRecyclerViewAdapter extends RecyclerView.Adapter<FolderRecyclerViewAdapter.FolderHolder> {
        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FolderHolder(LayoutInflater.from(FolderFragment.this.getContext())
                    .inflate(R.layout.folder_image_layout, parent, false));
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        @Override
        public void onBindViewHolder(final FolderHolder holder, final int position) {
            final String folder = folders.get(position).getFolder();

            holder.textView.setText(folder);
            if (selectMode && selectedFolder.containsKey(position)) {
                holder.cardView.setCardBackgroundColor(ORANGE);
            } else {
                holder.cardView.setCardBackgroundColor(WHITE);
            }

            Glide.with(FolderFragment.this.getContext())
                    .load(folders.get(position).getPath())
                    .thumbnail(0.01f)
                    // .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(holder.imageView);

            initListener(holder, folder, position);
        }

        private void initListener(final FolderHolder holder, final String folder, final int position) {
            final Image image = folders.get(position);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectMode) {
                        toggleFolderSelection(holder.cardView, folders.get(position), position);
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
                    ActivityUtil.showPopup(getContext(), holder.more);
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
            for (int i = 0; i < folders.size(); ++i)
                selectedFolder.put(i, folders.get(i));
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
    }

    public boolean exitSelectMode() {
        if (selectMode) {
            selectMode = false;
            folderAdapter.unSelectAll();
            return true;
        } else {
            return false;
        }
    }

    public void setFolders(List<Image> folders) {
        this.folders = folders;
        this.folderAdapter.notifyDataSetChanged();
        refresh.setRefreshing(false);
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
