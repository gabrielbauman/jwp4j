package com.gabrielbauman.jwp4j;

interface TypeUtils {

    static Byte asByte(Object object) {
        if (null == object) return null;
        if (object instanceof Number) {
            if (object instanceof Byte) return (Byte) object;
            if (object instanceof Integer) return ((Integer) object).byteValue();
            if (object instanceof Long) return ((Long) object).byteValue();
            if (object instanceof Float) return ((Float) object).byteValue();
            if (object instanceof Double) return ((Double) object).byteValue();
        }
        if (object instanceof String) return Byte.parseByte((String) object);
        throw new IllegalArgumentException();
    }

    static Integer asInteger(Object object) {
        if (null == object) return null;
        if (object instanceof Number) {
            if (object instanceof Integer) return (Integer) object;
            if (object instanceof Long) return ((Long) object).intValue();
            if (object instanceof Float) return ((Float) object).intValue();
            if (object instanceof Double) return ((Double) object).intValue();
            if (object instanceof Byte) return ((Byte) object).intValue();
        }
        if (object instanceof String) return Integer.parseInt((String) object);
        throw new IllegalArgumentException();
    }

    static Long asLong(Object object) {
        if (null == object) return null;
        if (object instanceof Number) {
            if (object instanceof Long) return (Long) object;
            if (object instanceof Integer) return ((Integer) object).longValue();
            if (object instanceof Double) return ((Double) object).longValue();
            if (object instanceof Float) return ((Float) object).longValue();
            if (object instanceof Byte) return ((Byte) object).longValue();
        }
        if (object instanceof String) return Long.parseLong((String) object);
        throw new IllegalArgumentException();
    }

    static Double asDouble(Object object) {
        if (null == object) return null;
        if (object instanceof Number) {
            if (object instanceof Double) return (Double) object;
            if (object instanceof Float) return ((Float) object).doubleValue();
            if (object instanceof Integer) return ((Integer) object).doubleValue();
            if (object instanceof Long) return ((Long) object).doubleValue();
            if (object instanceof Byte) return ((Byte) object).doubleValue();
        }
        if (object instanceof String) return Double.parseDouble((String) object);
        throw new IllegalArgumentException();
    }

    static Float asFloat(Object object) {
        if (null == object) return null;
        if (object instanceof Number) {
            if (object instanceof Float) return (Float) object;
            if (object instanceof Double) return ((Double) object).floatValue();
            if (object instanceof Integer) return ((Integer) object).floatValue();
            if (object instanceof Long) return ((Long) object).floatValue();
            if (object instanceof Byte) return ((Byte) object).floatValue();
        }
        if (object instanceof String) return Float.parseFloat((String) object);
        throw new IllegalArgumentException();
    }

    static String asString(Object object) {
        if (null == object) return null;
        return object.toString();
    }

    static boolean asBoolean(Object object) {
        if (null == object) return false;
        if (object instanceof Boolean) return (boolean) object;
        if (object instanceof Number) {
            if (object instanceof Long) return ((Long) object) > 0;
            if (object instanceof Integer) return ((Integer) object) > 0;
            if (object instanceof Double) return ((Double) object) > 0;
            if (object instanceof Float) return ((Float) object) > 0;
            if (object instanceof Byte) return ((Byte) object) > 0;
        }
        return object instanceof String && ((String) object).equalsIgnoreCase("true");
    }

}
