package com.dbg.vgscorer.imaging;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Slf4j
@RestController("/image/mariokart")
public class MarioKartImagingController {

    private final ImageAnnotatorClient vision;
    private final String secret;

    public MarioKartImagingController(ImageAnnotatorClient imageAnnotatorClient,
                                      @Value("${secret}")String secret) {
        this.vision = imageAnnotatorClient;
        this.secret = secret;
    }


    @PostMapping("/text/results")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<String>> results(@RequestParam("file") MultipartFile file,
                                               @RequestHeader("secret") String secretParm) throws IOException {
        if(!secret.equals(secretParm)) return new ResponseEntity(HttpStatus.FORBIDDEN);
        Image image = Image.newBuilder().setContent(ByteString.copyFrom(file.getBytes())).build();
        Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        List<AnnotateImageResponse> responsesList = vision.batchAnnotateImages(asList(request)).getResponsesList();

        return new ResponseEntity(translateToStringList(responsesList),HttpStatus.OK);

    }

    private List<String> translateToStringList(List<AnnotateImageResponse> responsesList) {
        List<EntityAnnotation> entityAnnotations = responsesList.stream()
                .flatMap(response -> response.getTextAnnotationsList().stream())
                .skip(1)
                .filter(entityAnnotation -> !NumberUtils.isCreatable(entityAnnotation.getDescription()))
                .filter(entityAnnotation -> {
                    String s = entityAnnotation.getDescription().replaceAll("\\d", "");
                    if (s.matches("[A-Za-z]+")) return true;
                    return false;
                })
                .collect(Collectors.toList());

        Map<Integer, List<EntityAnnotation>> grouped = new HashMap<>();

        final Integer SLACK = 20;

        Integer prev = null;
        for(EntityAnnotation ea : entityAnnotations){
            int y = ea.getBoundingPoly().getVertices(0).getY();
            if(prev != null && (prev - SLACK <= y &&
                                prev + SLACK >= y)){
                List<EntityAnnotation> entityAnnotations1 = grouped.get(prev);
                entityAnnotations1.add(ea);
            }else{
                List<EntityAnnotation> newList = new ArrayList<>();
                newList.add(ea);
                grouped.put(y, newList);
                prev = y;
            }
        }

        List<Integer> sorted = grouped.keySet().stream().sorted().collect(Collectors.toList());
        List<String> descriptions = new ArrayList<>();

        for(Integer y: sorted){
            descriptions.add(grouped.get(y).stream()
                    .map(ea -> ea.getDescription())
                    .reduce((desc1, desc2) -> desc1 + " " + desc2)
                    .orElse(""));
        }

        return descriptions.stream()
                .map(description -> description.replaceAll("\\d", ""))
                .collect(Collectors.toList());
    }

}
