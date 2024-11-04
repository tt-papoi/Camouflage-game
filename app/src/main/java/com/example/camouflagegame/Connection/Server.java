package com.example.camouflagegame.Connection;

import static com.example.camouflagegame.Activity.DeployEquipmentActivity.gameInfo;

import android.os.Handler;
import android.os.Message;

import com.example.camouflagegame.Activity.DeployEquipmentActivity;
import com.example.camouflagegame.Activity.GameActivity;
import com.example.camouflagegame.Game.GameInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server extends Thread{
    public ServerSocket serverSocket;
    public Socket socket;
    public final int PORT = 9898;
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;
    private Handler gameHandler;
    private Handler deployHandler;
    public void setGameHandler(Handler gameHandler)
    {
        this.gameHandler = gameHandler;
    }

    public void setDeployHandler(Handler deployHandler) {
        this.deployHandler = deployHandler;
    }

    public void sendInfo(GameInfo info)
    {
        new Thread(() -> {
            try {
                if(socket.isConnected())
                {
                    objectOutputStream.writeObject(info);
                }
            } catch (IOException e) {
                sendAlert();
            }
        }).start();

    }
    public void sendAlert()
    {
        int DISCONNECT = -1;
        if(DeployEquipmentActivity.isRunning)
        {
            Message msg = deployHandler.obtainMessage(DISCONNECT);
            deployHandler.sendMessage(msg);
        }
        if(GameActivity.isRunning)
        {
            Message msg = gameHandler.obtainMessage(DISCONNECT);
            gameHandler.sendMessage(msg);
        }
        try
        {
            this.socket.close();
            this.serverSocket.close();
        }catch (IOException e){};
        Server.this.interrupt();
    }

    @Override
    public void run()
    {
        try {
            serverSocket = new ServerSocket(PORT);
            socket = serverSocket.accept();
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            sendAlert();

        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            while (socket != null && socket.isConnected()) {
                try {
                    GameInfo info = (GameInfo) objectInputStream.readObject();
                    gameInfo.TYPE = info.TYPE;
                    if (info.TYPE == info.SEND_STARTED_MAP) {
                        gameInfo.copyEnemyMap(info.yourMap);
                        gameInfo.isEnemyReadyButtonClicked = true;
                    }
                    if (info.TYPE == info.SEND_MAP) {
                        gameInfo.copyEnemyMap(info.yourMap);
                        gameInfo.posBullet[0] = info.posBullet[0];
                        gameInfo.posBullet[1] = info.posBullet[1];
                        gameInfo.isYourTurn = !info.isYourTurn;
                        Message msg = gameHandler.obtainMessage(info.TYPE);
                        gameHandler.sendMessage(msg);
                    }

                    if (info.TYPE == info.SEND_MESSAGE) {
                        gameInfo.message = info.message;
                        Message msg = gameHandler.obtainMessage(info.TYPE);
                        gameHandler.sendMessage(msg);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    sendAlert();
                    break;
                }
            }
        });

    }
}
