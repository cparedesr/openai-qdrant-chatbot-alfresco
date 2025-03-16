# openai-qdrant-chatbot-alfresco 🚀

¡Bienvenido al proyecto Alfresco Vector Embeddings! Aquí integramos Alfresco Community 23.4.1 con Java 17 junto a un servicio Python que indexa vectores embeddings en Qdrant.⚡

## ✨ Visión General
Este proyecto combina:

- Aplicación Java (process-of-the-box) para escuchar eventos en Alfresco (creación, actualización y eliminación de documentos) y extraer el texto de PDFs.
- Servicio Python (FastAPI + Qdrant) que recibe el texto, genera embeddings con OpenAI y los almacena en Qdrant, ofreciendo además un pequeño chatbot con Gradio.

## 📋 Requisitos Previos
- Java 17 ☕ (instalado y configurado correctamente).
- Maven 3.6 o superior.
- Docker y docker-compose para desplegar todos los servicios.
- Api-key de openai para la integración. La configuración de la api-key se realiza en el fichero .env de la carpeta alfresco-vector-embeddings-service.

## 📂 Estructura de Carpetas
- alfresco-vector-embeddings-listener/
➡ Contiene la aplicación Java (Spring Boot) y la lógica para interceptar y procesar los eventos de Alfresco.

- alfresco-vector-embeddings-service/
➡ El servicio Python con FastAPI, Qdrant y Gradio.

- docker-compose.yaml
➡ Orquestador que levanta todos los contenedores: Alfresco, Share, ActiveMQ, PostgreSQL, Qdrant, la app Java y el servicio Python.

## ⚙️ Compilación de la Aplicación Java
1. Entra a la carpeta alfresco-vector-embeddings-listener.
2. Ejecuta:
```
mvn clean install
```
Se descargarán dependencias y se construirá el jar final.

## 🚀 Arrancar Todo con Docker Compose
1. Asegúrate de que el archivo docker-compose.yaml esté en la raíz del proyecto y existan las carpetas alfresco-vector-embeddings-listener/ y alfresco-vector-embeddings-service/.
2. Desde la raíz, corre:
```
docker compose up --build
```
3. Esto levantará:
Alfresco Community 23.4.1 (puerto 8080).
Share (puerto 8081).
ActiveMQ, PostgreSQL, Qdrant y nuestros contenedores de Java y Python.
4. Revisa los logs para confirmar que todo funcione correctamente. ¡Paciencia en el primer arranque!


## 🐍 Detalles del Servicio Python
Dentro de alfresco-vector-embeddings-service/, encontrarás:

1. main.py:
Crea la app con FastAPI y monta Gradio en la ruta /chatbot.
Te permite acceder a la API y a una UI para el chatbot (en http://localhost:8000/chatbot).
2. index.py
Expone las rutas para crear, actualizar, consultar y eliminar los embeddings asociados a un documento:
POST /index
PUT /index
GET /index/{nodeid}
DELETE /index/{nodeid}
3. chatbot.py
Lógica del Chatbot con Gradio:
Recibe texto o audio.
Usa OpenAI Whisper para transcribir (si es audio).
Genera embeddings con OpenAI, busca en Qdrant y responde con GPT-4 (o similar).
Perfecto para demo interactiva. 💬
4. qdrant_setup.py
Comprueba y crea la colección en Qdrant al inicio, según variables de entorno como QDRANT_HOST, QDRANT_PORT y QDRANT_COLLECTION.

🔄 Flujo de Indexación
1. Java (Evento de creación o actualización)
Detecta que se creó/actualizó un PDF (cm:content), lo descarga, extrae el texto con PDFBox y lo envía al servicio Python.
2. Python
Recibe el texto, llama a OpenAI para generar embeddings y los guarda en Qdrant.
3. Java (Evento de eliminación)
Ordena a Python que borre el vector en Qdrant.
¡Así de simple y elegante! 🪄

## 🤖 Chatbot con Gradio
Disponible en http://localhost:8000/chatbot.
Permite introducir preguntas en texto o grabar en audio.
Genera la respuesta con apoyo en la información almacenada en Qdrant.
Ideal para pruebas rápidas y demostraciones.