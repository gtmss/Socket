package org.example.client;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


public class SocketImageDownloader {
    public void downloadImage(String imgUrl, String filename) throws IOException {

        Semaphore semaphore = new Semaphore(2);
        ExecutorService exec = Executors.newFixedThreadPool(4);

        URL url = new URL(imgUrl);
        int port;
        if (url.toString().contains("https")) {
            port = 443;
        } else port = 80;

        Socket socket = new Socket(url.getHost(), port);

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

        InputStream inputStream = socket.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        socket.close();

        byte[] responseBytes = byteArrayOutputStream.toByteArray();
        int headerLength = getHeaderLength(responseBytes);

        byte[] imageBytes = Arrays.copyOfRange(responseBytes, headerLength, responseBytes.length);

        FileOutputStream outputStream1 = new FileOutputStream("images/" + filename);
        outputStream1.write(imageBytes);
        outputStream1.close();
        System.out.println("Image downlaoded: " + filename);
    }

    private static int getHeaderLength(byte[] responseBytes) {
        final byte[] delimiter = "\r\n\r\n".getBytes();
        int headerLength = -1;

        for (int i = 0; i < responseBytes.length - delimiter.length; i++) {
            boolean found = true;
            for (int j = 0; j < delimiter.length; j++) {
                if (responseBytes[i+j] != delimiter[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                headerLength = i + delimiter.length;
                break;
            }
        }

        return headerLength;
    }

}
