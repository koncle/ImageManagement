package com.koncle.imagemanagement.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
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
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.ImageUtils;
import com.koncle.imagemanagement.util.MapUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/15.
 */

public class MapActivity extends AppCompatActivity implements GeocodeSearch.OnGeocodeSearchListener, AMap.OnCameraChangeListener {
    MapView mapView;
    AMap aMap;
    List<Image> images;
    private int markerHeight = 100;
    private int markerWidth = 100;
    private GeocodeSearch geocodeSearch;
    List<Marker> markers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);

        images = getIntent().getExtras().getParcelableArrayList(ActivityUtil.ACTIVITY_IMAGE_TAG);

        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 此方法必须重写
        aMap = mapView.getMap();

        aMap.setOnCameraChangeListener(this);

        geocodeSearch = new GeocodeSearch(this);
        geocodeSearch.setOnGeocodeSearchListener(this);

        for (int i = 0; i < 3; ++i) {
            Image image = images.get(i);
            // rectify coordinate
            LatLng latLng = new LatLng(Double.parseDouble(image.getLat()) - i * 0.001, Double.parseDouble(image.getLng()) - i * 0.001);
            latLng = MapUtils.convert2AMapCoord(this, latLng);

            getAddress(MapUtils.convertToLatLonPoint(latLng));

            // move camera to the position
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

            Marker m = addImageMarker(image.getPath(), latLng);
            markers.add(m);
            if (i % 2 == 1) m.setVisible(false);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
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
                Toast.makeText(this, result.getRegeocodeAddress().getFormatAddress(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No result", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "error ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }
}
