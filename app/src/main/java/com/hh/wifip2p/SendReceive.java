package com.hh.wifip2p;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SendReceive {

    private InputStream inputStream;
    private OutputStream outputStream;

    public Boolean status = true;
    public SendReceive(Socket socket) throws IOException {
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    public void sendMessage(String msg) {
        if (msg == null) {
            return;
        }
        try {
            byte[] msgBuf = msg.getBytes();
            byte[] lenBuf = ByteBuffer.allocate(4).putInt(msgBuf.length).array();
            outputStream.write(lenBuf);
            outputStream.write(msgBuf);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String recvMessage() {
        String ans = null;
        try {
            byte[] lenBuf = readBytes(4);
            int len = ByteBuffer.wrap(lenBuf).getInt();
            byte[] buf = readBytes(len);
            ans = new String(buf, 0, len);
        } catch (IOException e) {
            status = false;
            close();
        }
        return ans;
    }

    private byte[] readBytes(int length) throws IOException {
        byte[] buf = new byte[length];
        int left = 0;
        while (left < length) {
            int fl = inputStream.read(buf, left, length - left);
            if (fl == -1) {
                throw new EOFException("read faild.");
            }
            left += fl;
        }
        return buf;
    }

    public void close() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
