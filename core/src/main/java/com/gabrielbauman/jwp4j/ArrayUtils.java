package com.gabrielbauman.jwp4j;

import java.util.Arrays;

interface ArrayUtils {

    /**
     * A custom split method that preserves empty spaces and doesn't use regular expressions
     */
    static String[] split(String s, char delimiter) {

        String[] result = new String[0];

        int posPrevDelim = -1;
        int posNextDelim = s.indexOf(delimiter);
        int posLastDelim = s.lastIndexOf(delimiter);
        int delimiterCount = 0;

        while (posPrevDelim < posLastDelim) {
            delimiterCount++;
            result = concat(result, s.substring(posPrevDelim + 1, posNextDelim));
            posPrevDelim = posNextDelim;
            posNextDelim = s.indexOf(delimiter, posNextDelim + 1);
        }

        result = concat(result, s.substring(posLastDelim + 1));

        assert result.length == delimiterCount + 1;

        return result;
    }

    static String[] concat(String[] array, String element) {
        String[] result = Arrays.copyOf(array, array.length + 1);
        result[result.length - 1] = element;
        return result;
    }

    static <T> boolean contains(T[] haystack, T needle) {
        for (T value: haystack) {
            if (value == needle) return true;
        }
        return false;
    }

}
