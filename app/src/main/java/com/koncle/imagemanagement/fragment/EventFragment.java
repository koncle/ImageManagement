package com.koncle.imagemanagement.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class EventFragment extends Fragment implements HasName {
    private String name;
    private EventRecyclerViewAdapter eventAdapter;
    private List<Event> events;

    final int ORANGE = Color.rgb(255, 223, 0);
    final int WHITE = Color.WHITE;
    private Operater operater;
    private SwipeRefreshLayout refresh;

    private int eventPositionWaitingForResult;
    private InnerEventAdapter adapterWaitingForResult;

    public static Fragment newInstance(String name, Operater operater) {
        EventFragment f = new EventFragment();
        f.setName(name);
        f.setOperater(operater);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, null);
        RecyclerView recyclerView = view.findViewById(R.id.event_root_recycler_view);

        this.events = ImageService.getEvents();

        eventAdapter = new EventRecyclerViewAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(eventAdapter);

        FloatingActionButton fab = view.findViewById(R.id.event_add);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventDialogFragment dialog = EventDialogFragment.newInstance(new EventDialogFragment.OnInputFinished() {
                    @Override
                    public void inputFinished(String eventName) {
                        events.add(ImageService.addEvent(eventName));
                        eventAdapter.notifyItemInserted(events.size() - 1);
                    }
                });
                dialog.show(getActivity().getFragmentManager(), "a");
            }
        });

        return view;
    }

    public void showPopup(View view, final int position, final InnerEventAdapter innerEventAdapter) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.event_op, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.event_delete:
                        ImageService.deleteEvent(events.get(position));
                        events.remove(position);
                        eventAdapter.notifyItemRemoved(position);
                        break;
                    case R.id.event_add_image:
                        ActivityUtil.selectImages(getContext());
                        eventPositionWaitingForResult = position;
                        adapterWaitingForResult = innerEventAdapter;
                }
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
            }
        });
        popupMenu.show();
    }


    class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventHolder> {
        private List<InnerEventAdapter> adapters = new ArrayList<>();
        private List<LinearLayoutManager> managers = new ArrayList<>();
        private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

        @Override
        public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            EventHolder holder = new EventHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_outer_item_layout, parent, false));
            // to support multiple recycler views
            holder.recyclerView.setRecycledViewPool(viewPool);
            return holder;
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        @Override
        public void onBindViewHolder(final EventHolder holder, final int position) {
            holder.title.setText(events.get(position).getName());
            holder.innerAdapter.setData(events.get(position).getImageList());
            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopup(holder.imageButton, position, holder.innerAdapter);
                }
            });
        }

        class EventHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;
            TextView title;
            InnerEventAdapter innerAdapter;
            ImageButton imageButton;

            EventHolder(View view) {
                super(view);
                recyclerView = view.findViewById(R.id.event_outer_recycler_view);

                LinearLayoutManager manager = new LinearLayoutManager(EventFragment.this.getContext());
                innerAdapter = new InnerEventAdapter(manager);
                manager.setOrientation(LinearLayoutManager.HORIZONTAL);

                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(innerAdapter);

                title = view.findViewById(R.id.event_title);
                imageButton = view.findViewById(R.id.event_more);
            }
        }
    }

    class InnerEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LinearLayoutManager manager;
        private List<Image> images;
        private int size = 0;

        private final int LINE = 1;
        private final int IMAGE = 0;

        public void setData(List<Image> images) {
            this.images = images;
            this.size = images.size() * 2;
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }

        public InnerEventAdapter(LinearLayoutManager manager) {
            this.manager = manager;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == IMAGE) {
                return new EventImageHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_inner_item_layout, parent, false));
            } else {
                return new EventLineHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_inner_item_line_layout, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (getItemViewType(position) == LINE) {
                if (position == this.size - 1) {
                    ((EventLineHolder) holder).line.setVisibility(View.GONE);
                } else {
                    ((EventLineHolder) holder).line.setVisibility(View.VISIBLE);
                }
            } else {
                final EventImageHolder imageHolder = (EventImageHolder) holder;
                Glide.with(EventFragment.this)
                        .load(this.images.get(position / 2).getPath())
                        .into(imageHolder.image);
                holder.itemView.setTag(position);
                imageHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtil.showSingleImageWithPos(getContext(), images, position / 2, imageHolder.image);
                    }
                });
                Log.w("EventFragment", "item : " + position);
            }
        }

        @Override
        public int getItemCount() {
            return size;
        }

        class EventImageHolder extends RecyclerView.ViewHolder {
            public ImageView image;

            public EventImageHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.event_image);
            }
        }

        class EventLineHolder extends RecyclerView.ViewHolder {
            public ImageView line;

            public EventLineHolder(View itemView) {
                super(itemView);
                line = itemView.findViewById(R.id.line);
            }
        }
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

    public void handleResult(List<Image> images) {
        if (images != null) {
            ImageService.recoverDaoSession(images);
            Event event = ImageService.addImages2Event(events.get(eventPositionWaitingForResult), images);
            adapterWaitingForResult.setData(event.getImageList());
        }
    }
}
