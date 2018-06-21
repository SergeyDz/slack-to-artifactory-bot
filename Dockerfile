FROM openjdk:8

ENV SBT_VERSION 1.1.1
ENV slackkey 123
ENV afuser user
ENV afpassword pass

RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

WORKDIR /slack-bot

COPY . /slack-bot

CMD sbt run
