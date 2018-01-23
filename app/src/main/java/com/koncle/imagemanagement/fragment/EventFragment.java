package com.koncle.imagemanagement.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.DrawerActivity;
import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.InputDialogFragment;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.DESCComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class EventFragment extends Fragment implements HasName {
    private static final String TAG = EventFragment.class.getSimpleName();
    private final String name = DrawerActivity.EVENT_FRAGMENT_NAME;
    private RecyclerView eventsRecyclerView;
    private EventRecyclerViewAdapter eventAdapter;
    private List<Event> events;

    final int ORANGE = Color.rgb(255, 223, 0);
    final int WHITE = Color.WHITE;
    private Operator operator;
    private SwipeRefreshLayout refresh;

    private int eventPositionWaitingForAddImageResult;
    private InnerEventAdapter adapterWaitingForAddImageResult;
    private DESCComparator descComparator = new DESCComparator();

    public static Fragment newInstance() {
        EventFragment f = new EventFragment();
        return f;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.event_fragment, null);
        eventsRecyclerView = view.findViewById(R.id.event_root_recycler_view);

        this.events = ImageService.getEvents();

        eventAdapter = new EventRecyclerViewAdapter();
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        eventsRecyclerView.setAdapter(eventAdapter);

        return view;
    }

    private void addEvent() {
        InputDialogFragment dialog = InputDialogFragment.newInstance(new InputDialogFragment.OnInputFinished() {
            @Override
            public void inputFinished(String eventName) {
                if ("".equals(eventName)) {
                    Toast.makeText(getContext(), "Event name can't be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                Event e = ImageService.addEvent(eventName);
                int index = events.indexOf(e);
                if (-1 == index) {
                    events.add(e);
                    eventAdapter.notifyItemInserted(events.size() - 1);
                } else {
                    eventsRecyclerView.smoothScrollToPosition(index);
                }
            }
        }, "Event Name Is Required");
        dialog.show(getActivity().getFragmentManager(), "a");
    }

    public void showPopup(View view, final int pos, final InnerEventAdapter innerEventAdapter) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.event_image_op, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.event_image_delete:
                        ImageService.deleteImageFromEvent(innerEventAdapter.getImageByPosition(pos), innerEventAdapter.getEvent());
                        innerEventAdapter.deleteImage(pos);
                        break;
                }
                return true;
            }
        });
        popupMenu.show();
    }


    class EventRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int ADD_TYPE = 1;
        private static final int EVENT_TYPE = 2;
        private List<InnerEventAdapter> adapters = new ArrayList<>();
        private List<LinearLayoutManager> managers = new ArrayList<>();
        private RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder holder;
            if (viewType == EVENT_TYPE) {
                EventHolder eventholder = new EventHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_outer_item_layout, parent, false));
                // to support multiple recycler views
                eventholder.recyclerView.setRecycledViewPool(viewPool);
                holder = eventholder;
            } else {
                holder = new AddHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.event_outer_add_item_layout, parent, false));
            }
            return holder;
        }

        @Override
        public int getItemCount() {
            return events.size() + 1;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (getItemViewType(position) == EVENT_TYPE) {
                final EventHolder finalHolder = (EventHolder) holder;
                finalHolder.title.setText(events.get(position).getName());
                finalHolder.innerAdapter.setData(events.get(position).getImageList(), events.get(position));

                finalHolder.add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtil.selectImages(getContext());
                        eventPositionWaitingForAddImageResult = position;
                        adapterWaitingForAddImageResult = finalHolder.innerAdapter;
                    }
                });
                finalHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageService.deleteEvent(events.get(position));
                        events.remove(position);
                        eventAdapter.notifyItemRemoved(position);
                    }
                });
                finalHolder.modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputDialogFragment dialog = InputDialogFragment.newInstance(new InputDialogFragment.OnInputFinished() {
                            @Override
                            public void inputFinished(String eventName) {
                                ImageService.updateEvent(events.get(position), eventName);
                                finalHolder.title.setText(eventName);
                            }
                        }, "A New Title");
                        dialog.show(getActivity().getFragmentManager(), "modify");
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == events.size())
                return ADD_TYPE;
            else
                return EVENT_TYPE;
        }

        class AddHolder extends RecyclerView.ViewHolder {
            AddHolder(View itemView) {
                super(itemView);
                itemView.findViewById(R.id.event_card_add).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addEvent();
                    }
                });
            }
        }

        class EventHolder extends RecyclerView.ViewHolder {
            RecyclerView recyclerView;
            public TextView title;
            InnerEventAdapter innerAdapter;
            ImageButton add;
            ImageButton delete;
            ImageButton modify;

            EventHolder(View view) {
                super(view);
                recyclerView = view.findViewById(R.id.event_outer_recycler_view);

                LinearLayoutManager manager = new LinearLayoutManager(EventFragment.this.getContext());
                innerAdapter = new InnerEventAdapter(manager);
                manager.setOrientation(LinearLayoutManager.HORIZONTAL);

                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(innerAdapter);

                title = view.findViewById(R.id.event_title);
                add = view.findViewById(R.id.event_add_image);
                delete = view.findViewById(R.id.event_delete);
                modify = view.findViewById(R.id.event_modify_name);
            }
        }
    }

    class InnerEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LinearLayoutManager manager;
        private List<Image> images;
        private int size = 0;

        private final int LINE = 1;
        private final int IMAGE = 0;
        private Event event;

        void setData(List<Image> images, Event event) {
            this.images = images;
            this.size = images.size() * 2;
            notifyDataSetChanged();
            this.event = event;
        }

        void deleteImage(int position) {
            images.remove(position / 2);
            this.size -= 2;
            if (position == size - 4) {
                notifyItemRangeRemoved(position, 2);
            } else {
                notifyDataSetChanged();
            }
        }

        // 1 3 5 7 9
        Image getImageByPosition(int pos) {
            return images.get(pos / 2);
        }

        Event getEvent() {
            return event;
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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
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
                imageHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showPopup(imageHolder.image, position, InnerEventAdapter.this);
                        return true;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        operator = (Operator) context;
        Log.w(TAG, "onAttach");
    }

    public String getName() {
        return name;
    }

    public void handleResult(List<Image> images) {
        if (images != null) {
            ImageService.recoverDaoSession(images);
            Event event = ImageService.addImages2Event(events.get(eventPositionWaitingForAddImageResult), images);
            event.resetImageList();
            adapterWaitingForAddImageResult.setData(event.getImageList(), event);
        }
    }
}
