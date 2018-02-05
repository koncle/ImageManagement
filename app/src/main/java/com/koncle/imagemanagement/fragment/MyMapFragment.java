package com.koncle.imagemanagement.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.DrawerActivity;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.markerClusters.ClusterClickListener;
import com.koncle.imagemanagement.markerClusters.ClusterItem;
import com.koncle.imagemanagement.markerClusters.ClusterOverlay;
import com.koncle.imagemanagement.markerClusters.ClusterRender;
import com.koncle.imagemanagement.message.ImageChangeObserver;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;
import com.koncle.imagemanagement.util.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Koncle on 2018/1/19.
 */

public class MyMapFragment extends SupportMapFragment
        implements HasName, AMap.OnCameraChangeListener,
        GeocodeSearch.OnGeocodeSearchListener, ClusterRender, ClusterClickListener, ImageChangeObserver {
    final String name = DrawerActivity.MAP_FRAGMENT_NAME;
    Operator operator;
    AMap aMap;
    List<Image> images;
    List<Marker> markers = new ArrayList<>();
    LruCache<Long, Bitmap> bitmapLruCache = new LruCache<Long, Bitmap>(1024 * 1024 * 8) {
        @Override
        protected int sizeOf(Long key, Bitmap value) {
            return value.getWidth() * value.getHeight();
        }
    };
    private int markerHeight = 100;
    private int markerWidth = 100;
    private GeocodeSearch geocodeSearch;
    private View mapLayout = null;
    private MapView mapView = null;
    private Map<Integer, Drawable> mBackDrawAbles = new HashMap<Integer, Drawable>();
    private int clusterRadius = 70;
    private ClusterOverlay clusterOverlay;

    public static MyMapFragment newInstance() {
        Bundle args = new Bundle();
        MyMapFragment fragment = new MyMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (mapLayout == null) {
            mapLayout = layoutInflater.inflate(R.layout.map_fragment_layout, null);
            mapView = mapLayout.findViewById(R.id.map);
            mapView.onCreate(bundle);
            final ProgressBar progressBar = mapLayout.findViewById(R.id.progressBar);
            if (aMap == null) {
                aMap = mapView.getMap();
                aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
                    @Override
                    public void onMapLoaded() {
                        progressBar.setVisibility(View.GONE);
                        geocodeSearch = new GeocodeSearch(getContext());
                        geocodeSearch.setOnGeocodeSearchListener(MyMapFragment.this);
                        aMap.setOnCameraChangeListener(MyMapFragment.this);
                        initMarker();
                    }
                });
            }
        } else {
            if (mapLayout.getParent() != null) {
                ((ViewGroup) mapLayout.getParent()).removeView(mapLayout);
            }
        }
        return mapLayout;
    }

    private void initMarker() {
        new Thread() {
            @Override
            public void run() {
                images = ImageService.getImagesWithLoc();
                List<ClusterItem> items = new ArrayList<>();
                for (int i = 0; i < images.size(); i++) {
                    Image image = images.get(i);
                    image.setPos(convert2LatLng(image.getLat(), image.getLng()));
                    items.add(image);
                }
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(items.get(items.size() - 1).getPosition(), 19));

                if (clusterOverlay != null) {
                    clusterOverlay.onDestroy();
                }
                clusterOverlay = new ClusterOverlay(aMap, items, dp2px(getActivity(), clusterRadius), getActivity());
                clusterOverlay.setClusterRenderer(MyMapFragment.this);
                clusterOverlay.setOnClusterClickListener(MyMapFragment.this);
            }
        }.start();
    }

    public void refreshMarkers() {
        if (clusterOverlay == null) return;
        initMarker();
    }

    private LatLng convert2LatLng(String lat, String lng) {
        LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        return MapUtils.convert2AMapCoord(getContext(), latLng);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        geocodeSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getRegeocodeAddress() != null
                    && result.getRegeocodeAddress().getFormatAddress() != null) {
                Toast.makeText(getContext(), result.getRegeocodeAddress().getFormatAddress(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No result", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "error ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        clusterOverlay.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        mapView.onSaveInstanceState(bundle);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    @Override
    public View getDrawAble(int clusterNum, ClusterItem clusterItem) {
        Bitmap bitmap = bitmapLruCache.get(((Image) clusterItem).getId());
        if (bitmap == null) {
            bitmap = ImageUtils.loadBitmap(((Image) clusterItem).getPath(), 60, 60);
            bitmapLruCache.put(((Image) clusterItem).getId(), bitmap);
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.map_item_layout, null, true);
        ImageView imageView = view.findViewById(R.id.map_image);
        imageView.setImageBitmap(bitmap);
        TextView textView = view.findViewById(R.id.map_text);
        textView.setText(String.valueOf(clusterNum));
        return view;
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        /*
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds latLngBounds = builder.build();
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
        */
        List<Image> images = new ArrayList<>();
        for (ClusterItem item : clusterItems) {
            images.add((Image) item);
        }
        ActivityUtil.showImageList(getContext(), images, "From Map");
    }

    private boolean isValidImage(Image image) {
        return !(Double.parseDouble(image.getLat()) == 0 || Double.parseDouble(image.getLng()) == 0);
    }

    @Override
    public void onImageAdded(Image image) {
        if (isValidImage(image)) {
            //clusterOverlay.addClusterItem(image);
            refreshMarkers();
        }
    }

    @Override
    public void onImageMoved(Image oldImage, Image newImage) {
    }

    @Override
    public void onImageDeleted(Image image) {
        if (isValidImage(image)) {
            // clusterOverlay.removeClusterItem(image);
            refreshMarkers();
        }
    }
}
