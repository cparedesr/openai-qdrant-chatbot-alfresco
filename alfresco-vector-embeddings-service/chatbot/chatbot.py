import gradio as gr
from openai import OpenAI
from decouple import config
from qdrant.qdrant_setup import qdrant_client, collection_name

# Configura tu API key de OpenAI
client = OpenAI(api_key=config("OPENAI_API_KEY"))

def fresquito_answer(text_input, audio_input):
    
    # Validación: solo debe usarse un tipo de entrada
    if text_input and audio_input:
        return "Introduce solo texto O audio, no ambos.", None
    if not text_input and not audio_input:
        return "Por favor, introduce una pregunta en texto o audio.", None

    # 1) Determinar si la entrada es texto o audio
    if text_input:
        question_text = text_input
        mode = "text"
    else:
        # Transcribir el audio usando la nueva API de OpenAI
        with open(audio_input, "rb") as audio_file:
            transcription = client.audio.transcriptions.create(
                model="whisper-1",
                file=audio_file
            )
        question_text = transcription.text
        mode = "audio"

    # 2) Generar embedding usando la nueva interfaz
    embedding_response = client.embeddings.create(
        model="text-embedding-ada-002",
        input=question_text
    )
    query_vector = embedding_response.data[0].embedding

    # 3) Buscar en Qdrant (documentos similares)
    search_result = qdrant_client.search(
        collection_name=collection_name,
        query_vector=query_vector,
        limit=3  # Ajusta según necesidades
    )

    # 4) Combinar contenido de los documentos para formar contexto
    context_text = ""
    for point in search_result:
        if "text" in point.payload:
            context_text += point.payload["text"] + "\n"

    # 5) Validar si hay contexto
    if not context_text:
        answer_text = "No tengo información sobre eso."
    else:
        # 6) Armar el mensaje que le pasaremos al modelo de Chat
        system_content = config("PROMT_SYSTEM")
        user_message = (
            f"Contexto:\n{context_text}\n\n"
            f"Pregunta: {question_text}\n\n"
            "Responde únicamente con la información proporcionada en el contexto."
        )

        # 7) Crear la respuesta con ChatCompletion en la nueva API
        completion = client.chat.completions.create(
            model="gpt-4",  # o "gpt-4" si lo tienes disponible
            messages=[
                {"role": "system", "content": system_content},
                {"role": "user", "content": user_message}
            ],
            max_tokens=2000,
            temperature=0.2  # Baja temperatura para reducir la creatividad
        )
        # Obtenemos la respuesta generada
        answer_text = completion.choices[0].message.content.strip()

    # 8) Devolver la respuesta: texto o audio
    if mode == "text":
        return answer_text, None
    else:
        from gtts import gTTS
        tts = gTTS(answer_text, lang="es")
        output_audio = "respuesta.mp3"
        tts.save(output_audio)
        return None, output_audio

# 9) Crear la interfaz de Gradio para "Fresquito"
fresquito_app = gr.Interface(
    fn=fresquito_answer,
    inputs=[
        gr.Textbox(
            label="Pregunta de texto",
            placeholder="Escribe tu pregunta...",
            lines=5,  # Aumenta el número de líneas para la caja de pregunta
        ),
        gr.Audio(sources=["microphone"], type="filepath", label="Pregunta de audio")
    ],
    outputs=[
        gr.Textbox(
            label="Respuesta en texto",
            lines=20,  # Aumenta el número de líneas para la caja de respuesta
        ),
        gr.Audio(label="Respuesta en audio")
    ],
    title="Fresquito Assistant AI",
    description="Introduce tu pregunta en texto o audio. Si usas audio, la respuesta será en audio."
)