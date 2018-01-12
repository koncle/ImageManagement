package com.koncle.imagemanagement.dataManagement;

import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;

import com.koncle.imagemanagement.util.TagUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by 10976 on 2018/1/11.
 */

public class ImageAttribute {
    private static final String TAG = ImageAttribute.class.getSimpleName();
    private static boolean DEBUG = false;
    /*
    TAG_APERTURE：光圈值。
    TAG_DATETIME：拍摄时间，取决于设备设置的时间。
    TAG_EXPOSURE_TIME：曝光时间。
    TAG_FLASH：闪光灯。
    TAG_FOCAL_LENGTH：焦距。
    TAG_IMAGE_LENGTH：图片高度。
    TAG_IMAGE_WIDTH：图片宽度。
    TAG_ISO：ISO。
    TAG_MAKE：设备品牌。
    TAG_MODEL：设备型号，整形表示，在ExifInterface中有常量对应表示。
    TAG_ORIENTATION：旋转角度，整形表示，在ExifInterface中有常量对应表示。
    */

    public static String getTime(String imagePath) {
        File file = new File(imagePath);
        try {
            ExifInterface exifInterface = new ExifInterface(imagePath);
            String time1 = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            Log.i("time", " " + time1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * path 为照片的路径
     */
    public static double[] getLocation(String path) {
        if (TextUtils.isEmpty(path.trim())) return null;

        float output1 = 0;
        float output2 = 0;

        try {
            ExifInterface exifInterface = new ExifInterface(path);
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if (latValue != null && latRef != null && lngValue != null && lngRef != null) {
                try {
                    output1 = convertRationalLatLonToFloat(latValue, latRef);
                    output2 = convertRationalLatLonToFloat(lngValue, lngRef);
                } catch (IllegalArgumentException e) {
                    if (DEBUG) {
                        Log.i(TAG, "can't convert location : " + path);
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            if (DEBUG) {
                Log.i(TAG, "can't resulve attribute from : " + path);
            }
            return null;
        }

        // String context = Context.LOCATION_SERVICE;
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(output1);
        location.setLongitude(output2);
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double[] f = {lat, lng};
        if (TagUtil.DEBUG) {
            Log.i("loc", lat + " : " + lng);
        }
        return f;
    }

    private static float convertRationalLatLonToFloat(
            String rationalString, String ref) {
        try {
            String[] parts = rationalString.split(",");

            String[] pair;
            pair = parts[0].split("/");
            double degrees = Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim());

            pair = parts[1].split("/");
            double minutes = Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim());

            pair = parts[2].split("/");
            double seconds = Double.parseDouble(pair[0].trim())
                    / Double.parseDouble(pair[1].trim());

            double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
            if ((ref.equals("S") || ref.equals("W"))) {
                return (float) -result;
            }
            return (float) result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
    }
}
