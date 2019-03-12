package com.github.archerlml.gymbuddy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.archerlml.gymbuddy.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by archerlml on 12/6/17.
 */

public class Rule {
    public enum Aggregation {
        Total,
        Average,
        Interval,
        Count,
        Value;
        static Map<String, Aggregation> typeMap;

        static Map<String, Aggregation> getTypeMap() {
            if (typeMap == null) {
                typeMap = new HashMap<>();
                for (Aggregation aggregation : Aggregation.values()) {
                    typeMap.put(aggregation.toString().toLowerCase(), aggregation);
                }
            }
            return typeMap;
        }

        @JsonValue
        public String toValue() {
            return toString().toLowerCase();
        }

        @JsonCreator
        public static Aggregation forValue(String type) {
            return getTypeMap().get(type.toLowerCase());
        }
    }

    public Figure.Type type;
    public Aggregation aggregation;
    public Float min;
    public Float max;

    @Override
    public String toString() {
        return Util.getString(aggregation, " of ", type, ": ", min == null ? "–∞" : min, "~", max == null ? "+∞" : max);
    }
}
