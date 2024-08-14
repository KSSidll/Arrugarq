FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update
RUN apt-get install -y wget unzip git

ENV ANDROID_SDK_URL=https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
ENV ANDROID_SDK_ROOT=/usr/local/android-sdk

# Install Android SDK
ENV PATH ${PATH}:${ANDROID_SDK_ROOT}/cmdline-tools/latest/bin:${ANDROID_SDK_ROOT}/platform-tools:${ANDROID_SDK_ROOT}/build-tools/34.0.0
RUN mkdir -p ${ANDROID_SDK_ROOT}/cmdline-tools
RUN wget -q ${ANDROID_SDK_URL} -O android-sdk.zip
RUN unzip -q android-sdk.zip -d ${ANDROID_SDK_ROOT}/cmdline-tools
RUN mv ${ANDROID_SDK_ROOT}/cmdline-tools/cmdline-tools ${ANDROID_SDK_ROOT}/cmdline-tools/latest
RUN rm android-sdk.zip

# Accept licenses
RUN yes | sdkmanager --licenses

# Install necessary SDK components
RUN sdkmanager "platform-tools" \
    "platforms;android-34" \
    "platforms;android-35" \
    "build-tools;34.0.0" \
    "build-tools;35.0.0"

WORKDIR /build