FROM node:5.11.1

RUN apt-get update && apt-get install -y libelf1

RUN useradd jenkins --shell /bin/bash --create-home
USER jenkins
