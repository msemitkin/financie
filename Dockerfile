FROM openjdk:17-oracle
RUN apt-get update && \
    apt-get install -y curl && \
    curl -s "https://get.sdkman.io" | bash && \
    bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install gradle"
RUN ./gradlew build -x test
RUN mkdir /financie
COPY /build/libs/financie-0.0.1-SNAPSHOT.jar /financie
WORKDIR /financie
CMD ["java", "-jar", "financie-0.0.1-SNAPSHOT.jar"]