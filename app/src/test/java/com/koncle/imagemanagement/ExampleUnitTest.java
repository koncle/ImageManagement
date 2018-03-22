package com.koncle.imagemanagement;

import com.koncle.imagemanagement.util.Functions;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() throws Exception {
        float res = Functions.sigmoid((float) Math.pow(100, 100));
        float res2 = Functions.sigmoid((float) -Math.pow(100, 100));
        float res3 = Functions.sigmoid((float) 0.5);
        System.out.println("res : " + res + " res2 " + res2 + " res3 " + res3);
    }
}