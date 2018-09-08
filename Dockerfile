FROM anapsix/alpine-java:9
COPY ./build/install .
WORKDIR RoleService/bin
EXPOSE 80
CMD ./RoleService


