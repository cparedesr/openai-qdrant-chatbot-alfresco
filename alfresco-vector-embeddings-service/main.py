from fastapi import FastAPI
from routes.index import router
import gradio as gr
from chatbot.chatbot import fresquito_app

app = FastAPI()
app.include_router(router)

# Monta la aplicación Gradio en la ruta '/fresquito'
app = gr.mount_gradio_app(app, fresquito_app, path="/chatbot")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000)