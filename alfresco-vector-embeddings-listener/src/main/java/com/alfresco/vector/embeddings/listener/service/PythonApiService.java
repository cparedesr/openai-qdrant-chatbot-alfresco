package com.alfresco.vector.embeddings.listener.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PythonApiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PythonApiService.class);

    @Value("${python.api.host}")
    private String pythonApiHost;

    @Value("${python.api.port}")
    private int pythonApiPort;

    private final RestTemplate restTemplate = new RestTemplate();

    public void indexVectorEmbeddings(String nodeId, String text) {

        String url = String.format("http://%s:%d/index", pythonApiHost, pythonApiPort);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("nodeid", nodeId);
        requestBody.put("text", text);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestBody, String.class);
            LOGGER.info("Respuesta de la API de Python: {}", response.getBody());
        } catch (Exception e) {
            LOGGER.error("Error al llamar a la API de Python en {}: {}", url, e.getMessage());
        }
    }

    public void deleteVectorEmbeddings(String nodeId) {

        String url = String.format("http://%s:%d/index/%s", pythonApiHost, pythonApiPort, nodeId);

        try {

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, null, String.class
            );
            LOGGER.info("Respuesta de Python (delete): {}", response.getBody());
        } catch (Exception e) {
            LOGGER.error("Error al llamar a la API de Python (delete) en {}: {}", url, e.getMessage());
        }
    }
}