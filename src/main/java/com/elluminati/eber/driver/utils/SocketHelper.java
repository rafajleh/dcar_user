package com.elluminati.eber.driver.utils;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static com.elluminati.eber.driver.BuildConfig.BASE_URL;

public class SocketHelper {
    private static SocketHelper socketHelper;
    private Socket socket;
    public static String UPDATE_LOCATION = "update_location";
    public static String JOIN_TRIP = "join_trip";
    public static String TRIP_DETAIL_NOTIFY = "trip_detail_notify";
    private SocketListener socketListener;

    public boolean isConnected() {
        return socket.connected();
    }

    private SocketHelper() {
        try {
            socket = IO.socket(BASE_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static SocketHelper getInstance() {
        if (socketHelper == null) {
            socketHelper = new SocketHelper();
        }
        return socketHelper;

    }

    public void setSocketConnectionListener(SocketListener socketListener) {
        this.socketListener = socketListener;
    }

    public Socket getSocket() {
        return socket;
    }

    public void socketConnect() {
        if (!socket.connected()) {
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.connect();
        }
    }

    public void socketDisconnect() {
        if (socket.connected()) {
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        }
    }


    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            AppLog.Log(SocketHelper.class.getSimpleName(), "Socket Connected");
            if (socketListener != null) {
                socketListener.onSocketConnect();
            }


        }
    };


    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            AppLog.Log(SocketHelper.class.getSimpleName(), "Socket Disconnected");
            if (socketListener != null) {
                socketListener.onSocketDisconnect();
            }
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

            AppLog.Log(SocketHelper.class.getSimpleName(), "Socket ConnectError");

        }
    };


    public interface SocketListener {

        void onSocketConnect();
        void onSocketDisconnect();

    }


}

