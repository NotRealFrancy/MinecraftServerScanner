package org.github.notrealfrancy.mss;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinecraftServerScanner {

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.println("Enter the target host: ");
        String host = reader.nextLine();
        reader.close();
        int timeout = 1000;

        System.out.println("Scanning for Minecraft servers on " + host);

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CompletableFuture<Void>[] futures = new CompletableFuture[65536];

        for (int port = 0; port <= 65535; port++) {
            final int currentPort = port;

            futures[port] = CompletableFuture.runAsync(() -> {
                if (isMinecraftServer(host, currentPort, timeout)) {
                    System.out.println("Minecraft server found on " + host + ":" + currentPort);
                }
            }, executorService);
        }

        CompletableFuture.allOf(futures).join();

        executorService.shutdown();
    }

    private static boolean isMinecraftServer(String host, int port, int timeout) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);

            byte[] data = new byte[]{(byte) 0xFE, (byte) 0x01};
            socket.getOutputStream().write(data);

            byte[] response = new byte[256];
            int bytesRead = socket.getInputStream().read(response);

            boolean isMinecraftServer = bytesRead > 0 && response[0] == (byte) 0xFF;

            socket.close();
            return isMinecraftServer;
        } catch (Exception e) {
            return false;
        }
    }

}
