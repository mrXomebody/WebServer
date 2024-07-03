
package com.yourpackage.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/api/hello")
    public Map<String, Object> hello(@RequestParam String visitor_name,
            @RequestHeader("X-Forwarded-For") String clientIp) {
        Map<String, Object> response = new HashMap<>();
        try {
            String location = getLocation(clientIp);
            String temperature = getTemperature(location);
            String greeting = "Hello, " + visitor_name + "!, the temperature is " + temperature + " degrees Celsius in "
                    + location;

            response.put("client_ip", clientIp);
            response.put("location", location);
            response.put("greeting", greeting);

        } catch (Exception e) {
            logger.error("Error while processing request: ", e);
            response.put("error", "An error occurred while processing your request.");
        }
        return response;
    }

    private String getLocation(String ip) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.ipgeolocation.io/ipgeo?apiKey=d8b3fa7162644e3f965c76a8c56607f9&ip=" + ip;

        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    apiUrl,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> apiResponse = responseEntity.getBody();

            if (apiResponse != null && apiResponse.get("city") != null) {
                return (String) apiResponse.get("city");
            }
        } catch (Exception e) {
            logger.error("Error while fetching location: ", e);
        }
        return "Unknown Location";
    }

    @SuppressWarnings("unchecked")
    private String getTemperature(String location) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + location
                + "&appid=8a02928b40e8b63ffff80b45b8f9e820&units=metric";

        try {
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    apiUrl,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> apiResponse = responseEntity.getBody();

            if (apiResponse != null && apiResponse.get("main") != null) {
                Map<String, Object> main = (Map<String, Object>) apiResponse.get("main");
                if (main.get("temp") != null) {
                    return main.get("temp").toString();
                }
            }
        } catch (Exception e) {
            logger.error("Error while fetching temperature: ", e);
        }
        return "Unknown Temperature";
    }
}