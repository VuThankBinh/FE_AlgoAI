package com.example.nckh;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;
    private SocketListener listener;

    public interface SocketListener {
        void onOutput(String output);
        void onError(String error);
    }

    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionAttempts = 5;
            options.reconnectionDelay = 1000;
            socket = IO.socket(ApiConfig.getSocketUrl(), options);
        } catch (Exception e) {
            Log.e(TAG, "Error creating socket: " + e.getMessage());
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            socket.connect();
            setupSocketListeners();
        }
    }

    public void disconnect() {
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
    }

    private void setupSocketListeners() {
        socket.on(Socket.EVENT_CONNECT, args -> Log.d(TAG, "Connected to server"));
        socket.on(Socket.EVENT_DISCONNECT, args -> Log.d(TAG, "Disconnected from server"));
        socket.on(Socket.EVENT_CONNECT_ERROR, args -> Log.e(TAG, "Connection error: " + args[0]));

        socket.on("output", args -> {
            if (listener != null) {
                listener.onOutput(args[0].toString());
            }
        });

        socket.on("error", args -> {
            if (listener != null) {
                listener.onError(args[0].toString());
            }
        });
    }

    public void executeCode(String code, String language, String input) {
        if (socket != null && socket.connected()) {
            try {
                JSONObject data = new JSONObject();
                data.put("code", code);
                data.put("language", language);
                data.put("input", input);
                socket.emit("execute", data);
            } catch (JSONException e) {
                Log.e(TAG, "Error creating JSON: " + e.getMessage());
            }
        }
    }

    public void sendInput(String input) {
        if (socket != null && socket.connected()) {
            socket.emit("input", input);
        }
    }
} 