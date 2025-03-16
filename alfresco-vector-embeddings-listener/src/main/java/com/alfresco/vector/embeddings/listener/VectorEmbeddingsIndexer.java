package com.alfresco.vector.embeddings.listener;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VectorEmbeddingsIndexer {

	public static void main(String[] args) {
		SpringApplication.run(VectorEmbeddingsIndexer.class, args);
	}

}