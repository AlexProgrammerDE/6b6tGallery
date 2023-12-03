FROM openjdk:21-jdk

VOLUME ["/data/websitegallery/public", "/data/websitegallery/submit"]
COPY /build/distributions/WebsiteGallery-*.tar /data/websitegallery/WebsiteGallery.tar

WORKDIR /data/websitegallery

RUN tar -xvf WebsiteGallery.tar

RUN mv WebsiteGallery-* WebsiteGallery

RUN java -version

CMD ["/bin/bash", "WebsiteGallery/bin/WebsiteGallery"]

EXPOSE 5757
