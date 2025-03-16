package com.alfresco.vector.embeddings.listener.event;

import org.alfresco.event.sdk.handling.filter.EventFilter;
import org.alfresco.event.sdk.handling.filter.NodeTypeFilter;
import org.alfresco.event.sdk.handling.handler.OnNodeDeletedEventHandler;
import org.alfresco.repo.event.v1.model.*;
import com.alfresco.vector.embeddings.listener.service.PythonApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler que escucha la eliminación de un documento cm:content
 * y ordena al servicio Python que borre su vector.
 */
@Component
public class ContentDeletedHandler implements OnNodeDeletedEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ContentDeletedHandler.class);

    private final PythonApiService pythonApiService;

    public ContentDeletedHandler(PythonApiService pythonApiService) {
        this.pythonApiService = pythonApiService;
    }

    @Override
    public void handleEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {
        NodeResource nodeResource = (NodeResource) repoEvent.getData().getResource();
        String nodeId = nodeResource.getId();

        LOG.info("Se ha eliminado un documento cm:content con ID={}, nombre={}",
            nodeId, nodeResource.getName()
        );

        // Llamamos a Python para eliminar el vector
        pythonApiService.deleteVectorEmbeddings(nodeId);
        LOG.info("Se ha solicitado la eliminación del vector para el documento {}", nodeId);
    }

    /**
     * Filtra solo la eliminación de nodos cm:content.
     */
    @Override
    public EventFilter getEventFilter() {
        return NodeTypeFilter.of("cm:content");
    }
}
