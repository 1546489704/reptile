package com.reptile.wuthering.waves.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;

public class JacksonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();


    static {
        // 反序列化: JSON 字段中有Java对象中没有不报错
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 禁用默认的多态类型处理
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 序列化: 排除值为 null 的对象
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        TimeZone timeZone = TimeZone.getTimeZone(ZoneId.systemDefault());
        DateTimeFormatter dateTimeFormatter = DateUtil.DATE_TIME_FORMATTER;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtil.DATE_TIME_PATTERN);
        simpleDateFormat.setTimeZone(timeZone);
        MAPPER.setDateFormat(simpleDateFormat);
        MAPPER.setTimeZone(timeZone);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(Long.class, ToStringSerializer.instance);
        javaTimeModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        MAPPER.registerModule(javaTimeModule);
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    public static TypeFactory getTypeFactory() {
        return getObjectMapper().getTypeFactory();
    }


    @SneakyThrows
    public static <T> String toJson(T domain) {
        return getObjectMapper().writeValueAsString(domain);
    }

    @SneakyThrows
    public static <T> T toObject(Map<String, Object> content, Class<T> valueType) {
        return getObjectMapper().convertValue(content, valueType);
    }

    @SneakyThrows
    public static <T> T toObject(String content, TypeReference<T> typeReference) {
        return getObjectMapper().readValue(content, typeReference);
    }

    @SneakyThrows
    public static <T> T toObject(String content, JavaType javaType) {
        return getObjectMapper().readValue(content, javaType);
    }

    @SneakyThrows
    public static <T> T toObject(String content, Class<T> valueType) {
        return getObjectMapper().readValue(content, valueType);
    }


    public static <T> List<T> toList(String content, Class<T> clazz) {
        JavaType javaType = getObjectMapper().getTypeFactory().constructParametricType(List.class, clazz);
        return toObject(content, javaType);
    }



    public static <K, V> Map<K, V> toMap(String content, Class<K> keyClass, Class<V> valueClass) {
        JavaType javaType = getObjectMapper().getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
        return toObject(content, javaType);
    }

    public static Map<String, Object> toMap(String content) {
        return toMap(content, String.class, Object.class);
    }

    public static <T> Set<T> toSet(String content, Class<T> clazz) {
        JavaType javaType = getTypeFactory().constructCollectionLikeType(Set.class, clazz);
        return toObject(content, javaType);
    }

    public static <T> T[] toArray(String content, Class<T> clazz) {
        JavaType javaType = getTypeFactory().constructArrayType(clazz);
        return toObject(content, javaType);
    }

    public static <T> T[] toArray(String content) {
        return toObject(content, new TypeReference<T[]>() {
        });
    }

    @SneakyThrows
    public static JsonNode toNode(String content) {
        return getObjectMapper().readTree(content);
    }

    @SneakyThrows
    public static JsonNode toNode(JsonParser jsonParser) {
        return getObjectMapper().readTree(jsonParser);
    }

    @SneakyThrows
    public static JsonParser createParser(String content) {
        return getObjectMapper().createParser(content);
    }

    public static <R> R loop(JsonNode jsonNode, Function<JsonNode, R> function) {
        if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                loop(entry.getValue(), function);
            }
        }

        if (jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                loop(node, function);
            }
        }

        if (jsonNode.isValueNode()) {
            return function.apply(jsonNode);
        } else {
            return null;
        }
    }

}
