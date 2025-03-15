from qdrant_client import QdrantClient
from qdrant_client.http.models import Distance, VectorParams
from decouple import config

q_host = config("QDRANT_HOST")
q_port = config("QDRANT_PORT")
collection_name = config("QDRANT_COLLECTION")

qdrant_client = QdrantClient(host=q_host, port=q_port)

def init_qdrant_collection():
    """Verifica si la colección existe y, en caso contrario, la crea."""
    collections = qdrant_client.get_collections().collections
    if collection_name not in [col.name for col in collections]:
        qdrant_client.create_collection(
            collection_name=collection_name,
            vectors_config=VectorParams(size=1536, distance=Distance.COSINE)
        )

init_qdrant_collection()