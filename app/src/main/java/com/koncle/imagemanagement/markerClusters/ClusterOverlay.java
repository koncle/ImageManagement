package com.koncle.imagemanagement.markerClusters;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.AlphaAnimation;
import com.amap.api.maps.model.animation.Animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by yiyi.qi on 16/10/10.
 * 整体设计采用了两个线程,一个线程用于计算组织聚合数据,一个线程负责处理Marker相关操作
 */
public class ClusterOverlay implements AMap.OnCameraChangeListener,
        AMap.OnMarkerClickListener {
    private AMap mAMap;
    private Context mContext;
    private List<ClusterItem> mClusterItems;
    private Map<ClusterItem, Cluster> clusterItemClusterMap;

    private List<Cluster> mClusters;
    private int mClusterSize;
    private ClusterClickListener mClusterClickListener;
    private ClusterRender mClusterRender;
    private List<Marker> mAddMarkers = new ArrayList<Marker>();
    private double mClusterDistance;
    private HandlerThread mMarkerHandlerThread = new HandlerThread("addMarker");
    private HandlerThread mSignClusterThread = new HandlerThread("calculateCluster");
    private Handler mMarkerhandler;
    private Handler mSignClusterHandler;
    private float mPXInMeters;
    private boolean mIsCanceled = false;

    private float zoomLevel;
    private float senseLevel = 0.1f;
    private AlphaAnimation mADDAnimation = new AlphaAnimation(0, 1);

    /**
     * 构造函数
     *
     * @param amap
     * @param clusterSize 聚合范围的大小（指点像素单位距离内的点会聚合到一个点显示）
     * @param context
     */
    public ClusterOverlay(AMap amap, int clusterSize, Context context) {
        this(amap, null, clusterSize, context);
    }

    /**
     * 构造函数,批量添加聚合元素时,调用此构造函数
     *
     * @param amap
     * @param clusterItems 聚合元素
     * @param clusterSize
     * @param context
     */
    public ClusterOverlay(AMap amap, List<ClusterItem> clusterItems,
                          int clusterSize, Context context) {
        //默认最多会缓存80张图片作为聚合显示元素图片,根据自己显示需求和app使用内存情况,可以修改数量

        /*
        mLruCache = new LruCache<Integer, BitmapDescriptor>(100) {
            protected void entryRemoved(boolean evicted, Integer key, BitmapDescriptor oldValue, BitmapDescriptor newValue) {
                oldValue.getBitmap().recycle();
            }
        };
        */

        if (clusterItems != null) {
            mClusterItems = clusterItems;
        } else {
            mClusterItems = new ArrayList<ClusterItem>();
        }
        clusterItemClusterMap = new HashMap<>();

        mContext = context;
        mClusters = new ArrayList<Cluster>();
        this.mAMap = amap;
        mClusterSize = clusterSize;
        mPXInMeters = mAMap.getScalePerPixel();
        mClusterDistance = mPXInMeters * mClusterSize;
        amap.setOnCameraChangeListener(this);
        amap.setOnMarkerClickListener(this);
        initThreadHandler();
        assignClusters();
    }

    /**
     * 设置聚合点的点击事件
     *
     * @param clusterClickListener
     */
    public void setOnClusterClickListener(
            ClusterClickListener clusterClickListener) {
        mClusterClickListener = clusterClickListener;
    }

    /**
     * 添加一个聚合点
     *
     * @param item
     */
    public void addClusterItem(ClusterItem item) {
        Message message = Message.obtain();
        message.what = SignClusterHandler.CALCULATE_SINGLE_CLUSTER;
        message.obj = item;
        mSignClusterHandler.sendMessage(message);
    }

    public void removeClusterItem(ClusterItem item) {
        Message message = Message.obtain();
        message.what = SignClusterHandler.DELETE_CLUSTER_ITEM;
        message.obj = item;
        mSignClusterHandler.sendMessage(message);
    }

    public void deleteClusterItem(ClusterItem item) {
        for (Cluster cluster : mClusters) {
            int index = cluster.getClusterItems().indexOf(item);
            if (index != -1) {
                cluster.getClusterItems().remove(index);
                // delete the cluster which contains only one image
                if (cluster.getClusterItemCount() == 0) {
                    Marker marker = cluster.getMarker();
                    marker.remove();
                    mAddMarkers.remove(marker);
                } else {
                    // change visible image
                    if (index == 0) {
                        updateCluster(cluster);
                    }
                }
            }
        }
    }

    /**
     * 设置聚合元素的渲染样式，不设置则默认为气泡加数字形式进行渲染
     *
     * @param render
     */
    public void setClusterRenderer(ClusterRender render) {
        mClusterRender = render;
    }

    public void onDestroy() {
        mIsCanceled = true;
        mSignClusterHandler.removeCallbacksAndMessages(null);
        mMarkerhandler.removeCallbacksAndMessages(null);
        mSignClusterThread.quit();
        mMarkerHandlerThread.quit();
        for (Marker marker : mAddMarkers) {
            marker.remove();
        }
        mAddMarkers.clear();
    }

    //初始化Handler
    private void initThreadHandler() {
        mMarkerHandlerThread.start();
        mSignClusterThread.start();
        mMarkerhandler = new MarkerHandler(mMarkerHandlerThread.getLooper());
        mSignClusterHandler = new SignClusterHandler(mSignClusterThread.getLooper());
    }

    @Override
    public void onCameraChange(CameraPosition arg0) {
    }

    @Override
    public void onCameraChangeFinish(CameraPosition arg0) {
        if (Math.abs(arg0.zoom - zoomLevel) > senseLevel) {
            mPXInMeters = mAMap.getScalePerPixel();
            mClusterDistance = mPXInMeters * mClusterSize;
            zoomLevel = arg0.zoom;
            assignClusters();
        }
        Log.w("ZOOM LEVEL", "" + arg0.zoom);
    }

    //点击事件
    @Override
    public boolean onMarkerClick(Marker arg0) {
        if (mClusterClickListener == null) {
            return true;
        }
        Cluster cluster = (Cluster) arg0.getObject();
        if (cluster != null) {
            mClusterClickListener.onClick(arg0, cluster.getClusterItems());
            return true;
        }
        return false;
    }

    /**
     * 将聚合元素添加至地图上
     */
    private void addClusterToMap(List<Cluster> clusters) {

        ArrayList<Marker> removeMarkers = new ArrayList<>();
        removeMarkers.addAll(mAddMarkers);

        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        MyAnimationListener myAnimationListener = new MyAnimationListener(removeMarkers);
        for (Marker marker : removeMarkers) {
            marker.setAnimation(alphaAnimation);
            marker.setAnimationListener(myAnimationListener);
            marker.startAnimation();
        }

        for (Marker marker : removeMarkers) {
            marker.remove();
        }

        removeMarkers.clear();

        for (Cluster cluster : clusters) {
            // show
            addSingleClusterToMap(cluster);

            // put into map
            for (ClusterItem item : cluster.getClusterItems()) {
                clusterItemClusterMap.put(item, cluster);
            }
        }
    }

    /**
     * 将单个聚合元素添加至地图显示
     *
     * @param cluster
     */
    private void addSingleClusterToMap(Cluster cluster) {
        LatLng latlng = cluster.getCenterLatLng();
        MarkerOptions markerOptions = new MarkerOptions();
        BitmapDescriptor descriptor = null;
        for (ClusterItem clusterItem : cluster.getClusterItems()) {
            descriptor = getBitmapDes(cluster.getClusterItemCount(), clusterItem);
            if (descriptor != null) break;
        }

        if (descriptor != null) {
            markerOptions.anchor(0.5f, 0.5f)
                    .icon(descriptor)
                    .position(latlng);
            Marker marker = mAMap.addMarker(markerOptions);
            marker.setAnimation(mADDAnimation);
            marker.setObject(cluster);

            marker.startAnimation();
            cluster.setMarker(marker);
            mAddMarkers.add(marker);
        } else {
            Log.e("add marker", "cluster's images are invalid");
        }
    }

    private void calculateClusters() {
        mIsCanceled = false;
        mClusters.clear();
        //LatLngBounds visibleBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
        for (ClusterItem clusterItem : mClusterItems) {
            // cancel the calculation
            if (mIsCanceled) {
                return;
            }
            // get position to calculate clusters nearby
            LatLng latlng = clusterItem.getPosition();
            // if the pic is visible

            //if (visibleBounds.contains(latlng)) {

            // get nearby cluster
                Cluster cluster = getCluster(latlng, mClusters);
            // if there is no cluster
            // create one
            if (cluster == null) {
                    cluster = new Cluster(latlng);
                    mClusters.add(cluster);
            }
            // put the pic into a nearest cluster
            cluster.addClusterItem(clusterItem);

            //}

        }
        // end calculation

        //复制一份数据，规避同步
        // ?? what does it mean?
        List<Cluster> clusters = new ArrayList<Cluster>();
        clusters.addAll(mClusters);
        Message message = Message.obtain();
        message.what = MarkerHandler.ADD_CLUSTER_LIST;
        message.obj = clusters;
        // if a new request is coming
        // then cancel this action
        if (mIsCanceled) {
            return;
        }
        // send msg to put markers
        mMarkerhandler.sendMessage(message);
    }

    /**
     * 对点进行聚合
     */
    private void assignClusters() {
        mIsCanceled = true;
        mSignClusterHandler.removeMessages(SignClusterHandler.CALCULATE_CLUSTER);
        mSignClusterHandler.sendEmptyMessage(SignClusterHandler.CALCULATE_CLUSTER);
    }

    private void moved() {
        LatLngBounds visibleBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
        List<Cluster> clusters = new ArrayList<>();
        clusters.addAll(mClusters);

        List<Cluster> newClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            if (mIsCanceled) {
                return;
            }
            LatLng latLng = cluster.getCenterLatLng();
            if (visibleBounds.contains(latLng)) {
                // can bse reused
                cluster.setNew(false);
            } else {
                // not visible, need to be deleted
                cluster.setNeedDelete(true);
            }
            newClusters.add(cluster);
        }
        // travel all items
        for (ClusterItem clusterItem : mClusterItems) {
            // item that is not shown but is visible
            if (clusterItemClusterMap.get(clusterItem) == null && visibleBounds.contains(clusterItem.getPosition())) {
                // add to new cluster
                Cluster cluster = getCluster(clusterItem.getPosition(), newClusters);
                if (cluster == null) {
                    cluster = new Cluster(clusterItem.getPosition());
                    cluster.setNew(true);
                    cluster.setNumChanged(false);
                    newClusters.add(cluster);
                } else {
                    if (!cluster.isNew()) {
                        cluster.setNumChanged(true);
                    }
                }
                // add the item to cluster
                cluster.addClusterItem(clusterItem);
            }
        }
        Message msg = Message.obtain();
        msg.what = MarkerHandler.CHANGE_CLUSTER_LIST;
        msg.obj = newClusters;
        mMarkerhandler.sendMessage(msg);
    }

    void zoomIn() {
    }

    void zoomOut() {

    }

    /**
     * 在已有的聚合基础上，对添加的单个元素进行聚合
     *
     * @param clusterItem
     */
    private void calculateSingleCluster(ClusterItem clusterItem) {
        LatLngBounds visibleBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng latlng = clusterItem.getPosition();
        if (!visibleBounds.contains(latlng)) {
            return;
        }
        Cluster cluster = getCluster(latlng, mClusters);
        if (cluster != null) {
            cluster.addClusterItem(clusterItem);
            Message message = Message.obtain();
            message.what = MarkerHandler.UPDATE_SINGLE_CLUSTER;

            message.obj = cluster;
            mMarkerhandler.removeMessages(MarkerHandler.UPDATE_SINGLE_CLUSTER);
            mMarkerhandler.sendMessageDelayed(message, 5);


        } else {

            cluster = new Cluster(latlng);
            mClusters.add(cluster);
            cluster.addClusterItem(clusterItem);
            Message message = Message.obtain();
            message.what = MarkerHandler.ADD_SINGLE_CLUSTER;
            message.obj = cluster;
            mMarkerhandler.sendMessage(message);

        }
    }

    /**
     * 根据一个点获取是否可以依附的聚合点，没有则返回null
     *
     * @param latLng
     * @return
     */
    private Cluster getCluster(LatLng latLng, List<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            LatLng clusterCenterPoint = cluster.getCenterLatLng();
            double distance = AMapUtils.calculateLineDistance(latLng, clusterCenterPoint);
            if (distance < mClusterDistance) {/* && mAMap.getCameraPosition().zoom < 19) {*/
                return cluster;
            }
        }

        return null;
    }


    /**
     * 获取每个聚合点的绘制样式
     */
    private BitmapDescriptor getBitmapDes(int num, ClusterItem clusterItem) {

        BitmapDescriptor bitmapDescriptor;
        /*
        View view = mViewLruCache.get(((Image) clusterItem).getId().intValue());
        view = LayoutInflater.from(mContext).inflate(R.layout.map_item_layout, null, true);
        ImageView imageView = view.findViewById(R.id.map_image);
        imageView.setImageDrawable(mClusterRender.getDrawAble(num, clusterItem));
        mViewLruCache.put(((Image) clusterItem).getId().intValue(), view);
        TextView textView = view.findViewById(R.id.map_text);
        textView.setText(String.valueOf(num));
         */
        long start = System.currentTimeMillis();
        View view = mClusterRender.getDrawAble(num, clusterItem);
        if (view == null) return null;

        bitmapDescriptor = BitmapDescriptorFactory.fromView(view);
        Log.w("draw", "time : " + (System.currentTimeMillis() - start));
        return bitmapDescriptor;
    }

    /**
     * 更新已加入地图聚合点的样式
     */
    private void updateCluster(Cluster cluster) {
        Marker marker = cluster.getMarker();
        marker.setIcon(getBitmapDes(cluster.getClusterItemCount(), cluster.getClusterItems().get(0)));
    }


//-----------------------辅助内部类用---------------------------------------------

    /**
     * marker渐变动画，动画结束后将Marker删除
     */
    class MyAnimationListener implements Animation.AnimationListener {
        private List<Marker> mRemoveMarkers;

        MyAnimationListener(List<Marker> removeMarkers) {
            mRemoveMarkers = removeMarkers;
        }

        @Override
        public void onAnimationStart() {

        }

        @Override
        public void onAnimationEnd() {
            for (Marker marker : mRemoveMarkers) {
                marker.remove();
            }
            mRemoveMarkers.clear();
        }
    }

    /**
     * 处理market添加，更新等操作
     */
    class MarkerHandler extends Handler {

        public static final int CHANGE_CLUSTER_LIST = 3;
        static final int ADD_CLUSTER_LIST = 0;
        static final int ADD_SINGLE_CLUSTER = 1;
        static final int UPDATE_SINGLE_CLUSTER = 2;

        MarkerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            List<Cluster> clusters;
            switch (message.what) {
                case ADD_CLUSTER_LIST:
                    clusters = (List<Cluster>) message.obj;
                    addClusterToMap(clusters);
                    break;
                case ADD_SINGLE_CLUSTER:
                    Cluster cluster = (Cluster) message.obj;
                    addSingleClusterToMap(cluster);
                    break;
                case UPDATE_SINGLE_CLUSTER:
                    Cluster updateCluster = (Cluster) message.obj;
                    updateCluster(updateCluster);
                    break;
                case CHANGE_CLUSTER_LIST:
                    clusters = (List<Cluster>) message.obj;

            }
        }
    }

    /**
     * 处理聚合点算法线程
     */
    class SignClusterHandler extends Handler {
        public static final int DELETE_CLUSTER_ITEM = 5;
        static final int CALCULATE_CLUSTER = 0;
        static final int CALCULATE_SINGLE_CLUSTER = 1;
        static final int ZOOM_OUT = 2;
        static final int ZOOM_IN = 3;
        static final int MOVE = 4;

        SignClusterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            ClusterItem item;
            switch (message.what) {
                case CALCULATE_CLUSTER:
                    calculateClusters();
                    break;
                case CALCULATE_SINGLE_CLUSTER:
                    item = (ClusterItem) message.obj;
                    mClusterItems.add(item);
                    Log.i("yiyi.qi", "calculate single cluster");
                    calculateSingleCluster(item);
                    break;
                case DELETE_CLUSTER_ITEM:
                    item = (ClusterItem) message.obj;
                    mClusterItems.remove(item);
                    deleteClusterItem(item);
                case ZOOM_OUT:
                    break;
                case ZOOM_IN:
                    break;
                case MOVE:
                    break;
            }
        }
    }
}