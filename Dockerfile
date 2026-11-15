# ---------- build: GraalVM native image (Linux) ----------
FROM ghcr.io/graalvm/native-image-community:21 AS build
WORKDIR /build

ARG MAVEN_VERSION=3.9.9
RUN microdnf install -y tar gzip && \
    curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz" -o /tmp/maven.tar.gz && \
    tar -xzf /tmp/maven.tar.gz -C /opt && \
    ln -s "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven && \
    rm /tmp/maven.tar.gz
ENV PATH="/opt/maven/bin:${PATH}"

# single-module project: pom first so the dependency layer is cached
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src src

RUN mvn -B -Pnative -DskipTests package native:compile

# ---------- runtime ----------
FROM debian:bookworm-slim
WORKDIR /app
EXPOSE 8080
COPY --from=build /build/target/gateway-pokemon /app/app
ENTRYPOINT ["/app/app"]
