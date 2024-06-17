package com.whiskels.order.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    public static String toJson(Object obj) {
        return MAPPER.writeValueAsString(obj);
    }
}
