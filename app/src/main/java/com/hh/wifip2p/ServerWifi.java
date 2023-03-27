package com.hh.wifip2p;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerWifi implements Runnable{

    Socket socket;
    ServerSocket serverSocket;
    InputStream inputStream;
    OutputStream outputStream;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();

            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();



        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
