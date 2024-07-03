package com.yourpackage.controllers;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    private final RestTemplate restTemplate;

    public HelloController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/api/hello")
    public Map<String, Object> hello(@RequestParam String visitor_name, HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();
        String location = getLocation(clientIp);
        String temperature = getTemperature(location);
        String greeting = "Hello, " + visitor_name + "!, the temperature is " + temperature + " degrees Celsius in "
                + location;

        Map<String, Object> response = new HashMap<>();
        response.put("client_ip", clientIp);
        response.put("location", location);
        response.put("greeting", greeting);

        return response;
    }

    private String getLocation(String ip) {
        String apiUrl = "http://ip-api.com/json/" + ip;

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> apiResponse = responseEntity.getBody();

        if (apiResponse != null && apiResponse.get("city") != null) {
            return (String) apiResponse.get("city");
        }
        return "Unknown Location";
    }

    @SuppressWarnings("unchecked")
    private String getTemperature(String location) {
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + location
                + "&appid=5b139ff3911845d4afb150936240307&units=metric";

        ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                apiUrl,
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> apiResponse = responseEntity.getBody();

        if (apiResponse != null && apiResponse.get("main") != null) {
            Map<String, Object> main = (Map<String, Object>) apiResponse.get("main");
            if (main.get("temp") != null) {
                return main.get("temp").toString();
            }
        }
        return "Unknown Temperature";
    }
}