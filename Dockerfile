FROM node

RUN useradd jenkins --shell /bin/bash --create-home
USER jenkins