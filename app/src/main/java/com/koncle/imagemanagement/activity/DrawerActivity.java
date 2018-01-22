package com.koncle.imagemanagement.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
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
import com.koncle.imagemanagement.service.ImageListenerService;
import com.koncle.imagemanagement.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Koncle on 2018/1/20.
 */

public class DrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Operator {

    public static final String WATCH_TAG = "folders";
    private static final boolean INIT_TABLES = false;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SCAN_OK_SHOW = 2;
    private static final int FOLDER_FRAGMENT = 0;
    private static final int EVENT_FRAGMENT = 1;
    private static final int MAP_FRAGMENT = 2;
    private final int SCAN_OK = 1;
    private int curFragmentIndex = FOLDER_FRAGMENT;
    private ProgressDialog progressDialog;
    private List<Fragment> fragments;

    private Handler mHandler = new MyHandler();
    private DrawerLayout drawer;
    private List<MySearchSuggestion> searchHistory = new ArrayList<>();
    private FloatingSearchView fsv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        initDataBase(INIT_TABLES); // 31.12416648864746 : 120.62750244140625
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
                    mHandler.sendEmptyMessage(SCAN_OK_SHOW);
                }
            }).start();
        }
    }

    private void show() {
        initToolbar();
        initFragments();
        initWatcher();
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
                String text = Pattern.compile(fsv.getQuery(), Pattern.CASE_INSENSITIVE)
                        .matcher(item.getBody())
                        .replaceFirst("<font color=\"#000\">" + fsv.getQuery() + "</font>");
                textView.setText(Html.fromHtml(text));
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

    private void initWatcher() {
        List<String> folders = new ArrayList<>();
        for (Image i : ImageService.getFolders()) {
            int index = i.getPath().lastIndexOf("/");
            folders.add(i.getPath().substring(0, index));
        }
        Intent intent = new Intent(this, ImageListenerService.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(WATCH_TAG, (ArrayList<String>) folders);
        intent.putExtras(bundle);
        startService(intent);
    }

    private void initFragments() {
        // init
        fragments = new ArrayList<>();
        fragments.add(FolderFragment.newInstance("folder", this));
        fragments.add(EventFragment.newInstance("event", this));
        fragments.add(MyMapFragment.newInstance("map", this));

        // show first fragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.main_fragment, fragments.get(curFragmentIndex), ((HasName) fragments.get(curFragmentIndex)).getName())
                .commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == SelectImageActivity.RESULT_CODE) {
            List<Image> images = data.getExtras().getParcelableArrayList(SelectImageActivity.IMAGES);
            ((EventFragment) fragments.get(1)).handleResult(images);
        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.map:
                ActivityUtil.showMap(this, ImageService.getImagesWithLoc());
                break;
            case R.id.refresh_data:
                refreshData();
                break;
            case R.id.search:

        }
        return super.onOptionsItemSelected(item);
    }
    */

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.folders_fragment) {
            switchFragment(0);
        } else if (id == R.id.events_fragment) {
            switchFragment(1);
        } else if (id == R.id.map_fragment) {
            switchFragment(2);
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void refreshData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ImageService.getSystemPhotoList(DrawerActivity.this);
                mHandler.sendEmptyMessage(SCAN_OK);
            }
        }).start();
    }

    @Override
    public void hideToolbar() {
        fsv.setVisibility(View.GONE);
    }

    @Override
    public void showToolbar() {
        fsv.setVisibility(View.VISIBLE);
    }

    private class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_OK_SHOW:
                    progressDialog.dismiss();
                    show();
                    break;
                case SCAN_OK:
                    List<Image> folders = ImageService.getFolders();
                    ((FolderFragment) fragments.get(0)).setFolderCovers(folders);
                    break;
            }
        }
    }
}
