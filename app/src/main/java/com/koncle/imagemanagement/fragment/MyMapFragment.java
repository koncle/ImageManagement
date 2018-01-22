package com.koncle.imagemanagement.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.SupportMapFragment;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.markerClusters.ClusterClickListener;
import com.koncle.imagemanagement.markerClusters.ClusterItem;
import com.koncle.imagemanagement.markerClusters.ClusterOverlay;
import com.koncle.imagemanagement.markerClusters.ClusterRender;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Koncle on 2018/1/19.
 */

public class MyMapFragment extends SupportMapFragment
        implements HasName, AMap.OnCameraChangeListener,
        GeocodeSearch.OnGeocodeSearchListener, ClusterRender, ClusterClickListener {
    String name;
    Operator operator;
    AMap aMap;
    List<Image> images;
    private int markerHeight = 100;
    private int markerWidth = 100;
    private GeocodeSearch geocodeSearch;
    private View mapLayout = null;
    List<Marker> markers = new ArrayList<>();
    private MapView mapView = null;

    private Map<Integer, Drawable> mBackDrawAbles = new HashMap<Integer, Drawable>();
    private int clusterRadius = 50;
    private ClusterOverlay clusterOverlay;

    public static MyMapFragment newInstance(String name, Operator operator) {

        Bundle args = new Bundle();

        MyMapFragment fragment = new MyMapFragment();
        fragment.setArguments(args);
        fragment.setName(name);
        fragment.setOperator(operator);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (mapLayout == null) {
            mapLayout = layoutInflater.inflate(R.layout.basemap_support_fragment_activity, null);
            mapView = mapLayout.findViewById(R.id.map);
            mapView.onCreate(bundle);
            final ProgressBar progressBar = mapLayout.findViewById(R.id.progressBar);
            if (aMap == null) {
                aMap = mapView.getMap();
                aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
                    @Override
                    public void onMapLoaded() {
                        progressBar.setVisibility(View.GONE);
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
        aMap.setOnCameraChangeListener(this);

        geocodeSearch = new GeocodeSearch(getContext());
        geocodeSearch.setOnGeocodeSearchListener(this);

        new Thread() {
            @Override
            public void run() {
                images = ImageService.getImagesWithLoc();
                List<ClusterItem> items = new ArrayList<>();
                Image image1 = images.get(0);
                for (int i = 0; i < 100; i++) {
                    Image image = new Image();
                    image.setPath(image1.getPath());
                    // rectify coordinate
                    LatLng latLng = new LatLng(Double.parseDouble(image1.getLat()) + Math.random(), Double.parseDouble(image1.getLng()) + Math.random());
                    //latLng = MapUtils.convert2AMapCoord(getContext(), latLng);

                    image.setPos(latLng);
                    items.add(image);
                }

                clusterOverlay = new ClusterOverlay(aMap, items, dp2px(getActivity(), clusterRadius), getActivity());
                clusterOverlay.setClusterRenderer(MyMapFragment.this);
                clusterOverlay.setOnClusterClickListener(MyMapFragment.this);

                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(items.get(0).getPosition(), 19));
            }
        }.start();
    }

    private Marker addImageMarker(String path, LatLng latLng) {
        // mark
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Hello");
        markerOptions.visible(true);

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(ImageUtils.loadBitmap(path, markerHeight, markerWidth));
        markerOptions.icon(bitmapDescriptor);
        return aMap.addMarker(markerOptions);
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
    public void setName(String s) {
        this.name = s;
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
    public Drawable getDrawAble(int clusterNum, ClusterItem clusterItem) {
        int radius = dp2px(getActivity(), 80);
        return new BitmapDrawable(null, ImageUtils.loadBitmap(((Image) clusterItem).getPath(), 60, 60));
        /*
        if (clusterNum == 1) {
            Drawable bitmapDrawable = mBackDrawAbles.get(1);
            if (bitmapDrawable == null) {
                bitmapDrawable =
                        getActivity().getResources().getDrawable(
                                R.drawable.icon_openmap_mark);
                mBackDrawAbles.put(1, bitmapDrawable);
            }

            return bitmapDrawable;
        } else if (clusterNum < 5) {
            Drawable bitmapDrawable = mBackDrawAbles.get(2);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius,
                        Color.argb(159, 210, 154, 6)));
                mBackDrawAbles.put(2, bitmapDrawable);
            }

            return bitmapDrawable;
        } else if (clusterNum < 10) {
            Drawable bitmapDrawable = mBackDrawAbles.get(3);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius,
                        Color.argb(199, 217, 114, 0)));
                mBackDrawAbles.put(3, bitmapDrawable);
            }

            return bitmapDrawable;
        } else {
            Drawable bitmapDrawable = mBackDrawAbles.get(4);
            if (bitmapDrawable == null) {
                bitmapDrawable = new BitmapDrawable(null, drawCircle(radius,
                        Color.argb(235, 215, 66, 2)));
                mBackDrawAbles.put(4, bitmapDrawable);
            }

            return bitmapDrawable;
        }
        */
    }

    private Bitmap drawImageWithNum(int width, int height, String path) {
        Bitmap bitmap = ImageUtils.loadBitmap(path, width, height);
        Canvas canvas = new Canvas(bitmap);
        return null;
    }

    private Bitmap drawCircle(int radius, int color) {

        Bitmap bitmap = Bitmap.createBitmap(radius * 2, radius * 2,
                Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        RectF rectF = new RectF(0, 0, radius * 2, radius * 2);
        paint.setColor(color);
        canvas.drawArc(rectF, 0, 360, true, paint);
        return bitmap;
    }

    @Override
    public void onClick(Marker marker, List<ClusterItem> clusterItems) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (ClusterItem clusterItem : clusterItems) {
            builder.include(clusterItem.getPosition());
        }
        LatLngBounds latLngBounds = builder.build();
        aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 0));
    }

}
