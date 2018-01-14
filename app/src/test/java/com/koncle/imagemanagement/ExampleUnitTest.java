package com.koncle.imagemanagement;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void test() {
        Pattern p = Pattern.compile("^([/\\w_ -[^\\x00-\\xff]]+)?/([\\w -_[^\\x00-\\xff]]+)/([\\w- _[^\\x00-\\xff]]+)\\.(.*)$");
        Matcher m = p.matcher("/DCIM/P80111-161929.jpg");
        while (m.find()) {
            for (int i = 0; i < m.groupCount(); ++i)
                System.out.println(m.group(i));
        }
    }

    @Test
    public void testString() {
        String path = "/asdf/asdf/asd/f/DCIM/P80111-161929.jpg";
        String[] f = path.split("/");
        String folder = f[f.length - 2];
        String name = f[f.length - 1].split("\\.")[0];
        System.out.println(folder + ":" + name);
    }

    @Test
    public void testSubstring() {
        String path = "/asdf/asdf/asd/f/DCIM/P80111-161929.jpg";
        int i = path.lastIndexOf("/");
        System.out.println(path.substring(0, i));
    }
}