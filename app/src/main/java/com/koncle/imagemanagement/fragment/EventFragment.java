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
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.DrawerActivity;
import com.koncle.imagemanagement.activity.ImageChangeListener;
import com.koncle.imagemanagement.activity.WeakReference;
import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dialog.InputDialogFragment;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class EventFragment extends Fragment implements HasName, ImageChangeListener {
    private static final String TAG = EventFragment.class.getSimpleName();
    public static String className = EventFragment.class.getSimpleName();

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

        ((SimpleItemAnimator) eventsRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        eventsRecyclerView.getItemAnimator().setChangeDuration(0);

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


        public void refreshData() {
            events = ImageService.getEvents();
            notifyItemRangeChanged(0, getItemCount());
        }

        public void refreshData(List<Event> refreshEvents) {
            for (Event event : refreshEvents) {
                int index = events.indexOf(event);
                if (index != 0) {
                    events.set(index, event);
                }
            }
            notifyItemRangeChanged(0, getItemCount());
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (getItemViewType(position) == EVENT_TYPE) {
                final EventHolder finalHolder = (EventHolder) holder;
                final Event event = events.get(position);
                finalHolder.title.setText(event.getName());
                finalHolder.innerAdapter.setData(event);

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
                        ImageService.deleteEvent(event);
                        events.remove(position);
                        // item remove will not call onbindview again
                        eventAdapter.notifyItemRemoved(position);
                        eventAdapter.notifyItemRangeChanged(position, events.size() - position);
                    }
                });
                finalHolder.modify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputDialogFragment dialog = InputDialogFragment.newInstance(new InputDialogFragment.OnInputFinished() {
                            @Override
                            public void inputFinished(String eventName) {
                                ImageService.updateEvent(event, eventName);
                                finalHolder.title.setText(eventName);
                            }
                        }, "A New Title");
                        dialog.show(getActivity().getFragmentManager(), "modify");
                    }
                });
                finalHolder.show.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityUtil.showImageList(getContext(), event, event.getName());
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
            ImageButton show;

            EventHolder(View view) {
                super(view);
                recyclerView = view.findViewById(R.id.event_outer_recycler_view);

                LinearLayoutManager manager = new LinearLayoutManager(EventFragment.this.getContext());
                innerAdapter = new InnerEventAdapter(manager);
                manager.setOrientation(LinearLayoutManager.HORIZONTAL);

                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(innerAdapter);


                ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
                recyclerView.getItemAnimator().setChangeDuration(0);

                title = view.findViewById(R.id.event_title);
                add = view.findViewById(R.id.event_add_image);
                delete = view.findViewById(R.id.event_delete);
                modify = view.findViewById(R.id.event_modify_name);
                show = view.findViewById(R.id.event_show_list);
            }
        }
    }

    class InnerEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final LinearLayoutManager manager;
        private List<Image> images;

        private final int LINE = 1;
        private final int IMAGE = 0;
        private Event event;

        void setData(Event event) {
            event.resetImageList();
            this.images = event.getImageList();
            notifyItemRangeChanged(0, images.size());
            this.event = event;
        }

        void refreshData() {
            event.resetImageList();
            this.images = event.getImageList();
            notifyItemRangeChanged(0, images.size());
        }

        void deleteImage(int position) {
            images.remove(position);
            notifyItemRemoved(position);
            // is the last
            if (position == images.size()) {
                // refresh previous item
                notifyItemRangeChanged(position - 1, 1);
            } else {
                notifyItemRangeChanged(position, images.size() - position);
            }
        }

        // 1 3 5 7 9
        Image getImageByPosition(int pos) {
            return images.get(pos);
        }

        Event getEvent() {
            return event;
        }

        public InnerEventAdapter(LinearLayoutManager manager) {
            this.manager = manager;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new EventImageHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_inner_item_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            // LINE
            if (position == this.images.size() - 1) {
                ((EventImageHolder) holder).line.setVisibility(View.GONE);
            } else {
                ((EventImageHolder) holder).line.setVisibility(View.VISIBLE);
            }

            final EventImageHolder imageHolder = (EventImageHolder) holder;
            Image image = this.images.get(position);
            Glide.with(EventFragment.this)
                    .load(image.getPath())
                    .asBitmap()
                    .into(imageHolder.image);
            // GIF
            if (image.getType() == Image.TYPE_GIF) {
                ((EventImageHolder) holder).gifText.setVisibility(View.VISIBLE);
            } else {
                ((EventImageHolder) holder).gifText.setVisibility(View.GONE);
            }
            holder.itemView.setTag(position);
            imageHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityUtil.showSingleImageWithPos(getContext(), events.get(position), position, imageHolder.image);

                    adapterWaitingForAddImageResult = InnerEventAdapter.this;
                }
            });
            imageHolder.image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showPopup(imageHolder.image, position, InnerEventAdapter.this);
                    return true;
                }
            });
        }


        @Override
        public int getItemCount() {
            return images.size();
        }

        class EventImageHolder extends RecyclerView.ViewHolder {
            ImageView image;
            TextView gifText;
            RelativeLayout line;

            public EventImageHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.event_image);
                gifText = itemView.findViewById(R.id.gif_text);
                line = itemView.findViewById(R.id.line);
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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

    public void onImageDeleted(Image image) {
        if (eventAdapter == null) return;
        List<Event> concernedEvents = image.getEvents();
        eventAdapter.refreshData(concernedEvents);
    }

    @Override
    public void onImageAdded(Image image) {

    }

    public void onImageMoved(Image oldImage, Image newImage) {
        if (eventAdapter == null) return;
        eventAdapter.refreshData();
    }

    public void addImage2Events(List<Image> images) {
        if (images != null) {
            ImageService.recoverDaoSession(images);
            ImageService.addImages2EventInThread(events.get(eventPositionWaitingForAddImageResult), images);
        }
    }

    public void onImageAddedToAnEvent() {
        if (adapterWaitingForAddImageResult == null) return;

        Event event = events.get(eventPositionWaitingForAddImageResult);
        event.resetImageList();
        adapterWaitingForAddImageResult.setData(event);
        Toast.makeText(getContext(), "image added", Toast.LENGTH_SHORT).show();

        eventPositionWaitingForAddImageResult = -1;
        adapterWaitingForAddImageResult = null;
        WeakReference.removeSelections();
    }

    public void onImageDeleted() {
        if (adapterWaitingForAddImageResult == null) return;
        adapterWaitingForAddImageResult.refreshData();
    }
}
