FROM openjdk:8u212-jre-slim-stretch

COPY ./kelpie.tar .

RUN tar xf kelpie.tar && rm -f kelpie.tar

RUN mv kelpie/bin/* /usr/local/bin/ && \
    mv kelpie/lib/* /usr/local/lib/ && \
    rm -rf kelpie

ENTRYPOINT [ "/usr/local/bin/kelpie" ]
