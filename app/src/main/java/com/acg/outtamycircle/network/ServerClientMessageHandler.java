package com.acg.outtamycircle.network;

import android.support.annotation.NonNull;
import android.support.v4.util.Pools;
import android.util.Log;

import com.acg.outtamycircle.GameStatus;
import com.acg.outtamycircle.network.googleimpl.GoogleRoom;
import com.acg.outtamycircle.network.googleimpl.MessageReceiver;
import com.acg.outtamycircle.network.googleimpl.MyGoogleSignIn;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ServerClientMessageHandler implements NetworkMessageHandler {
    private static final int MAX_BUFFER_SIZE = 400; //TODO
    private static final byte ENDING_CHAR = 127;
    private static final int DEFAULT_CAPACITY = 30;

    private RealTimeMultiplayerClient client;
    private MessageReceiver first, second;
    private Pools.Pool<GameMessage> pool;
    private String roomId;
    private List<String> players;
    private Room room;

    //TODO visibility
    byte[] buffer = new byte[MAX_BUFFER_SIZE];
    private int currentBufferSize = 0;

    public ServerClientMessageHandler setRoom(Room room) {
        this.room = room;
        this.roomId = room.getRoomId(); //TODO
        return this;
    }

    public ServerClientMessageHandler setRoomId(String roomId){
        this.roomId = roomId;
        return this;
    }

    public ServerClientMessageHandler setClient(RealTimeMultiplayerClient client){
        this.client = client;
        return this;
    }

    public ServerClientMessageHandler setPlayerList(List<String> players){
        this.players = players;
        return this;
    }

    //TODO change
    public ServerClientMessageHandler setReceivers(MessageReceiver first, MessageReceiver second){
        this.first = first;
        this.second = second;
        return this;
    }

    final String TAG = "JUAN";

    private final RealTimeMultiplayerClient.ReliableMessageSentCallback mywtf = new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
        @Override
        public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
            Log.d(TAG, "RealTime message sent");
            Log.d(TAG, "  statusCode: " + statusCode);
            Log.d(TAG, "  tokenId: " + tokenId);
            Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
        }
    };

    @Override
    public void sendReliable(final String playerId) {
        Log.d("PEPPE", Arrays.toString(buffer));
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("PEPPE","room id " + GoogleRoom.mRoomId);
                final Task<Integer> sendTask = GoogleRoom.getInstance().getRealTimeMultiplayerClient()
                        .sendReliableMessage(buffer, GoogleRoom.mRoomId, playerId, mywtf);
                //TODO Check
                sendTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Impossibile inviare il messaggio " + e);
                    }
                });
                sendTask.addOnCompleteListener(new OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(@NonNull Task<Integer> task) {
                        Log.d(TAG, "Completato");
                        synchronized (sendTask) {
                            sendTask.notify();
                        }
                    }
                });
                sendTask.addOnCanceledListener(new OnCanceledListener() {
                       @Override
                       public void onCanceled() {
                           Log.d("JUAN", "Canceled");
                       }
                });
            }
        }).start();


/*        synchronized (sendTask){
            while(!sendTask.isComplete()){
                try {
                    sendTask.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }*/

    }

    @Override
    public void sendUnreliable(String player) {
        client.sendUnreliableMessage(buffer, roomId, player);
    }

    @Override
    public void broadcastReliable(){
        //TODO
        Log.d("PEPPE","sono " + GoogleRoom.mMyId);
        for(Participant player: GoogleRoom.mParticipants) {
            String playerId = player.getParticipantId();
            Log.d("PEPPE", "manderei a " + playerId);
            if(!playerId.equals(GoogleRoom.mMyId)) {
                Log.d("PEPPE", "mando a " + playerId);
                sendReliable(playerId);
            }
        }
    }

    @Override
    public void broadcastUnreliable() {
        final Task<Void> task = client.sendUnreliableMessageToOthers(buffer,GoogleRoom.mRoomId);
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("JUAN","failure " + e);
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d("JUAN", "complete and success? " + task.isSuccessful());
            }
        });
    }

    @Override
    public void putInBuffer(GameMessage message) {
        //TODO se ci sono troppi messaggi? RuntimeException?
        GameMessage.Type type = message.getType();
        message.putInBuffer(buffer, currentBufferSize);
        currentBufferSize += type.length;
        buffer[currentBufferSize] = ENDING_CHAR;
    }

    @Override
    public synchronized Iterable<GameMessage> getMessages() {
        MessageReceiver tmp = first;
        second.clear();
        first = second;
        second = tmp;
        return tmp.getMessages();
    }

    @Override
    public synchronized void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
        byte[] messageData = realTimeMessage.getMessageData();
        int cursor = 0;
        Log.d("ABCDARIO","HO ricevuto qualcosa zzu");

        while( cursor < messageData.length && messageData[cursor]!=ENDING_CHAR) {
            GameMessage gameMessage = pool.acquire();
            gameMessage.setSender(realTimeMessage.getSenderParticipantId());
            int length = GameMessage.Type.values()[messageData[cursor]].length;
            gameMessage.copyBuffer(messageData, cursor, cursor + length - 1); //TODO check
            first.storeMessage(gameMessage);
            cursor += length;
        }
    }

    @Override
    public void clearBuffer(){
        currentBufferSize = 0;
        buffer[0] = ENDING_CHAR;
    }
}
