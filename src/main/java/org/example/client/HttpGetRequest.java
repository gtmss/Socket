package org.example.client;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpGetRequest {

    public static void main(String[] args) throws Exception {
        
        // Define the target URL
        URL url = new URL("http://me.utm.md/");
        // Open a connection to the URL using a Socket
        Socket socket = new Socket(url.getHost(), 80);

        // Send the HTTP GET request to the server
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println("GET " + url.getPath() + " HTTP/1.1");
        out.println("Host: " + url.getHost());
        out.println("Content-Type: text/html;charset=utf-8");
        out.println("User-Agent: Mozilla/5.0 (X11; Linux i686; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
        out.println("Accept-Language: ro");
        out.println("Content-Language: en, ase, ru");
        out.println("Vary: Accept-Encoding");
        out.println("Connection: close");
        out.println();


        // Read the response from the server
        BufferedReader responseBufferStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line;
        String allLines = " ";
        while ((line = responseBufferStream.readLine()) != null) {
            allLines = allLines + "\n" + line;
        }

        List<String> imagesURLs = getImagesUrls(allLines);
        var imagesCleanUrl = ImageManipulator.addDomainToURLs(url, imagesURLs);
        var imageDownloader = new SocketImageDownloader();
        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);

        exec.execute(() -> {
            imagesCleanUrl.forEach(img -> {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                var filename = img.toString();
                filename = filename.substring(filename.lastIndexOf("/") + 1);
                System.out.println(filename);
                try {
                    imageDownloader.downloadImage(img, filename);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                semaphore.release();
            });
        });
        exec.shutdown();
        // Close the connections
        out.close();
        responseBufferStream.close();
        socket.close();

    }


    private static List<String> getImagesUrls(String text) {
        String img;
        List<String> images = new ArrayList<>();
        Pattern pImage = Pattern.compile("<img.*src\\s*=\\s*(.*?)(jpg|png|gif)[^>]*?>", Pattern.CASE_INSENSITIVE);
        Matcher mImage = pImage.matcher(text);

        while (mImage.find()) {
            img = mImage.group();
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                var rawURL = m.group(1);
                var cleanURL = rawURL.contains("'") ? rawURL.replaceAll("'", "") : rawURL;
                images.add(cleanURL);
            }
        }
        return images;
    }




}


