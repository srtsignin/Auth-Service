FROM anapsix/alpine-java:8
COPY ./build/install .
WORKDIR RoleService/bin
EXPOSE 80
CMD ./RoleService


