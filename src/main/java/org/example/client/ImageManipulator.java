package org.example.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageManipulator {
    public static List<String> addDomainToURLs(URL url, List<String> imagesURL) {
        List<String> finalImg = new ArrayList<>();
        for (String image : imagesURL) {
            if (!image.contains("://")) {
                finalImg.add(url + image);
                System.out.println(url + image);
            }else{
                finalImg.add(image);
                System.out.println(image);
            }
        }
        return finalImg;
    }
}
