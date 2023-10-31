package com.vctek.orderservice.promotionengine.droolsruleengineservice.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vctek.orderservice.promotionengine.droolsruleengineservice.exception.DroolsRuleValueFormatterException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Map2StringUtils {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static String mapToString(Map<String, String> map) {
        try {
            if(MapUtils.isNotEmpty(map)) {
                return objectMapper.writeValueAsString(map);
            }

            return StringUtils.EMPTY;
        } catch (JsonProcessingException e) {
            throw new DroolsRuleValueFormatterException("Cannot parse map value to String");
        }
    }

    public static Map<String,String> stringToMap(String mapValues) {
        try {
            if(StringUtils.isNotBlank(mapValues)) {
                return objectMapper.readValue(mapValues, new TypeReference<Map<String, String>>(){});
            }

            return new HashMap<>();
        } catch (IOException e) {
            throw new DroolsRuleValueFormatterException("Cannot parse map value to String");
        }
    }
}
