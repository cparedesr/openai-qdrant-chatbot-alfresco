package com.alfresco.vector.embeddings.listener.event;

import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.filter.NodeTypeFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeCreatedEventHandler;
import org.alfresco.repo.event.v1.model.*;
import com.alfresco.vector.embeddings.listener.service.PythonApiService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ContentCreatedHandler implements OnNodeCreatedEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ContentCreatedHandler.class);

    private final RestTemplate restTemplate;
    private final PythonApiService pythonApiService;

    @Value("${alfresco.acs.host}")
    private String alfrescoHost;

    @Value("${alfresco.acs.user}")
    private String alfrescoUser;

    @Value("${alfresco.acs.password}")
    private String alfrescoPassword;

    public ContentCreatedHandler(RestTemplate restTemplate,
                                 PythonApiService pythonApiService) {
        this.restTemplate = restTemplate;
        this.pythonApiService = pythonApiService;
    }

    @Override
    public void handleEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {
        NodeResource nodeResource = (NodeResource) repoEvent.getData().getResource();
        String nodeId = nodeResource.getId();

        LOG.info("Se ha creado un documento cm:content con ID={}, nombre={}, mimetype={}",
            nodeId,
            nodeResource.getName(),
            (nodeResource.getContent() != null) ? nodeResource.getContent().getMimeType() : "desconocido"
        );

        byte[] contentBytes = downloadContent(nodeId);
        if (contentBytes == null) {
            LOG.warn("El nodo {} no tiene contenido o no se pudo descargar.", nodeId);
            return;
        }

        try (InputStream pdfStream = new ByteArrayInputStream(contentBytes);
             PDDocument pdfDoc = PDDocument.load(pdfStream)) {

            PDFTextStripper stripper = new PDFTextStripper();
            String extractedText = stripper.getText(pdfDoc);
            LOG.debug("Texto extraído ({} caracteres): {}", extractedText.length(), extractedText);

            pythonApiService.indexVectorEmbeddings(nodeId, extractedText);
            LOG.info("Documento {} indexado correctamente en Python", nodeId);

        } catch (IOException e) {
            LOG.error("Error al procesar el PDF del nodo {}", nodeId, e);
        }
    }

    @Override
    public EventFilter getEventFilter() {
        return NodeTypeFilter.of("cm:content");
    }

    /**
     * Descarga el contenido binario de un nodo usando las credenciales que tengas configuradas.
     * Ajusta la URL base según tu entorno.
     *
     * @param nodeId El ID del documento en Alfresco
     * @return El contenido en bytes o null si falló
     */
    private byte[] downloadContent(String nodeId) {

        String url = String.format("%s/alfresco/api/-default-/public/alfresco/versions/1/nodes/%s/content",
                alfrescoHost, nodeId);

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(alfrescoUser, alfrescoPassword);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                LOG.warn("No se pudo descargar el contenido. Status: {}", response.getStatusCode());
            }
        } catch (Exception ex) {
            LOG.error("Error descargando contenido de nodo {} en {}", nodeId, url, ex);
        }
        return null;
    }
}