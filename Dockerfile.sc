FROM debian
RUN apt update && \
    apt install -y wget && \
    rm -rf /var/lib/apt/lists/* && \
    wget -O - https://saucelabs.com/downloads/sc-4.5.3-linux.tar.gz | tar xvzf -

ENV BUILD_TAG ""
RUN printf "#!/bin/bash \n\
    if [ -z \"\$BUILD_TAG\" ]; then\n\
    /sc-4.5.3-linux/bin/sc -u \$SAUCE_USERNAME -k \$SAUCE_ACCESS_KEY \n\
    else\n\
    /sc-4.5.3-linux/bin/sc -u \$SAUCE_USERNAME -k \$SAUCE_ACCESS_KEY -i \$BUILD_TAG \n\
    fi \n\
    \n" > /entrypoint.sh && chmod 755 /entrypoint.sh
ENTRYPOINT /entrypoint.sh
