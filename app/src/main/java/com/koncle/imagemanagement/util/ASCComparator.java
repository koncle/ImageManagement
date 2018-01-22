package com.koncle.imagemanagement.util;

import com.koncle.imagemanagement.bean.Image;

import java.util.Comparator;

/**
 * Created by Koncle on 2018/1/20.
 */

public class ASCComparator implements Comparator<Image> {

    @Override
    public int compare(Image o1, Image o2) {
        return o1.compareTo(o2);
    }
}
