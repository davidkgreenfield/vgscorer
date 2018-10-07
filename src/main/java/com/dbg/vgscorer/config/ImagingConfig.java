package com.dbg.vgscorer.config;

import com.google.api.gax.core.CredentialsProvider;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;

import static springfox.documentation.builders.PathSelectors.regex;

@Configuration
@EnableSwagger2
public class ImagingConfig  {

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient(CredentialsProvider credentialsProvider) throws IOException {
        ImageAnnotatorSettings clientSettings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();

        return ImageAnnotatorClient.create(clientSettings);
    }

}
