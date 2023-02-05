gradlew.bat clean shadowJar
docker run -v %CD%:/source http4k/amazoncorretto-lambda-runtime-arm64:latest build/libs/getit-api-all.jar build/distributions/getit-api-all.zip
sam.cmd build
sam.cmd deploy