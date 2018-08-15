FROM anapsix/alpine-java:9
COPY ./build/install .
WORKDIR AuthService/bin
EXPOSE 80
CMD ./AuthService


