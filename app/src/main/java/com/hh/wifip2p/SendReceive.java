package com.hh.wifip2p;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

public class SendReceive implements Runnable{
    Socket mSocket;

    DataInputStream inputStream;

    DataOutputStream outputStream;
    MainActivity mainActivity_;
    byte[] buf;

    public byte[] getBuf() {
        return buf;
    }

    public SendReceive(Socket socket,MainActivity mainActivity) throws IOException {
        mSocket = socket;
        mainActivity_ = mainActivity;
        inputStream = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));
        outputStream = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));

    }

    public void sendMessage(String msg) throws IOException {
        int ans = msg.getBytes().length;
        outputStream.writeInt(ans);
        outputStream.write(msg.getBytes());
        outputStream.flush();
    }

    public String receiveMessage() throws IOException {
        int len = inputStream.readInt();
        buf = new byte[len];
        int readLen = 0;
        while(readLen<len){
            readLen+=inputStream.read(buf,readLen,len-readLen);
        }
        return new String(buf,0,len);
    }

    @Override
    public void run() {
        while(mSocket.isConnected()){
            String recv_msg = null;
            try {
                recv_msg = receiveMessage();
                mainActivity_.handler.obtainMessage(MessageOptions.MESSAGE_CLIP_SET_STRING.getValue(),recv_msg).sendToTarget();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}