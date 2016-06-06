FROM buildpack-deps:trusty-curl

# Install 32-bit libraries required by the Android SDK
RUN apt-get update && apt-get install -y --no-install-recommends \
    lib32ncurses5 \
    lib32stdc++6 \
    lib32z1 \
    libc6-i386 \
 && rm -rf /var/lib/apt/lists/*

# Install Java 8
RUN curl -fsSL http://abondance.internal.forgerock.com/pkg/jdk/jdk-8u92-linux-x64.tar.gz | tar xzf - -C /opt && mv /opt/jdk1.8.0_92 /opt/jdk8
RUN update-alternatives --install "/usr/bin/java" java "/opt/jdk8/bin/java" 1

# Disabled beacuse requires Docker >= 1.9
#ARG build_uid=1011
#ARG build_gid=1011
ENV build_uid=1011
ENV build_gid=1011

RUN groupadd -r build -g ${build_gid} \
 && useradd -m -r -g build -u ${build_uid} build

USER build
WORKDIR /home/build

# Install the Android SDK
ENV ANDROID_SDK_VERSION r24.4.1
ENV ANDROID_BUILD_TOOLS_VERSION 23.0.2
ENV ANDROID_SDK_FILENAME android-sdk_${ANDROID_SDK_VERSION}-linux.tgz
ENV ANDROID_SDK_URL https://dl.google.com/android/${ANDROID_SDK_FILENAME}
ENV ANDROID_API_LEVELS android-14,android-15,android-16,android-17,android-18,android-19,android-20,android-21,android-22,android-23 
ENV ANDROID_HOME /home/build/android-sdk-linux
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

RUN wget -q ${ANDROID_SDK_URL} && \
    tar -xzf ${ANDROID_SDK_FILENAME} && \
    rm ${ANDROID_SDK_FILENAME} && \
    echo y | android update sdk --no-ui -a --filter tools,platform-tools,${ANDROID_API_LEVELS},build-tools-${ANDROID_BUILD_TOOLS_VERSION},extra-android-m2repository,extra-google-m2repository

WORKDIR /home/build/app

CMD ["/bin/bash", "gradlew", "clean", "build"]
