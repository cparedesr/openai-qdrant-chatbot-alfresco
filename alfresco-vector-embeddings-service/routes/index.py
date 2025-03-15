from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from uuid import UUID
import openai
from qdrant_client.http.models import PointIdsList
from decouple import config
from qdrant.qdrant_setup import qdrant_client, collection_name

router = APIRouter()

openai.api_key = config("OPENAI_API_KEY")

class PostNode(BaseModel):
    nodeid: UUID
    text: str

@router.post("/index")
async def index_post(data: PostNode):
    
    nodeid_str = str(data.nodeid)
    
    existing_points = qdrant_client.retrieve(
        collection_name=collection_name,
        ids=[nodeid_str],
        with_payload=True,
        with_vectors=False
    )
    
    if existing_points:
        raise HTTPException(status_code=409, detail="Document already exists")

    embedding_response = openai.embeddings.create(
        model="text-embedding-ada-002",
        input=data.text
    )
    
    vector = embedding_response.data[0].embedding

    qdrant_client.upsert(
         collection_name=collection_name,
         points=[
             {
                 "id": str(data.nodeid),
                 "vector": vector,
                 "payload": {"text": data.text}
             }
         ]
    )
    
    return {
        "statusCode": "200",  
        "data": {
            "nodeid": str(data.nodeid),
            "payload": data.text,
            "vector": vector
            }
        }
    
class UpdateNode(BaseModel):
    nodeid: UUID
    text: str    

@router.put("/index")
async def update_node(data: UpdateNode):
    nodeid_str = str(data.nodeid)
    
    result = qdrant_client.retrieve(
        collection_name=collection_name,
        ids=[nodeid_str],
        with_payload=True,
        with_vectors=False
    )
    
    if not result:
        raise HTTPException(status_code=404, detail="No existe el nodo en la BBDD" )
    embedding_response = openai.embeddings.create(
        model="text-embedding-ada-002",
        input=data.text
    )
    vector = embedding_response.data[0].embedding
    qdrant_client.upsert(
        collection_name=collection_name,
        points=[
             {
                 "id": str(data.nodeid),
                 "vector": vector,
                 "payload": {"text": data.text}
             }
         ]
    )
    return {"status": "success", "nodeid": str(data.nodeid), "vector": vector}

@router.get("/index/{nodeid}")
async def search_node(nodeid: UUID) :
    
    nodeid_str = str(nodeid)
    
    result = qdrant_client.retrieve(
        collection_name=collection_name,
        ids=[nodeid_str],
        with_payload=True,
        with_vectors=True
    )
    
    if result:
        return {
            "statusCode": "200",
            "data": {
                "nodeid": nodeid_str,
                "payload": result[0].payload,
                "vector": result[0].vector
            }
        }
        
    else:
        raise HTTPException(status_code=404, detail="No existe el nodo en la BBDD")
    
@router.delete("/index/{nodeid}")
async def delete_node(nodeid: UUID):
    nodeid_str = str(nodeid)
    
    result = qdrant_client.retrieve(
        collection_name=collection_name,
        ids=[nodeid_str],
        with_payload=True,
        with_vectors=False
    )
    
    if not result:
        raise HTTPException(status_code=404, detail="No existe el nodo en la BBDD")
    
    points_selector = PointIdsList(points=[nodeid_str])
    
    qdrant_client.delete(
        collection_name=collection_name,
        points_selector=points_selector
    )
    
    return {"status": "deleted", 
            "data": {
                "nodeid": nodeid_str}
            }