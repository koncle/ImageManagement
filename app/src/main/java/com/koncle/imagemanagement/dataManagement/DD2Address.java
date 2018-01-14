package com.koncle.imagemanagement.dataManagement;

import android.util.Log;

import com.koncle.imagemanagement.bean.Address;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by 10976 on 2018/1/11.
 */

public class DD2Address {
    public String[] adjustLatAndLng(double[] d) {
        //接受从网页换回的xml文件
        String str1 = "";
        double lat1 = d[0];
        double lng1 = d[1];

        //經度
        String Lng = null;
        //維度
        String Lat = null;
        BufferedReader d1;
        URL url;

        //用未纠偏的经纬度访问纠偏网站
        String searchServiceURL = "http://api.zdoz.net/transgpsbd.aspx?lat=" + lat1 + "&lng=" + lng1 + "";
        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;

        try {
            url = new URL(searchServiceURL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Content-Type", "text/plain");
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(false);
            int status = httpConnection.getResponseCode();
            if (status == 200) {
                inputStream = httpConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                //得到偏移后的经纬度
                String str = bufferedReader.readLine();
                String[] s = str.split(",");
                String temp[] = s[0].split(":");
                Lng = temp[1];
                String temp1[] = s[1].split(":");
                Lat = temp1[1].substring(0, temp1[1].length() - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                //关闭资源
                httpConnection.disconnect();
                bufferedReader.close();
                inputStream.close();
                return new String[]{Lng, Lat};
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    //d数组  经纬度信息
    public String GetAddress(double[] d) {
        String[] tmp = adjustLatAndLng(d);
        //經度
        String Lng = tmp[0];
        //維度
        String Lat = tmp[1];

        BufferedReader d1;
        String searchServiceURL;
        URL url;
        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        Address address;
        String str1 = "";

        //用纠偏后经纬度重新访问百度API
        searchServiceURL = "http://api.map.baidu.com/geocoder/v2/?ak=XXXXXXXXXXXXXXXXXXX&callback=renderReverse&location=" + Lat + "," + Lng + "&output=xml&pois=1";
        Log.i("TEST", searchServiceURL);
        try {
            url = new URL(searchServiceURL);
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestProperty("Content-Type", "text/plain");
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(false);
            int status = httpConnection.getResponseCode();
            if (status == 200) {
                InputStream in = httpConnection.getInputStream();
                d1 = new BufferedReader(new InputStreamReader(in));
                String str;
                while ((str = d1.readLine()) != null) {
                    str1 = str1 + str;
                }
            }

            //解析读取到的字符串
            SAXReader sax = new SAXReader();
            Document document = (Document) sax.read(new ByteArrayInputStream(str1.getBytes("UTF-8")));
            //获得根元素
            Element root = document.getRootElement();
            Element root1 = root.element("status");
            /** status
             *  0   正常
             1   服务器内部错误
             2   请求参数非法
             3   权限校验失败
             4   配额校验失败
             5   ak不存在或者非法
             101 服务禁用
             102 不通过白名单或者安全码不对
             2xx 无权限
             3xx 配额错误
             */
            //当status=0时，做处理，并将返回的结果写入数据库中
            if (root1.getText().equals("0")) {
                address = new Address();
                Element root2 = root.element("result");

                Element root3 = root2.element("formatted_address");
                address.setFormatted_address(root3.getText());
                Element root4 = root2.element("addressComponent");

                Element root5 = root4.element("streetNumber");
                address.setStreetnumber(root5.getText());

                root5 = root4.element("street");
                address.setStreet(root5.getText());

                root5 = root4.element("district");
                address.setDistrict(root5.getText());

                root5 = root4.element("city");
                address.setCity(root5.getText());

                root5 = root4.element("province");
                address.setProvince(root5.getText());

                root5 = root4.element("country");
                address.setCountry(root5.getText());

                root5 = root4.element("countryCode");
                address.setCountrycode(root5.getText());
                Log.i("TEST", address.toString() + "gergergreg");
                return address.toString();

            } else {
                //当status!=0时，做处理
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpConnection.disconnect();
                bufferedReader.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

