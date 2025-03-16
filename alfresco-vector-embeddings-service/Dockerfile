FROM python:3.9-slim

RUN apt-get update && apt-get install -y \
    iptables \
    curl \
    iputils-ping

COPY . /app
WORKDIR /app

RUN pip install -r requirements.txt

EXPOSE 8000

CMD ["python", "main.py"]