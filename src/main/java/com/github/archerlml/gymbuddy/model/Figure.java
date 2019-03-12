package com.github.archerlml.gymbuddy.model;


import android.util.LongSparseArray;

import com.annimon.stream.Stream;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by archerlml on 11/2/17.
 */

public class Figure extends UserEntity {

    private Map<String, LongSparseArray<Record>> allFigure = new HashMap<>();

    public enum Type {
        Weight,
        Breast,
        Waist,
        Hip,
        Arm,
        Steps,
        Distance;
        static Map<String, Type> typeMap;

        static Map<String, Type> getTypeMap() {
            if (typeMap == null) {
                typeMap = new HashMap<>();
                for (Type type : Type.values()) {
                    typeMap.put(type.toString().toLowerCase(), type);
                }
            }
            return typeMap;
        }

        @JsonValue
        public String toValue() {
            return toString().toLowerCase();
        }

        @JsonCreator
        public static Type forValue(String type) {
            return getTypeMap().get(type.toLowerCase());
        }
    }

    public Map<String, List<Record>> getFigure() {
        Map<String, List<Record>> figure = new HashMap<>();
        for (Map.Entry<String, LongSparseArray<Record>> entry : allFigure.entrySet()) {
            figure.put(entry.getKey(), mapToList(entry.getValue()));
        }
        return figure;
    }

    public void setFigure(Map<String, List<Record>> figure) {
        allFigure.clear();
        for (Map.Entry<String, List<Record>> entry : figure.entrySet()) {
            allFigure.put(entry.getKey(), listToMap(entry.getValue()));
        }
    }

    public List<Record> figureAsList(Type type) {
        LongSparseArray<Record> sparseArray = allFigure.get(type.toString().toLowerCase());
        List<Record> records = mapToList(sparseArray);
        if (records != null) {
            Collections.sort(records, (r1, r2) -> Long.compare(r1.time, r2.time));
        }
        return records;
    }

    public void setFigure(Type type, LongSparseArray<Record> records) {
        allFigure.put(type.toString().toLowerCase(), records);
    }

    public void setFigure(Type type, List<Record> records) {
        allFigure.put(type.toString().toLowerCase(), listToMap(records));
    }

    public LongSparseArray<Record> figureAsSparseArray(Type type, LongSparseArray<Record> defaultValue) {
        LongSparseArray<Record> value = allFigure.get(type.toString().toLowerCase());
        return value == null ? defaultValue : value;
    }

    public LongSparseArray<Record> figureAsSparseArray(Type type) {
        return figureAsSparseArray(type, null);
    }

    public static List<Record> mapToList(LongSparseArray<Record> sparseArray) {
        if (sparseArray == null) {
            return null;
        }
        List<Record> records = new ArrayList<>();
        for (int i = 0; i < sparseArray.size(); i++) {
            long key = sparseArray.keyAt(i);
            Record record = sparseArray.get(key);
            records.add(record);
        }
        return records;
    }

    public static LongSparseArray<Record> listToMap(List<Record> list) {
        if (list == null) {
            return null;
        }
        LongSparseArray<Record> array = new LongSparseArray<>();
        Stream.of(list).forEach(r -> array.append(r.time, r));
        return array;
    }

    public boolean contains(Type type) {
        return allFigure.containsKey(type.toString().toLowerCase());
    }
}
