package com.koncle.imagemanagement.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 10976 on 2018/1/17.
 */

public abstract class BaseDialogFragment extends DialogFragment implements View.OnClickListener {

    private List<Tag> allTags;

    private Map<Integer, Tag> selectedTags;

    public Map<Integer, Tag> getSelectedTags() {
        return selectedTags;
    }

    public void setData(List<Tag> tags) {
        allTags = ImageService.getTags();
        selectedTags = new HashMap<>();
        if (tags != null) {
            for (Tag tag : tags) {
                int i = allTags.indexOf(tag);
                selectedTags.put(i, tag);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_tag_layout, null);
        initViews(view);
        return view;
    }

    private void initViews(View root) {
        // tag list
        RecyclerView recyclerView = root.findViewById(R.id.tag_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        final TagAdapter tagAdapter = new TagAdapter();
        recyclerView.setAdapter(tagAdapter);

        // Input Operations
        final LinearLayout inputLayout = root.findViewById(R.id.dialog_input_liearlayout);

        Button addTagButton = root.findViewById(R.id.dialog_add_tag);
        addTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.VISIBLE);
            }
        });

        ImageButton close = root.findViewById(R.id.dialog_input_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.GONE);
            }
        });

        ImageButton complete = root.findViewById(R.id.dialog_input_complete);
        final EditText tagInput = root.findViewById(R.id.dialog_input);
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputLayout.setVisibility(View.GONE);
                String inputTag = tagInput.getText().toString();
                if (!"".equals(inputTag.trim())) {
                    Tag tag = ImageService.addTagIfNotExists(inputTag);
                    if (!allTags.contains(tag))
                        allTags.add(tag);
                    tagAdapter.notifyDataSetChanged();
                    tagInput.setText("");
                }
                inputLayout.setVisibility(View.GONE);
            }
        });

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
                BaseDialogFragment.this.onClick(v);
                dismiss();
            }
        });
    }

    class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagHolder> {

        @Override
        public TagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_tag_item_layout, parent, false);
            return new TagHolder(v);
        }

        @Override
        public void onBindViewHolder(final TagHolder holder, final int position) {
            if (selectedTags.containsKey(position)) {
                holder.select.setChecked(true);
            } else {
                holder.select.setChecked(false);
            }

            holder.tag.setText(allTags.get(position).getTag());

            holder.select.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedTags.put(position, allTags.get(position));
                    } else {
                        selectedTags.remove(position);
                    }
                }
            });

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.select.toggle();
                }
            });
        }

        @Override
        public int getItemCount() {
            return allTags.size();
        }

        class TagHolder extends RecyclerView.ViewHolder {
            public TextView tag;
            public CheckBox select;
            public RelativeLayout layout;

            public TagHolder(View itemView) {
                super(itemView);
                tag = itemView.findViewById(R.id.dialog_tag);
                select = itemView.findViewById(R.id.dialog_select);
                layout = itemView.findViewById(R.id.dialog_relativelayout);
            }
        }
    }
}
