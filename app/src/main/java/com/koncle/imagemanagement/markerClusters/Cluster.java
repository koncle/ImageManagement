package com.koncle.imagemanagement.markerClusters;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yiyi.qi on 16/10/10.
 */

public class Cluster {


    private LatLng mLatLng;
    private List<ClusterItem> mClusterItems;
    private Marker mMarker;

    private List<Cluster> mAddClusters;
    private boolean numChanged;
    private boolean needDelete;
    private boolean isNew;

    Cluster(LatLng latLng) {

        mLatLng = latLng;
        mClusterItems = new ArrayList<ClusterItem>();
        mAddClusters = new ArrayList<>();
    }

    public boolean isNumChanged() {
        return numChanged;
    }

    public void setNumChanged(boolean numChanged) {
        this.numChanged = numChanged;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public boolean isNeedDelete() {
        return needDelete;
    }

    public void setNeedDelete(boolean needDelete) {
        this.needDelete = needDelete;
    }

    void addClustersToBeAdded(Cluster cluster) {
        mAddClusters.add(cluster);
    }

    void clearClustersToBeAdded() {
        mAddClusters.clear();
    }

    void addAddClusters() {
        for (Cluster cluster : mAddClusters) {
            for (ClusterItem item : cluster.getClusterItems()) {
                if (!mClusterItems.contains(item)) {
                    mClusterItems.add(item);
                }
            }
        }
        mAddClusters.clear();
    }

    void addClusterItem(ClusterItem clusterItem) {
        mClusterItems.add(clusterItem);
    }

    int getClusterItemCount() {
        return mClusterItems.size();
    }

    LatLng getCenterLatLng() {
        return mLatLng;
    }

    Marker getMarker() {
        return mMarker;
    }

    void setMarker(Marker marker) {
        mMarker = marker;
    }

    List<ClusterItem> getClusterItems() {
        return mClusterItems;
    }

    public void merge(Cluster cluster) {
        mClusterItems.addAll(cluster.getClusterItems());
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Cluster cluster = new Cluster(mLatLng);
        cluster.mClusterItems = this.mClusterItems;
        cluster.mMarker = this.mMarker;
        return cluster;
    }
}
