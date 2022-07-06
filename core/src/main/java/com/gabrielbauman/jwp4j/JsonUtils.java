package com.gabrielbauman.jwp4j;

import org.json.simple.JSONValue;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface JsonUtils {

    @SuppressWarnings({"unchecked", "rawtypes"})
    static Map<String, Object> deserialize(String jsonString) {

        try {
            JSONParser parser = new JSONParser();
            Object parsed = parser.parse(jsonString, new ContainerFactory() {
                public List<?> creatArrayContainer() {
                    return new ArrayList<>();
                }

                public Map<String, Object> createObjectContainer() {
                    return new UniqueKeyLinkedHashMap();
                }
            });
            if (parsed == null) {
                throw new IllegalStateException("Parsing returned null");
            }
            return (Map) parsed;
        } catch (IllegalArgumentException | ParseException e) {
            throw new IllegalStateException("Parsing error: " + e, e);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Expecting a JSON object at the root but " + e, e);
        }
    }

    static String serialize(Map<String, ?> map) {
        return JSONValue.toJSONString(map);
    }

    static Object jsonValueToJavaType(String jsonValue) {
        if (null == jsonValue || jsonValue.equals("null")) return null;
        if (jsonValue.equals("true")) return true;
        if (jsonValue.equals("false")) return false;
        if (jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) return jsonValue.substring(1, jsonValue.length() - 1);
        if (jsonValue.matches("^-?\\d+$")) {
            long v = Long.parseLong(jsonValue);
            if (v < Integer.MAX_VALUE && v > Integer.MIN_VALUE) return (int) v;
            return v;
        }
        if (jsonValue.matches("^-?[\\d.]+$")) {
            double v = Double.parseDouble(jsonValue);
            if (v < Float.MAX_VALUE && v > Float.MIN_VALUE) return (int) v;
            return v;
        }
        return jsonValue;
    }

    static String javaTypeToJsonValue(Object value) {
        if (null == value) return "";
        if (value instanceof String) return String.format("\"%s\"", value);
        return value.toString();
    }

    private static <T> T getGeneric(final Map<String, Object> o, final String key, @SuppressWarnings("SameParameterValue") final Class<T> clazz) {

        if (o.get(key) == null) {
            return null;
        }

        Object value = o.get(key);

        if (!clazz.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Unexpected type of JSON object member with key " + key);
        }

        @SuppressWarnings("unchecked")
        T castValue = (T) value;
        return castValue;
    }

    static Map<String, Object> getJSONObject(final Map<String, Object> o, final String key) {

        @SuppressWarnings("unchecked")
        Map<String, Object> result = getGeneric(o, key, Map.class);

        if (result == null) {
            return null;
        }

        for (Object oKey : result.keySet()) {
            if (!(oKey instanceof String)) {
                throw new IllegalArgumentException("Value at " + key + " is not a JSON object");
            }
        }

        return result;
    }

    class UniqueKeyLinkedHashMap extends LinkedHashMap<String, Object> {
        @Override
        public Object put(String key, Object value) {
            if (this.containsKey(key)) {
                throw new IllegalArgumentException("An entry for '" + key + "' already exists. Keys must be unique.");
            }
            return super.put(key, value);
        }
    }


}
