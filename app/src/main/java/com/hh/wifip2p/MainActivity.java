package com.hh.wifip2p;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;

import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity<ServerC> extends AppCompatActivity {

    Log log;
    String debug_ = "wifi p2p debug";
    String debug_run = "run info";

    Button btnOnOff,btnDiscover,btnSend;
    TextView readMsg,connectionStatus;
    ListView listView;
    EditText writeMsg;

    WifiManager wifiManager;

    WifiP2pManager mWifiP2pManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mBroadcastReceiver;
    IntentFilter intentFilter;

    List<WifiP2pDevice> peers  = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] devicesArray;



    ServerWifi serverWifi;
    ClientClass clientClass;
    SendReceive sendReceive;

    ClipboardTools clipboardTools;
    ClipboardManager clipboardManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        initialWork();

        exqListener();

    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (MessageOptions.values()[msg.what]) {
                case MESSAGE_READ:
                    break;
                case MESSAGE_CLIP_STRING:{
                    if(msg.obj!=null){
                        String clipRead = (String)msg.obj;
                        readMsg.setText(clipRead);
                    }
                    break;
                }
                case MESSAGE_CLIP_SET_STRING:{
                    if(msg.obj!=null){
                        String clipRead = (String)msg.obj;
                        if(!clipboardTools.check(clipRead)){
                            clipboardTools.setTxt(clipRead);
                            readMsg.setText(clipRead);
                        }
                    }
                }

            }
            return true;
        }
    });




    private void initialWork(){
        btnOnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);

        readMsg = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);

        listView = findViewById(R.id.peerListView);

        writeMsg = findViewById(R.id.writeMsg);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        mChannel = mWifiP2pManager.initialize(this,getMainLooper(),null);

        mBroadcastReceiver = new WiFiDirectBroadcastReceiver(mWifiP2pManager,mChannel,this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        clipboardTools = new ClipboardTools(this,clipboardManager);
    }

    private void exqListener(){
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(debug_run, "wifi=>"+wifiManager.isWifiEnabled());
                if(wifiManager.isWifiEnabled()){
                    Toast.makeText(getApplicationContext(),"Wi-Fi on.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Wi-Fi off，请手动打开Wi-Fi.",Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mChannel==null){
                    Log.d(debug_run, "onClick: mChannel is null");
                }
                mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText(String.valueOf("Discover success."));
                    }
                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText(String.valueOf("Discover failed."));
                        Log.d(debug_run, "onFailure: reason=> "+i);
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = devicesArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getApplicationContext(),"Not connect.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String msg = clipboardTools.getData();
                        if(msg!=null){
                            Log.d(debug_run, "成功获取剪切板=> " + msg);
                            if(!clipboardTools.check(msg)){
                                if(sendReceive!=null) {
                                    try {
                                        sendReceive.sendMessage(msg);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        System.out.println("sendMessage有问题");
                                    }
                                }
                            }
                        }
                    }
                }).start();

            }
        });

    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceNameArray = new String[peerList.getDeviceList().size()];
                devicesArray = new WifiP2pDevice[peerList.getDeviceList().size()];

                int index=0;
                for(WifiP2pDevice device: peerList.getDeviceList()){
                    deviceNameArray[index] = device.deviceName;
                    devicesArray[index] = device;
                    index++;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);

                if(peers.size()==0){
                    Toast.makeText(getApplicationContext(),"No Device Found",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupAddress = wifiP2pInfo.groupOwnerAddress;

            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                connectionStatus.setText("Group owner.");
                serverWifi = new ServerWifi();
                Thread server_t = new Thread(serverWifi);
                server_t.start();

            }else if(wifiP2pInfo.groupFormed){
                connectionStatus.setText("Client.");
                clientClass = new ClientClass(groupAddress);
                clientClass.start();
            }
        }
    };


    ClipboardManager.OnPrimaryClipChangedListener clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            Log.d(debug_run, "onPrimaryClipChanged: 发生变化");
            String txt = clipboardTools.getData();
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        Log.d(debug_, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver,intentFilter);
        Log.d(debug_, "onResume: ");
        Log.i(debug_run, "注册广播");


        Log.d(debug_run, "onResume: "+clipboardTools.getData());

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
        Log.d(debug_, "onPause: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(debug_, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(debug_, "onDestroy: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(debug_, "onRestart: ");
    }




    public class ClientClass extends Thread {
        Socket socket;
        String hostAdd;
        SendReceive receive;
        public ClientClass (InetAddress hostAddress) {
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();

            Log.d(debug_run, "ClientClass: "+hostAddress.getHostAddress());
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
                //socket.setKeepAlive(true);
                sendReceive = new SendReceive(socket,MainActivity.this);
                while(true){
                    String recv_msg = null;
                    try {
                        recv_msg = sendReceive.receiveMessage();
                        if(recv_msg!=null){
                            Log.d("run info", "recv_msg=> "+recv_msg);
                            handler.obtainMessage(MessageOptions.MESSAGE_CLIP_SET_STRING.getValue(),recv_msg).sendToTarget();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}