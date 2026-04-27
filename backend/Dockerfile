# Stage 1: liboqs C 네이티브 라이브러리 빌드
FROM ubuntu:22.04 AS liboqs-builder

RUN apt-get update && apt-get install -y --no-install-recommends \
    cmake ninja-build gcc g++ libssl-dev git ca-certificates \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /build
RUN git clone --depth 1 --branch main https://github.com/open-quantum-safe/liboqs.git

WORKDIR /build/liboqs
RUN cmake -GNinja \
    -DCMAKE_BUILD_TYPE=Release \
    -DCMAKE_INSTALL_PREFIX=/opt/liboqs \
    -DBUILD_SHARED_LIBS=ON \
    -DOQS_BUILD_ONLY_LIB=ON \
    . \
    && ninja \
    && ninja install

# Stage 2: Spring Boot 앱 빌드
FROM eclipse-temurin:21-jdk-jammy AS app-builder

WORKDIR /workspace

# 의존성 레이어 캐시 (소스 변경 시 재다운로드 방지)
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle/ gradle/
RUN ./gradlew dependencies --no-daemon -q

COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# Stage 3: 운영 이미지
FROM eclipse-temurin:21-jre-jammy

# liboqs 네이티브 라이브러리 복사
COPY --from=liboqs-builder /opt/liboqs/lib /opt/liboqs/lib

COPY --from=app-builder /workspace/build/libs/*.jar app.jar

ENV LD_LIBRARY_PATH=/opt/liboqs/lib
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.library.path=/opt/liboqs/lib", \
    "-jar", "/app.jar"]
