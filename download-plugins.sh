#!/bin/bash -ex

if [ -z "$BLUEOCEAN_VERSION" ]; then
  echo "Please set BLUEOCEAN_VERSION environment variable"
  exit 1
fi

WORKDIR=/tmp/download-plugins
mkdir -p "$WORKDIR/maven"

MAVEN_VERSION=3.3.9
curl -fsSL http://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz \
  | tar  xzf - -C "$WORKDIR/maven" --strip-components=1

mkdir -p "$WORKDIR/fake-project"

cat > "$WORKDIR/fake-project/pom.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.jenkins-ci.plugins</groupId>
    <artifactId>plugin</artifactId>
    <version>2.3</version>
  </parent>

  <artifactId>download-plugins</artifactId>
  <packaging>pom</packaging>

  <dependencies>
    <dependency>
      <groupId>io.jenkins.blueocean</groupId>
      <artifactId>blueocean</artifactId>
      <version>$BLUEOCEAN_VERSION</version>
      <type>hpi</type>
      <scope>provided</scope>
    </dependency>
  </dependencies>

  <repositories>
    <repository>
      <id>repo.jenkins-ci.org</id>
      <url>https://repo.jenkins-ci.org/public/</url>
    </repository>
  </repositories>

  <pluginRepositories>
    <pluginRepository>
      <id>repo.jenkins-ci.org</id>
      <url>https://repo.jenkins-ci.org/public/</url>
    </pluginRepository>
  </pluginRepositories>

  <build>
    <plugins>
      <plugin>
        <groupId>org.jenkins-ci.tools</groupId>
        <artifactId>maven-hpi-plugin</artifactId>
        <version>1.119</version>
        <executions>
          <execution>
            <goals>
              <goal>bundle-plugins</goal>
            </goals>
            <configuration>
              <outputDirectory>/usr/share/jenkins/ref/plugins</outputDirectory>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
EOF

(cd "$WORKDIR/fake-project" && "$WORKDIR/maven/bin/mvn" -Dmaven.repo.local="$WORKDIR/.m2" -q package)
rm -rf "$WORKDIR"
