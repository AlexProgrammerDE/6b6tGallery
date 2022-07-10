./gradlew build
docker build -t 6b6tgallery:local .
docker run --env-file data.env -p 5757:5757 6b6tgallery:local
