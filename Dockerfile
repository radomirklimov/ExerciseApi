FROM azul/zulu-openjdk:21-latest
COPY build/libs/*.jar organizer.jar
ENTRYPOINT ["java","-jar","/organizer.jar"]
