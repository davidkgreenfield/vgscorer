package com.dbg.vgscorer.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class ImagingConfig {

    @Value("${GOOGLE_APPLICATION_CREDENTIALS}")
    String credJson;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient(CredentialsProvider credentialsProvider) throws IOException {

        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(credJson.getBytes()));

        FixedCredentialsProvider fixedCredentialsProvider = FixedCredentialsProvider.create(credentials);

        ImageAnnotatorSettings clientSettings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(fixedCredentialsProvider)
                .build();

        return ImageAnnotatorClient.create(clientSettings);
    }

}
