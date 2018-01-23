package com.koncle.imagemanagement.dialog;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/17.
 */

public class FolderSelectDialogFragment extends DialogFragment implements View.OnClickListener {

    private List<Image> folders;

    private int pos;
    private LinearLayoutManager manager;

    private OnSelectFinishedListener listener;

    public interface OnSelectFinishedListener {
        void selectFinished(String folderPath);
    }

    public void setListener(OnSelectFinishedListener listener) {
        this.listener = listener;
    }

    public static FolderSelectDialogFragment newInstance(OnSelectFinishedListener onSelectFinishedLisener) {
        Bundle args = new Bundle();
        FolderSelectDialogFragment fragment = new FolderSelectDialogFragment();
        fragment.setArguments(args);
        fragment.setListener(onSelectFinishedLisener);
        return fragment;
    }

    public String getSelectedFolderPath() {
        return ImageUtils.getFolderPathFromPath(folders.get(pos).getPath());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("folders", (ArrayList<? extends Parcelable>) folders);
        outState.putInt("pos", pos);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            folders = savedInstanceState.getParcelableArrayList("folders");
            pos = savedInstanceState.getInt("pos");
        } else {
            folders = ImageService.getFolders();
            pos = 0;
        }

        View view;
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            view = inflater.inflate(R.layout.dialog_folder_select_layout, null);
        } else {
            view = inflater.inflate(R.layout.dialog_folder_select_layout, null);
        }

        initViews(view);
        return view;
    }

    private void initViews(View root) {
        // tag list
        manager = new LinearLayoutManager(getContext());
        RecyclerView recyclerView = root.findViewById(R.id.folder_list);
        recyclerView.setLayoutManager(manager);
        final FolderAdapter folderAdapter = new FolderAdapter();
        recyclerView.setAdapter(folderAdapter);

        // Input Operations
        final LinearLayout inputLayout = root.findViewById(R.id.dialog_input_liearlayout);

        Button cancel = root.findViewById(R.id.dialog_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        root.findViewById(R.id.dialog_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderSelectDialogFragment.this.onClick(v);
                dismiss();
            }
        });
    }

    @Override
    public void onClick(View v) {
        listener.selectFinished(getSelectedFolderPath());
    }

    class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderHolder> {

        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_folder_item_layout, parent, false);
            return new FolderHolder(v);
        }

        @Override
        public void onBindViewHolder(final FolderHolder holder, final int position) {
            if (position == pos) {
                holder.select.setChecked(true);
            } else {
                holder.select.setChecked(false);
            }

            holder.folder.setText(folders.get(position).getFolder());

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != pos) {
                        holder.select.setChecked(true);
                        // set visible previous item uncheck
                        RelativeLayout relativeLayout = (RelativeLayout) manager.findViewByPosition(pos);
                        if (relativeLayout != null) {
                            RadioButton preButton = relativeLayout.findViewById(R.id.dialog_folder_select);
                            preButton.setChecked(false);
                        }
                        pos = position;
                    }
                }
            });

            holder.select.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position != pos) {
                        holder.select.setChecked(true);
                        // set visible previous item uncheck
                        RelativeLayout relativeLayout = (RelativeLayout) manager.findViewByPosition(pos);
                        if (relativeLayout != null) {
                            RadioButton preButton = relativeLayout.findViewById(R.id.dialog_folder_select);
                            preButton.setChecked(false);
                        }
                        pos = position;
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        class FolderHolder extends RecyclerView.ViewHolder {
            public TextView folder;
            public RadioButton select;
            public RelativeLayout relativeLayout;

            public FolderHolder(View itemView) {
                super(itemView);
                folder = itemView.findViewById(R.id.dialog_folder);
                select = itemView.findViewById(R.id.dialog_folder_select);
                relativeLayout = itemView.findViewById(R.id.dialog_folder_layout);
            }
        }
    }
}
