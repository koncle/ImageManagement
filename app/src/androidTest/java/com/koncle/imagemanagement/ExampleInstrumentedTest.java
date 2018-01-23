package com.koncle.imagemanagement;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.koncle.imagemanagement.dataManagement.ImageService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static String TAG = "test database";
    public static Context context;

    @BeforeClass
    public static void init() {
        context = InstrumentationRegistry.getTargetContext();
        ImageService.init(context);
    }

    @Test
    public void testFile() throws Exception {

    }

    @AfterClass
    public static void destroy() {
        ImageService.close();
    }
}
