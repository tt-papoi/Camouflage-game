package com.example.camouflagegame.Activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.camouflagegame.Connection.Client;
import com.example.camouflagegame.R;
import com.example.camouflagegame.Connection.Server;
import com.example.camouflagegame.Connection.WiFiDirectBroadcastReceiver;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class LobbyActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION = 1001;
    private static final int PERMISSIONS_REQUEST_CODE_NEARBY_DEVICE = 1000;
    public static boolean isRunning = false;
    private ConstraintLayout msgLayout;
    private ImageButton btnExitCreateRoom, btnExitJoinRoom, btnJoinRoom, btnCreateRoom, btnBack, btnConnect;
    private Spinner spinnerDeviceList;
    private ArrayAdapter<String> spinnerDeviceListAdapter;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private List<WifiP2pDevice> peers = new ArrayList<>();
    private ArrayList<String> deviceNameArray = new ArrayList<>();

    private WifiP2pDevice pickedDevice = null;
    public Thread discoverPeersThread = null;
    public static Server server = null;
    public static Client client = null;
    public static boolean isHost = true;
    private Thread waitingThread;
    private Boolean isCreateRoom = false;
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(discoverPeersThread != null)
            {
                discoverPeersThread.interrupt();
                discoverPeersThread = null;
            }
            if(waitingThread != null)
            {
                waitingThread.interrupt();
                waitingThread = null;
            }
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                server = new Server();
                server.start();
                isHost = true;
                Intent intent = new Intent(LobbyActivity.this, DeployEquipmentActivity.class);
                startActivity(intent);
            } else if (wifiP2pInfo.groupFormed) {
                Intent intent = new Intent(LobbyActivity.this, DeployEquipmentActivity.class);
                isHost = false;
                client = new Client(groupOwnerAddress);
                client.start();
                startActivity(intent);
            }
            stopDiscoverPeers();
        }
    };
    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peerList.getDeviceList());
            if (!refreshedPeers.equals(peers)) {
                peers.clear();
                peers.addAll(refreshedPeers);

                deviceNameArray.clear();

                if(!isCreateRoom)
                {
                    if(waitingThread != null)
                    {
                        waitingThread.interrupt();
                        waitingThread = null;
                    }
                    msgLayout.setBackgroundResource(R.drawable.devices_list_layout);
                }

                for(int i = 0; i < peers.size(); i++)
                {
                    deviceNameArray.add(peers.get(i).deviceName);
                }
                spinnerDeviceListAdapter.notifyDataSetChanged();
            }

            if (peers.size() == 0) {
                deviceNameArray.clear();
                pickedDevice = null;
                if(waitingThread != null)
                {
                    waitingThread.interrupt();
                    waitingThread = null;
                }
                startWaitingThread();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lobby_screen);
        initialize();
        clearConnection();
    }


    private void initialize() {
        msgLayout = findViewById(R.id.code_layout);
        msgLayout.setVisibility(View.INVISIBLE);

        spinnerDeviceList = findViewById(R.id.spinnerDeviceList);
        spinnerDeviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, deviceNameArray);
        spinnerDeviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeviceList.setAdapter(spinnerDeviceListAdapter);

        btnExitJoinRoom = findViewById(R.id.btnExitJoinRoom);
        btnExitCreateRoom = findViewById(R.id.btnExitCreateRoom);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnConnect = findViewById(R.id.btnConnect);

        btnBack = findViewById(R.id.btnBack_LobbyScreen);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        deletePersistentGroups();

        handleClickedCreateRoomButton();

        handleClickedJoinRoomButton();

        handleClickedBackToMainButton();

        handleClickedExitCreateRoomButton();

        handleClickedExitJoinRoomButton();

        handleClickedItemSpinnerDeviceList();

        handleClickedConnectButton();

        checkPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearConnection();
        if(client != null)
        {
            if(client.socket != null)
            {
                try {
                    client.socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            client = null;
        }
        if(server != null)
        {
            if(server.socket != null)
            {
                try {
                    server.socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            server = null;
        }
    }
    private void handleClickedItemSpinnerDeviceList()
    {
        spinnerDeviceList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(peers.size() != 0)
                {
                    pickedDevice = peers.get(position);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void handleClickedConnectButton()
    {
        btnConnect.setOnClickListener(v -> {
            if(!checkPermission())
            {
                return;
            }
            if(pickedDevice == null)
            {
                return;
            }
            deletePersistentGroups();

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = pickedDevice.deviceAddress;
            config.groupOwnerIntent = 0;
            config.wps.setup = WpsInfo.PBC;

            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(LobbyActivity.this, "Connect success",
                            Toast.LENGTH_SHORT).show();
                    startWaitingThread();
                    spinnerDeviceList.setVisibility(View.INVISIBLE);
                    if(discoverPeersThread != null)
                    {
                        discoverPeersThread.interrupt();
                        discoverPeersThread = null;
                    }
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(LobbyActivity.this, "Connect failed. Exit and join room again.",
                            Toast.LENGTH_SHORT).show();
                    if(discoverPeersThread != null)
                    {
                        discoverPeersThread.interrupt();
                        discoverPeersThread = null;
                    }
                    clearConnection();
                    createDiscoverPeersThread();
                }
            });
        });
    }
    public void startWaitingThread()
    {
        waitingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long time = 0;
                while(!Thread.currentThread().isInterrupted())
                {
                    if(time % 3 == 0)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msgLayout.setBackgroundResource(R.drawable.waiting_connection_layout3);
                            }
                        });
                    }
                    if(time % 3 == 1)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msgLayout.setBackgroundResource(R.drawable.waiting_connection_layout1);
                            }
                        });
                    }
                    if(time % 3 == 2)
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                msgLayout.setBackgroundResource(R.drawable.waiting_connection_layout2);
                            }
                        });
                    }
                    time += 1;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        });
        waitingThread.start();
    }
    private void handleClickedCreateRoomButton() {
        btnCreateRoom.setOnClickListener(v -> {
            if(!checkPermission())
            {
                return;
            }
            isCreateRoom = true;
            startWaitingThread();
            deletePersistentGroups();
            createDiscoverPeersThread();
            msgLayout.setVisibility(View.VISIBLE);
            btnExitCreateRoom.setVisibility(View.VISIBLE);
            btnCreateRoom.setVisibility(View.INVISIBLE);
            btnJoinRoom.setVisibility(View.INVISIBLE);
            spinnerDeviceList.setVisibility(View.INVISIBLE);

        });
    }
    private void handleClickedJoinRoomButton() {
        btnJoinRoom.setOnClickListener(v -> {
            if(!checkPermission())
            {
                return;
            }

            if(discoverPeersThread != null)
            {
                discoverPeersThread.interrupt();
                discoverPeersThread = null;
                clearConnection();
            }
            if(waitingThread != null)
            {
                waitingThread.interrupt();
                waitingThread = null;
            }
            isCreateRoom = false;
            spinnerDeviceListAdapter.clear();
            spinnerDeviceListAdapter.notifyDataSetChanged();
            deletePersistentGroups();
            createDiscoverPeersThread();
            msgLayout.setVisibility(View.VISIBLE);
            spinnerDeviceList.setVisibility(View.VISIBLE);
            btnExitJoinRoom.setVisibility(View.VISIBLE);
            btnJoinRoom.setVisibility(View.INVISIBLE);
            btnCreateRoom.setVisibility(View.INVISIBLE);
            btnConnect.setVisibility(View.VISIBLE);
            startWaitingThread();
        });
    }
    private void handleClickedBackToMainButton() {
        btnBack.setOnClickListener(v -> {
            if(discoverPeersThread != null)
            {
                discoverPeersThread.interrupt();
                discoverPeersThread = null;
            }
            if(waitingThread != null)
            {
                waitingThread.interrupt();
                waitingThread = null;
            }
            clearConnection();
            finish();
        });
    }
    private void handleClickedExitCreateRoomButton() {
        btnExitCreateRoom.setOnClickListener(v -> {
            if(discoverPeersThread != null)
            {
                discoverPeersThread.interrupt();
                discoverPeersThread = null;
            }
            if(waitingThread != null)
            {
                waitingThread.interrupt();
                waitingThread = null;
            }
            clearConnection();
            msgLayout.setVisibility(View.INVISIBLE);
            btnExitCreateRoom.setVisibility(View.INVISIBLE);
            btnCreateRoom.setVisibility(View.VISIBLE);
            btnJoinRoom.setVisibility(View.VISIBLE);
        });
    }
    private void handleClickedExitJoinRoomButton() {
        btnExitJoinRoom.setOnClickListener(v -> {
            if(discoverPeersThread != null)
            {
                discoverPeersThread.interrupt();
                discoverPeersThread = null;
            }
            clearConnection();
            msgLayout.setVisibility(View.INVISIBLE);
            btnExitJoinRoom.setVisibility(View.INVISIBLE);
            btnCreateRoom.setVisibility(View.VISIBLE);
            btnJoinRoom.setVisibility(View.VISIBLE);
            btnConnect.setVisibility(View.INVISIBLE);
        });
    }

    // Display a wifi requested message box
    private void buildAlertMessageNoWifi() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your WiFi seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }
    // Check wifi is on or off
    public boolean statusCheckWifiState() {
        final WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!manager.isWifiEnabled())
        {
            buildAlertMessageNoWifi();
        }
        return manager.isWifiEnabled();
    }
    // Check gps is on or off
    public boolean statusCheckLocation() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            buildAlertMessageNoGps();
        }
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    // Display a GPS requested message box
    private void buildAlertMessageNoGps()        {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("No", (dialog, id) -> dialog.cancel());
        final AlertDialog alert = builder.create();
        alert.show();
    }
    // Request permission ACCESS_FINE_LOCATION
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            else
            {
                Toast.makeText(LobbyActivity.this, "Require permission location", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_CODE_NEARBY_DEVICE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
            else
            {
                Toast.makeText(LobbyActivity.this, "Require nearby device permission", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    public boolean checkPermission()
    {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LobbyActivity.PERMISSIONS_REQUEST_CODE_ACCESS_FINE_LOCATION);
        }
        if (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissions(new String[]{Manifest.permission.NEARBY_WIFI_DEVICES},
                        LobbyActivity.PERMISSIONS_REQUEST_CODE_NEARBY_DEVICE);
            }
        }
        if(!statusCheckLocation())
        {
            return false;
        }
        return statusCheckWifiState();
    }
    public void discoverPeers()
    {
        if(!checkPermission())
        {
            return;
        }
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int i) {
            }
        });
    }
    public void clearConnection()
    {
        if(waitingThread != null)
        {
            waitingThread.interrupt();
            waitingThread = null;
        }
        stopDiscoverPeers();
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
        manager.cancelConnect(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }
    public void stopDiscoverPeers()
    {
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int i) {

            }
        });
    }
    public void createDiscoverPeersThread()
    {
        discoverPeersThread = new Thread(() -> {
            Log.d("deployEquipment", "start");
            while(!Thread.currentThread().isInterrupted())
            {
                discoverPeers();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    break;
                }
            }

            Log.d("deployEquipment", "stop thread");
        });
        discoverPeersThread.start();
    }
    private void deletePersistentGroups(){
        try {
            Method[] methods = WifiP2pManager.class.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("deletePersistentGroup")) {
                    // Delete any persistent group
                    for (int netId = 0; netId < 32; netId++) {
                        method.invoke(manager, channel, netId, null);
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}