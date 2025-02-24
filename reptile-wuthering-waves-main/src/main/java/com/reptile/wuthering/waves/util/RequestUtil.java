package com.reptile.wuthering.waves.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class RequestUtil {

    private final static  RestTemplate restTemplate = new RestTemplate();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T postRequest(String url, String requestBody, Class<T> responseType) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            // 使用 exchange 方法来发送 POST 请求，并处理泛型返回类型
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
    }

    public static String getRequest(String url) {
        try {
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
    }

    public static <T> T postRequest(String url, Object requestBody, Class<T> responseType) {
        try {
            HttpEntity<String> entity = getHttpEntity(requestBody);
            // 使用 exchange 方法来发送 POST 请求，并处理泛型返回类型
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
    }

    public static <T> T postRequestWithGeneric(String url, Object requestBody, ParameterizedTypeReference<T> responseType) {
        try {
            HttpEntity<String> entity = getHttpEntity(requestBody);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.POST, entity, responseType);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert request body to JSON", e);
        }
    }


    @SneakyThrows
    public static HttpEntity<String> getHttpEntity(Object requestBody) {
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
       return new HttpEntity<>(jsonBody, headers);
    }
}
