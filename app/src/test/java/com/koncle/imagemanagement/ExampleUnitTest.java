package com.koncle.imagemanagement;

import com.koncle.imagemanagement.util.ImageUtils;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        System.out.println(ImageUtils.getFolderPathFromPath("asdfasdf/asdf/asd/fa/sdf/asd/f"));
    }
}