FROM node:5.11.1

RUN useradd jenkins --shell /bin/bash --create-home
USER jenkins