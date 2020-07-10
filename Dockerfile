FROM gradle:jdk8

COPY ./kelpie.tar .

RUN tar xf kelpie.tar && rm -f kelpie.tar

RUN mv kelpie/bin/* /usr/local/bin/ && \
    mv kelpie/lib/* /usr/local/lib/ && \
    rm -rf kelpie

RUN git clone https://github.com/scalar-labs/kelpie-test.git kelpie-test && \
    cd kelpie-test/client-test && \
    gradle shadowJar

WORKDIR /home/gradle/kelpie-test/