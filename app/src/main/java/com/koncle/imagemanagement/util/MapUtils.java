package com.koncle.imagemanagement.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 10976 on 2018/1/16.
 */

public class MapUtils {
    public static final int ERROR = 1001;// 网络异常
    public static final int ROUTE_START_SEARCH = 2000;
    public static final int ROUTE_END_SEARCH = 2001;
    public static final int ROUTE_BUS_RESULT = 2002;// 路径规划中公交模式
    public static final int ROUTE_DRIVING_RESULT = 2003;// 路径规划中驾车模式
    public static final int ROUTE_WALK_RESULT = 2004;// 路径规划中步行模式
    public static final int ROUTE_NO_RESULT = 2005;// 路径规划没有搜索到结果

    public static final int GEOCODER_RESULT = 3000;// 地理编码或者逆地理编码成功
    public static final int GEOCODER_NO_RESULT = 3001;// 地理编码或者逆地理编码没有数据

    public static final int POISEARCH = 4000;// poi搜索到结果
    public static final int POISEARCH_NO_RESULT = 4001;// poi没有搜索到结果
    public static final int POISEARCH_NEXT = 5000;// poi搜索下一页

    public static final int BUSLINE_LINE_RESULT = 6001;// 公交线路查询
    public static final int BUSLINE_id_RESULT = 6002;// 公交id查询
    public static final int BUSLINE_NO_RESULT = 6003;// 异常情况

    public static void show(Context context, String info) {
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
    }

    public static void show(Context context, int info) {
        Toast.makeText(context, info, Toast.LENGTH_LONG).show();
    }

    public static void showerror(Context context, int rCode) {

        try {
            switch (rCode) {
                //服务错误码
                case 1001:
                    throw new AMapException(AMapException.AMAP_SIGNATURE_ERROR);
                case 1002:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_KEY);
                case 1003:
                    throw new AMapException(AMapException.AMAP_SERVICE_NOT_AVAILBALE);
                case 1004:
                    throw new AMapException(AMapException.AMAP_DAILY_QUERY_OVER_LIMIT);
                case 1005:
                    throw new AMapException(AMapException.AMAP_ACCESS_TOO_FREQUENT);
                case 1006:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_IP);
                case 1007:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_DOMAIN);
                case 1008:
                    throw new AMapException(AMapException.AMAP_INVALID_USER_SCODE);
                case 1009:
                    throw new AMapException(AMapException.AMAP_USERKEY_PLAT_NOMATCH);
                case 1010:
                    throw new AMapException(AMapException.AMAP_IP_QUERY_OVER_LIMIT);
                case 1011:
                    throw new AMapException(AMapException.AMAP_NOT_SUPPORT_HTTPS);
                case 1012:
                    throw new AMapException(AMapException.AMAP_INSUFFICIENT_PRIVILEGES);
                case 1013:
                    throw new AMapException(AMapException.AMAP_USER_KEY_RECYCLED);
                case 1100:
                    throw new AMapException(AMapException.AMAP_ENGINE_RESPONSE_ERROR);
                case 1101:
                    throw new AMapException(AMapException.AMAP_ENGINE_RESPONSE_DATA_ERROR);
                case 1102:
                    throw new AMapException(AMapException.AMAP_ENGINE_CONNECT_TIMEOUT);
                case 1103:
                    throw new AMapException(AMapException.AMAP_ENGINE_RETURN_TIMEOUT);
                case 1200:
                    throw new AMapException(AMapException.AMAP_SERVICE_INVALID_PARAMS);
                case 1201:
                    throw new AMapException(AMapException.AMAP_SERVICE_MISSING_REQUIRED_PARAMS);
                case 1202:
                    throw new AMapException(AMapException.AMAP_SERVICE_ILLEGAL_REQUEST);
                case 1203:
                    throw new AMapException(AMapException.AMAP_SERVICE_UNKNOWN_ERROR);
                    //sdk返回错误
                case 1800:
                    throw new AMapException(AMapException.AMAP_CLIENT_ERRORCODE_MISSSING);
                case 1801:
                    throw new AMapException(AMapException.AMAP_CLIENT_ERROR_PROTOCOL);
                case 1802:
                    throw new AMapException(AMapException.AMAP_CLIENT_SOCKET_TIMEOUT_EXCEPTION);
                case 1803:
                    throw new AMapException(AMapException.AMAP_CLIENT_URL_EXCEPTION);
                case 1804:
                    throw new AMapException(AMapException.AMAP_CLIENT_UNKNOWHOST_EXCEPTION);
                case 1806:
                    throw new AMapException(AMapException.AMAP_CLIENT_NETWORK_EXCEPTION);
                case 1900:
                    throw new AMapException(AMapException.AMAP_CLIENT_UNKNOWN_ERROR);
                case 1901:
                    throw new AMapException(AMapException.AMAP_CLIENT_INVALID_PARAMETER);
                case 1902:
                    throw new AMapException(AMapException.AMAP_CLIENT_IO_EXCEPTION);
                case 1903:
                    throw new AMapException(AMapException.AMAP_CLIENT_NULLPOINT_EXCEPTION);
                    //云图和附近错误码
                case 2000:
                    throw new AMapException(AMapException.AMAP_SERVICE_TABLEID_NOT_EXIST);
                case 2001:
                    throw new AMapException(AMapException.AMAP_ID_NOT_EXIST);
                case 2002:
                    throw new AMapException(AMapException.AMAP_SERVICE_MAINTENANCE);
                case 2003:
                    throw new AMapException(AMapException.AMAP_ENGINE_TABLEID_NOT_EXIST);
                case 2100:
                    throw new AMapException(AMapException.AMAP_NEARBY_INVALID_USERID);
                case 2101:
                    throw new AMapException(AMapException.AMAP_NEARBY_KEY_NOT_BIND);
                case 2200:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOADAUTO_STARTED_ERROR);
                case 2201:
                    throw new AMapException(AMapException.AMAP_CLIENT_USERID_ILLEGAL);
                case 2202:
                    throw new AMapException(AMapException.AMAP_CLIENT_NEARBY_NULL_RESULT);
                case 2203:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOAD_TOO_FREQUENT);
                case 2204:
                    throw new AMapException(AMapException.AMAP_CLIENT_UPLOAD_LOCATION_ERROR);
                    //路径规划
                case 3000:
                    throw new AMapException(AMapException.AMAP_ROUTE_OUT_OF_SERVICE);
                case 3001:
                    throw new AMapException(AMapException.AMAP_ROUTE_NO_ROADS_NEARBY);
                case 3002:
                    throw new AMapException(AMapException.AMAP_ROUTE_FAIL);
                case 3003:
                    throw new AMapException(AMapException.AMAP_OVER_DIRECTION_RANGE);
                    //短传分享
                case 4000:
                    throw new AMapException(AMapException.AMAP_SHARE_LICENSE_IS_EXPIRED);
                case 4001:
                    throw new AMapException(AMapException.AMAP_SHARE_FAILURE);
                default:
                    Toast.makeText(context, "查询失败：" + rCode, Toast.LENGTH_LONG).show();
                    logError("查询失败", rCode);
                    break;
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            logError(e.getMessage(), rCode);
        }
    }

    private static void logError(String info, int errorCode) {
        print(LINE);//start
        print("                                   错误信息                                     ");
        print(LINE);//title
        print(info);
        print("错误码: " + errorCode);
        print("                                                                               ");
        print("如果需要更多信息，请根据错误码到以下地址进行查询");
        print("  http://lbs.amap.com/api/android-sdk/guide/map-tools/error-code/");
        print("如若仍无法解决问题，请将全部log信息提交到工单系统，多谢合作");
        print(LINE);//end
    }

    //log
    public static final String TAG = "AMAP_ERROR";
    static final String LINE_CHAR = "=";
    static final String BOARD_CHAR = "|";
    static final int LENGTH = 80;
    static String LINE;

    static {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            sb.append(LINE_CHAR);
        }
        LINE = sb.toString();
    }


    private static void printLog(String s) {
        if (s.length() < LENGTH - 2) {
            StringBuilder sb = new StringBuilder();
            sb.append(BOARD_CHAR).append(s);
            for (int i = 0; i < LENGTH - 2 - s.length(); i++) {
                sb.append(" ");
            }
            sb.append(BOARD_CHAR);
            print(sb.toString());
        } else {
            String line = s.substring(0, LENGTH - 2);
            print(BOARD_CHAR + line + BOARD_CHAR);
            printLog(s.substring(LENGTH - 2));
        }
    }

    private static void print(String s) {
        Log.i(TAG, s);
    }

    /**
     * Convert location to AMAP type
     */
    public static LatLng convert2AMapCoord(Context context, LatLng sourceLatLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        // CoordType.GPS 待转换坐标类型
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标点
        converter.coord(sourceLatLng);
        // 执行转换操作
        return converter.convert();
    }

    /**
     * 把LatLng对象转化为LatLonPoint对象
     */
    public static LatLonPoint convertToLatLonPoint(LatLng latlon) {
        return new LatLonPoint(latlon.latitude, latlon.longitude);
    }

    /**
     * 把LatLonPoint对象转化为LatLon对象
     */
    public static LatLng convertToLatLng(LatLonPoint latLonPoint) {
        return new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude());
    }

    /**
     * long类型时间格式化
     */
    public static String convertToTime(long time) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(time);
        return df.format(date);
    }

    /**
     * 响应逆地理编码
     */
    public void getAddress(Context context, final LatLonPoint latLonPoint) {
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200,
                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
        GeocodeSearch geocodeSearch = new GeocodeSearch(context);
        geocodeSearch.getFromLocationAsyn(query);// 设置异步逆地理编码请求
    }

    /**
     * 响应逆地理编码的批量请求
     */
    private void getAddresses(Context context, List<LatLonPoint> geopointlist) {
        ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
        final GeocodeSearch geocodeSearch = new GeocodeSearch(context);
        for (final LatLonPoint point : geopointlist) {
            mExecutorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        RegeocodeQuery query = new RegeocodeQuery(point, 200,
                                GeocodeSearch.AMAP);// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
                        RegeocodeAddress result = geocodeSearch.getFromLocation(query);// 设置同步逆地理编码请求
                    } catch (AMapException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
