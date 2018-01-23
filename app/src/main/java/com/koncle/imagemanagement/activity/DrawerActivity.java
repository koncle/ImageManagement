package com.koncle.imagemanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.MySearchSuggestion;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.fragment.EventFragment;
import com.koncle.imagemanagement.fragment.FolderFragment;
import com.koncle.imagemanagement.fragment.HasName;
import com.koncle.imagemanagement.fragment.MyMapFragment;
import com.koncle.imagemanagement.fragment.Operator;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.view.View.GONE;

/**
 * Created by Koncle on 2018/1/20.
 */

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Operator {

    public static final String WATCH_TAG = "folders";
    private static final boolean INIT_TABLES = false;
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int FOLDER_FRAGMENT = 0;
    private static final int EVENT_FRAGMENT = 1;
    private static final int MAP_FRAGMENT = 2;

    public static final int SCAN_OK = 1;
    public static final int SCAN_OK_SHOW = 2;
    public static final int IMAGE_ADDED = 3;
    public static final int IMAGE_DELETED = 4;

    public static final String FOLDER_FRAGMENT_NAME = "folder";
    public static final String EVENT_FRAGMENT_NAME = "event";
    public static final String MAP_FRAGMENT_NAME = "map";

    private int curFragmentIndex = FOLDER_FRAGMENT;
    private ProgressDialog progressDialog;
    private List<Fragment> fragments;

    private DrawerLayout drawer;
    private List<MySearchSuggestion> searchHistory = new ArrayList<>();
    private FloatingSearchView fsv;

    private MyHandler handler;
    private List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        if (savedInstanceState != null) {
            curFragmentIndex = savedInstanceState.getInt("cur");
        }

        initDataBase(INIT_TABLES);
        Log.w(TAG, "onCreate");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("cur", curFragmentIndex);
    }

    private void initDataBase(boolean refresh) {
        ImageService.init(this);

        SharedPreferences sp = this.getSharedPreferences("dbPre", MODE_PRIVATE);
        if (refresh || !sp.getBoolean("create", false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("create", true);
            editor.apply();
            ImageService.refreshTables();
            initData(true);
        } else {
            initData(false);
        }
    }

    private void initData(boolean load) {
        if (!load) {
            show();
        } else {
            progressDialog = ProgressDialog.show(this, null, "Loading...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageService.getSystemPhotoList(DrawerActivity.this);
                    handler.sendEmptyMessage(SCAN_OK_SHOW);
                }
            }).start();
        }
    }

    private void show() {
        initToolbar();
        initFragments();
        //initWatcher();
        initFileObserver();
    }

    private void initFileObserver() {
        MsgCenter.init(this);
        handler = new MyHandler();
        MsgCenter.addHandler(handler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MsgCenter.close(this);
    }

    private void initToolbar() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer = findViewById(R.id.drawer_layout);

        fsv = findViewById(R.id.floating_search_view);
        fsv.attachNavigationDrawerToMenuButton(drawer);

        fsv.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                Toast.makeText(DrawerActivity.this, "change " + newQuery, Toast.LENGTH_SHORT).show();
            }
        });

        fsv.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.map:
                        ActivityUtil.showMap(DrawerActivity.this, ImageService.getImagesWithLoc());
                        break;
                    case R.id.refresh_data:
                        refreshData();
                        break;
                }
            }
        });

        fsv.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                fsv.swapSuggestions(searchHistory);
            }

            @Override
            public void onFocusCleared() {

            }
        });

        fsv.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                if (searchHistory.contains(item)) {
                    leftIcon.setImageDrawable(getDrawable(R.drawable.ic_history_black_24dp));
                } else {
                    leftIcon.setImageDrawable(getDrawable(R.drawable.search));
                }
                StringBuilder sb = new StringBuilder();
                int type = ((MySearchSuggestion) item).getType();
                if (type == MySearchSuggestion.TYPE_EVENT) {
                    sb.append("<strong>E:</strong> ");
                } else if (type == MySearchSuggestion.TYPE_TAG) {
                    sb.append("<strong>T:</strong> ");
                }
                String text = Pattern.compile(fsv.getQuery(), Pattern.CASE_INSENSITIVE)
                        .matcher(item.getBody())
                        .replaceFirst("<font color=\"#000\">" + fsv.getQuery() + "</font>");
                textView.setText(Html.fromHtml(sb.append(text).toString()));
            }
        });

        fsv.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                newQuery = newQuery.trim();
                if (!"".equals(newQuery)) {
                    fsv.swapSuggestions(ImageService.findSuggestions(newQuery));
                } else {
                    fsv.swapSuggestions(searchHistory);
                }
            }
        });

        fsv.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                showImagesByQuery(searchSuggestion, searchSuggestion.getBody());
                if (!searchHistory.contains(searchSuggestion))
                    searchHistory.add((MySearchSuggestion) searchSuggestion);
            }

            @Override
            public void onSearchAction(String currentQuery) {
                // show first suggestion
                List<MySearchSuggestion> suggestions = ImageService.findSuggestions(currentQuery);
                if (suggestions.size() > 0) {
                    showImagesByQuery(suggestions.get(0), currentQuery);
                    if (!searchHistory.contains(suggestions.get(0)))
                        searchHistory.add(suggestions.get(0));
                } else {
                    showImagesByQuery(null, currentQuery);
                }
            }
        });
    }

    private void showImagesByQuery(SearchSuggestion searchSuggestion, String query) {
        if (searchSuggestion != null) {
            if (((MySearchSuggestion) searchSuggestion).getType() == MySearchSuggestion.TYPE_TAG) {
                Tag tag = ImageService.searchTagByName(searchSuggestion.getBody());
                if (tag != null) {
                    ActivityUtil.showImageList(DrawerActivity.this, tag.getImages(), "search for : " + query);
                    return;
                }
            } else {
                Event event = ImageService.searchEventByName(searchSuggestion.getBody());
                if (event != null) {
                    ActivityUtil.showImageList(DrawerActivity.this, event.getImageList(), "search for : " + query);
                    return;
                }
            }
        }
        ActivityUtil.showImageList(DrawerActivity.this, null, "No Result for : " + query);
    }

    private Fragment getFragment(String name) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(name);
        if (fragment == null) {
            switch (name) {
                case EVENT_FRAGMENT_NAME:
                    fragment = EventFragment.newInstance();
                    break;
                case FOLDER_FRAGMENT_NAME:
                    fragment = FolderFragment.newInstance();
                    break;
                case MAP_FRAGMENT_NAME:
                    fragment = MyMapFragment.newInstance();
                    break;

            }
        }
        return fragment;
    }

    private void initFragments() {
        names = new ArrayList<>();
        names.add(FOLDER_FRAGMENT_NAME);
        names.add(EVENT_FRAGMENT_NAME);
        names.add(MAP_FRAGMENT_NAME);

        // ensure when the phone has been rotated,
        // the fragments can be loaded again from original manager
        fragments = new ArrayList<>();
        for (String name : names) {
            fragments.add(getFragment(name));
        }
        Fragment f = fragments.get(curFragmentIndex);
        if (!f.isAdded()) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment, f, ((HasName) f).getName())
                    .commit();
        }

        Log.w(TAG, "init Fragment");
    }

    private void switchFragment(int pos) {
        if (pos != curFragmentIndex) {
            Fragment curF = fragments.get(curFragmentIndex);
            Fragment nextF = fragments.get(pos);
            if (nextF.isAdded()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(curF)
                        .show(nextF)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .hide(curF)
                        .add(R.id.main_fragment, nextF, ((HasName) nextF).getName())
                        .commit();
            }
            curFragmentIndex = pos;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fsv.isSearchBarFocused()) {
            fsv.clearSearchFocus();
        } else {
            Fragment fragment = fragments.get(curFragmentIndex);
            switch (curFragmentIndex) {
                case FOLDER_FRAGMENT:
                    if (!((FolderFragment) fragment).exitSelectMode())
                        super.onBackPressed();
                    break;
                case EVENT_FRAGMENT:
                    super.onBackPressed();
                    break;
                case MAP_FRAGMENT:
                    super.onBackPressed();
                    break;
                default:
                    super.onBackPressed();
                    break;
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.folders_fragment) {
            switchFragment(FOLDER_FRAGMENT);
        } else if (id == R.id.events_fragment) {
            switchFragment(EVENT_FRAGMENT);
        } else if (id == R.id.map_fragment) {
            switchFragment(MAP_FRAGMENT);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SelectImageActivity.RESULT_CODE) {
            List<Image> images = data.getExtras().getParcelableArrayList(SelectImageActivity.IMAGES);
            ((EventFragment) fragments.get(1)).handleResult(images);
        } else if (resultCode == MultiColumnImagesActivity.RESULT_DELETE_IMAGE) {
            boolean deleteNum = data.getBooleanExtra(MultiColumnImagesActivity.RESULT_DELETE_IMAGE_NUM, false);
            String deleteFolder = data.getStringExtra(MultiColumnImagesActivity.RESULT_DELETE_IMAGE_FOLDER);
            ((FolderFragment) fragments.get(0)).handleResult(deleteNum, deleteFolder);
        }
    }

    @Override
    public void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageService.getSystemPhotoList(DrawerActivity.this);
                handler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

    @Override
    public void hideToolbar() {
        fsv.setVisibility(GONE);
    }

    @Override
    public void showToolbar() {
        fsv.setVisibility(View.VISIBLE);
    }

    public class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            Image image;
            List<Image> folders;
            switch (msg.what) {
                case SCAN_OK_SHOW:
                    progressDialog.dismiss();
                    show();
                    break;
                case SCAN_OK:
                    folders = ImageService.getFolders();
                    ((FolderFragment) fragments.get(0)).setFolderCovers(folders);
                    break;
                // notify folder to change images
                case IMAGE_ADDED:
                    image = msg.getData().getParcelable("image");
                    ((FolderFragment) fragments.get(0)).refreshCover(image);
                    break;
                case IMAGE_DELETED:
                    image = msg.getData().getParcelable("image");
                    ((FolderFragment) fragments.get(0)).refreshCover(image);
                    break;
                case MultiColumnImagesActivity.IMAGE_MOVED:
                    //image = msg.getData().getParcelable("image");
                    refreshData();
                    break;
            }
        }
    }
}
