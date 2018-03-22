package com.koncle.imagemanagement.fragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
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
import java.util.List;

/**
 * Created by Koncle on 2018/1/19.
 */

public class MyMapFragment extends SupportMapFragment
        implements HasName, GeocodeSearch.OnGeocodeSearchListener, AMap.OnCameraChangeListener,
        ClusterRender, ClusterClickListener, ImageChangeObserver, AMapLocationListener, LocationSource,
        AMap.OnMyLocationChangeListener {
    private static final float MAX_ZOOM_LEVEL = 3.0f;
    private static final float MIN_ZOOM_LEVEL = 20.0f;
    final String name = DrawerActivity.MAP_FRAGMENT_NAME;
    // locate
    public AMapLocationClient mlocationClient = null;
    public AMapLocationClientOption mLocationOption = null;
    Operator operator;
    AMap aMap;
    List<Image> images;
    LruCache<Long, Bitmap> markerBitmapCache = new LruCache<Long, Bitmap>(1024 * 1024 * 8) {
        @Override
        protected int sizeOf(Long key, Bitmap value) {
            return value.getWidth() * value.getHeight();
        }
    };
    private View mapLayout = null;
    private MapView mapView = null;
    private ClusterOverlay clusterOverlay;
    private int markerHeight = 70;
    private int markerWidth = 70;
    private int clusterRadius = 50;
    // decode position with (lat, lng)
    private GeocodeSearch geocodeSearch;
    private LatLng locateAddress = null;
    private ImageButton locateButton;
    private ImageButton locateStopButton;
    private float currentZoomLevel = 19.f;
    private Marker locateMarker;
    private OnLocationChangedListener onLocationChangedListener;

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

                        /*
                        geocodeSearch = new GeocodeSearch(getContext());
                        geocodeSearch.setOnGeocodeSearchListener(MyMapFragment.this);

                        //设置显示定位按钮 并且可以点击
                        UiSettings settings = aMap.getUiSettings();
                        //设置定位监听
                        aMap.setLocationSource(MyMapFragment.this);
                        // 是否显示定位按钮
                        settings.setMyLocationButtonEnabled(true);
                        // 是否可触发定位并显示定位层
                        aMap.setMyLocationEnabled(true);

                        aMap.setOnMyLocationChangeListener(MyMapFragment.this);
                        */

                        initImages();

                        initLocationOperations();
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

    private void initLocationOperations() {
        locateButton = mapLayout.findViewById(R.id.locate_button);
        locateStopButton = mapLayout.findViewById(R.id.locate_stop_button);

        locateButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    1);
                        } else {
                            if (mlocationClient == null) {
                                locateMarker = aMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
                                startLocating();
                                locateStopButton.setVisibility(View.VISIBLE);
                            } else {
                                locate();
                            }
                        }
                    }
                }
        );

        locateStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocating();
                locateStopButton.setVisibility(View.GONE);
            }
        });
    }

    private void initImages() {
        new Thread() {
            @Override
            public void run() {
                if (clusterOverlay != null) {
                    clusterOverlay.onDestroy();
                }

                images = ImageService.getImagesWithLoc();
                List<ClusterItem> items = new ArrayList<>();
                for (int i = 0; i < images.size(); i++) {
                    Image image = images.get(i);
                    image.setPos(convert2LatLng(image.getLat(), image.getLng()));
                    items.add(image);
                }

                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(items.get(0).getPosition(), MAX_ZOOM_LEVEL));

                clusterOverlay = new ClusterOverlay(aMap, items, dp2px(getActivity(), clusterRadius), getActivity());
                clusterOverlay.setClusterRenderer(MyMapFragment.this);
                clusterOverlay.setOnClusterClickListener(MyMapFragment.this);
            }
        }.start();
    }

    public void refreshMarkers() {
        if (clusterOverlay == null) return;
        initImages();
    }

    private LatLng convert2LatLng(String lat, String lng) {
        LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
        return MapUtils.convert2AMapCoord(getContext(), latLng);
    }

    public void startLocating() {
        mlocationClient = new AMapLocationClient(getContext());
        //初始化定位参数
        mLocationOption = new AMapLocationClientOption();

        //设置定位监听
        mlocationClient.setLocationListener(this);
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //设置定位参数
        mlocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        mlocationClient.startLocation();
        mlocationClient.getLastKnownLocation();
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locateAddress, currentZoomLevel));
    }

    public void locate() {
        mlocationClient.getLastKnownLocation();
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locateAddress, currentZoomLevel));
    }

    public void stopLocating() {
        Log.e("locating", "stop locating");
        if (mlocationClient != null)
            mlocationClient.onDestroy();

        if (locateMarker != null)
            locateMarker.setPosition(new LatLng(0, 0));

        mlocationClient = null;
        mLocationOption = null;
    }

    @Override
    public void onLocationChanged(AMapLocation amapLocation) {

        if (amapLocation != null) {
            Log.e("locating", "location changed " + amapLocation.getAddress());

            if (amapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表

                amapLocation.getAccuracy();//获取精度信息
                //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //Date date = new Date(amapLocation.getTime());
                //df.format(date);//定位时间

                locateAddress = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                LatLng gps = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                LatLng gd = MapUtils.convert2AMapCoord(getContext(), gps);
                Log.e("cord", "gps " + gps);
                Log.e("cord", "gd " + gd);

                locateAddress = gps;

                if (locateMarker == null) {

                    Log.e("locating", "location changed " + locateAddress);

                    locateMarker = aMap.addMarker(new MarkerOptions().position(locateAddress));

                    locateMarker.setAnchor((float) locateAddress.latitude, (float) locateAddress.longitude);
                } else {
                    locateMarker.setPosition(locateAddress);
                }
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + amapLocation.getErrorCode() + ", errInfo:"
                        + amapLocation.getErrorInfo());
            }
        }
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
        if (mlocationClient != null)
            mlocationClient.startLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (mlocationClient != null)
            mlocationClient.stopLocation();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mapView != null)
            mapView.onDestroy();

        if (clusterOverlay != null)
            clusterOverlay.onDestroy();

        if (mlocationClient != null)
            mlocationClient.onDestroy();

        if (markerBitmapCache != null)
            markerBitmapCache.evictAll();
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
        Bitmap bitmap = markerBitmapCache.get(((Image) clusterItem).getId());
        if (bitmap == null) {
            bitmap = ImageUtils.loadBitmap(((Image) clusterItem).getPath(), markerWidth, markerHeight);
            if (bitmap == null) {
                // ImageService.deleteImageByPath(((Image)clusterItem).getPath());
                return null;
            }
            markerBitmapCache.put(((Image) clusterItem).getId(), bitmap);
        }

        Log.w("bitmapCache", "hit : " + markerBitmapCache.hitCount() +
                "  miss : " + markerBitmapCache.missCount() +
                " rate : " + markerBitmapCache.hitCount() / (0.0f + markerBitmapCache.missCount() + markerBitmapCache.hitCount()));

        View view = LayoutInflater.from(getContext()).inflate(R.layout.map_item_layout, null, true);
        ImageView imageView = view.findViewById(R.id.map_image);

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = markerWidth;
        params.height = markerHeight;
        imageView.setLayoutParams(params);

        imageView.setImageBitmap(bitmap);

        TextView textView = view.findViewById(R.id.map_text);
        textView.setText(String.valueOf(clusterNum));
        return view;
    }

    // when user click image, this method will be called
    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
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

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        currentZoomLevel = cameraPosition.zoom;
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        this.onLocationChangedListener = onLocationChangedListener;
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void onMyLocationChange(Location location) {
        Log.e("location", " ");
    }
}
