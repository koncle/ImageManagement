package com.koncle.imagemanagement.util;

/**
 * Created by Koncle on 2018/3/21.
 */

public class Functions {
    /*
    *
    * [0, 1] -> [0, 1]
    * */
    public static float sinx(float x) {
        return (float) ((Math.sin((x - 0.5f) * Math.PI) + 1) / 2);
    }

    /*
    *
    * (-∞, +∞) -> [0, 1]
    * */
    public static float sigmoid(float x) {
        return (float) (1 / (1 + Math.pow(Math.E, -x)));
    }

    public static float parabola(float a, float b, float c, float x) {
        return a * x * x + b * x + c;
    }
}
