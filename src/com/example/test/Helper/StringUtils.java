package com.example.test.Helper;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by xiashaojun on 16-11-11.
 */

public class StringUtils {
    public static boolean isEmpty(String string) {
        if (string == null || "".equals(string)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNotEmpty(String string) {
        if (string == null || "".equals(string)) {
            return false;
        } else {
            return true;
        }
    }

    public static String[] sortStrings(String[] strings) {
        ArrayList<String> stringList = new ArrayList<String>();
        for (int i = 0; i < strings.length; i++) {
            stringList.add(strings[i]);
        }
        Collections.sort(stringList, Collections.reverseOrder());
        return stringList.toArray(strings);
    }
}
