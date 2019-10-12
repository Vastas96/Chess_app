package com.example.vytenis.chess_app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class SocketService extends Service {
    public static final String MY_IP = "192.168.0.37"; //my lan IP address
    public static final String MIF_IP = "172.24.4.54"; //MIF lan IP address
    public static final int SERVERPORT = 8080;
    private String SERVERIP;
    private PrintWriter out;
    private BufferedReader in;
    private Socket socket;
    private InetAddress serverAddr;
    private String incomingMessage;
    private String move;

    private String username = null;
    private String otherUsername = "test";
    private String color;
    private boolean gotResponse = false;
    private boolean success;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    private final IBinder myBinder = new LocalBinder();
    //TCPClient mTcpClient = new TCPClient();

    public class LocalBinder extends Binder {
        public SocketService getService() {
            System.out.println("I am in Localbinder ");
            return SocketService.this;

        }
    }

    @Override
    public void onCreate() {
        //System.out.println("Got server IP: "+getIpAddress());
        SERVERIP = MY_IP;
        super.onCreate();
        System.out.println("I am in on create");
    }

    public void IsBoundable(){
        Toast.makeText(this,"I bind like butter", Toast.LENGTH_LONG).show();
    }
    /*
    public static String getIpAddress() {
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&&inetAddress instanceof Inet4Address) {
                        String ipAddress=inetAddress.getHostAddress().toString();
                        Log.e("IP address",""+ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.v(TAG, "Socket  exception happened");
        }
        return null;
    }

    public String getCurrentSsid(Context context) {

        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
            // Get WiFi status MARAKANA
            WifiInfo info = wifiManager.getConnectionInfo();
            String textStatus = "";
            textStatus += "\n\nWiFi Status: " + info.toString();
            String BSSID = info.getBSSID();
            String MAC = info.getMacAddress();

            List<ScanResult> results = wifiManager.getScanResults();
            ScanResult bestSignal = null;
            int count = 1;
            String etWifiList = "";
            for (ScanResult result : results) {
                etWifiList += count++ + ". " + result.SSID + " : " + result.level + "\n" +
                        result.BSSID + "\n" + result.capabilities +"\n" +
                        "\n=======================\n";
            }
            Log.v(TAG, "from SO: \n"+etWifiList);

            // List stored networks
            List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
            for (WifiConfiguration config : configs) {
                textStatus+= "\n\n" + config.toString();
            }
            Log.v(TAG,"from marakana: \n"+textStatus);
        }
        return ssid;
    }
    */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            System.out.println("in sendMessage "+message);
            out.println(message);
            out.flush();
        }
    }

    public void sendMove(String move){
        System.out.println("Sending move "+move);
        sendMessage("1"+move);
    }

    public boolean waitForAuthorization(){
        while(!gotResponse){
            SystemClock.sleep(200);
        }
        gotResponse = false;
        return success;
    }

    public String waitForMove(){
        while(!gotResponse){
            SystemClock.sleep(200);
        }
        gotResponse = false;
        System.out.println("returning waiting task!");
        return move;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }
    public String getOtherUsername(){
        return otherUsername;
    }
    public String getColor(){
        return color;
    }
    @Override
    public int onStartCommand(Intent intent,int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        System.out.println("I am in on start");
        //  Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        return START_STICKY;
    }


    class connectSocket implements Runnable {

        @Override
        public void run() {

            try {
                //here you must put your computer's IP address.
                serverAddr = InetAddress.getByName(SERVERIP);
                Log.e("TCP Client", "C: Connecting...");
                //create a socket to make the connection with the server

                socket = new Socket(serverAddr, SERVERPORT);

                try {


                    //send the message to the server
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    Log.e("TCP Client", "C: Sent.");

                    Log.e("TCP Client", "C: Done.");

                    while(true){
                        incomingMessage = in.readLine();
                        if (incomingMessage != null) {
                            handleMessage(incomingMessage);
                            System.out.println(incomingMessage);
                        }
                        incomingMessage = null;
                    }

                }
                catch (Exception e) {

                    Log.e("TCP", "S: Error", e);

                }
            } catch (Exception e) {

                Log.e("TCP", "C: Error", e);

            }

        }

    }

    void handleMessage(String msg){
        System.out.println("Handling message "+msg);
        if(msg.contains("This username already exist")){
            success = false;
            gotResponse = true;
        }
        else if(msg.contains("Connection established!")){
            success = true;
            gotResponse = true;
        }
        else if(msg.contains("You were paired with")){
            System.out.println("got response!");
            otherUsername = msg.substring(msg.indexOf("h") + 1, msg.indexOf("@"));
            System.out.println(otherUsername);
            color = msg.substring(msg.length() - 6, msg.length() - 1);
            System.out.println(color);
            success = true;
            gotResponse = true;
        }
        else if(msg.startsWith("1")){
            System.out.println("got move response!");
            move = msg.substring(1, msg.length());
            gotResponse = true;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            socket.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        socket = null;
    }
}
