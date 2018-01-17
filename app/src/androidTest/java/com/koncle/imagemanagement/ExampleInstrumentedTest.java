package com.koncle.imagemanagement;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.dataManagement.ImageSource;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
    public void testDatabase() throws Exception {
        ImageService.refreshTables();

        ImageSource.getSystemPhotoList(context);

        List<Image> imageList = ImageService.getAllImages();
        ImageService.addTags(imageList.subList(0, 10), "tag1");
        ImageService.addTags(imageList.subList(10, 20), "tag2");

        List<Tag> tags = ImageService.getTags();
        for (Tag tag : tags) {
            List<Image> images = tag.getImages();
            Log.w(TAG, tag.getId() + " " + images.size());
        }
        ImageService.removeTags(imageList.get(0), tags.get(0));
        imageList = ImageService.getAllImages();
        Assert.assertEquals(imageList.get(0).getTags().size(), 0);
        Assert.assertEquals(tags.size(), 2);
    }

    @AfterClass
    public static void destroy() {
        ImageService.close();
    }
}
