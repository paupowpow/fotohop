package com.funk.paupowpow.ducky.p2pkit;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.funk.paupowpow.ducky.config.DuckyConfigs;
import com.funk.paupowpow.ducky.model.data.DuckyDatabaseHandler;
import com.funk.paupowpow.ducky.model.data.Quest;

import java.io.UnsupportedEncodingException;

import ch.uepaa.p2pkit.P2PKitClient;
import ch.uepaa.p2pkit.P2PKitStatusCallback;
import ch.uepaa.p2pkit.StatusResult;
import ch.uepaa.p2pkit.StatusResultHandling;
import ch.uepaa.p2pkit.discovery.InfoTooLongException;
import ch.uepaa.p2pkit.discovery.P2PListener;
import io.realm.RealmResults;


/**
 * Created by paulahaertel on 20.12.16.
 */

public class P2pkitHandler {

    private static final String APP_KEY = DuckyConfigs.APP_KEY;
    private static final String TAG = "4711 P2pkitHandler";

    private static P2pkitHandler instance;
    private static Activity activity;

    private P2pkitHandler(Activity activity) {
        this.activity = activity;
    }

    public static void initialize(Activity activity) {
        if(instance == null) {
            instance = new P2pkitHandler(activity);
        }
        Log.d(TAG, "initialize()");
    }

    public static P2pkitHandler getInstance() {
        return instance;
    }


    public void enableKit() {
        final StatusResult result = P2PKitClient.isP2PServicesAvailable(activity);
        if (result.getStatusCode() == StatusResult.SUCCESS) {
            P2PKitClient client = P2PKitClient.getInstance(activity);
            client.enableP2PKit(mStatusCallback, APP_KEY);
        } else {
            StatusResultHandling.showAlertDialogForStatusError(activity, result);
        }
    }

    private final P2PKitStatusCallback mStatusCallback = new P2PKitStatusCallback() {
        @Override
        public void onEnabled() {
            //ready to start discovery
            Log.d(TAG, "onEnabled()");
            startP2pDiscovery();
        }

        @Override
        public void onSuspended() {
            //p2pkit is temporarily suspended
            Log.d(TAG, "onSuspended()");
        }

        @Override
        public void onResumed() {
            //coming back from a suspended state
            Log.d(TAG, "onResumed()");
        }

        @Override
        public void onDisabled() {
            //p2pkit has been disabled
            Log.d(TAG, "onDisabled()");
        }

        @Override
        public void onError(StatusResult result) {
            //enabling failed, handle statusResult
            Log.d(TAG, "onError()");
        }
    };


    private final P2PListener mP2pDiscoveryListener = new P2PListener() {
        @Override
        public void onP2PStateChanged(int state) {
            Log.d(TAG, "onP2pStateChanged: " +  state);
        }

        @Override
        public void onPeerDiscovered(ch.uepaa.p2pkit.discovery.entity.Peer peer) {
            Log.d(TAG, "onPeerDiscovered: " + peer.getNodeId());
//            Log.d(TAG, "with info: " + new String(peer.getDiscoveryInfo()));
            showToast("Peer discovered");
            transferQuest();
            showToast(new String(peer.getDiscoveryInfo()));
        }

        @Override
        public void onPeerLost(ch.uepaa.p2pkit.discovery.entity.Peer peer) {
            Log.d(TAG, "onPeerLost: " + peer.getNodeId());
            showToast("Peer lost");
        }

        @Override
        public void onPeerUpdatedDiscoveryInfo(ch.uepaa.p2pkit.discovery.entity.Peer peer) {
            Log.d(TAG, "onPeerUpdatedDiscoveryInfo: " + peer.getNodeId() + " with new info: " + new String(peer.getDiscoveryInfo()));
        }

        @Override
        public void onProximityStrengthChanged(ch.uepaa.p2pkit.discovery.entity.Peer peer) {
            Log.d(TAG, "onProximityStrengthChanged: " + peer.getNodeId() + " changed proximity strength: " +  peer.getProximityStrength());
        }
    };

    public void startP2pDiscovery() {
        Log.d(TAG, "startP2pDiscovery()");

        P2PKitClient.getInstance(activity)
                .getDiscoveryServices()
                .addP2pListener(mP2pDiscoveryListener);
    }

    public void stopP2pDiscovery() {
        Log.d(TAG, "stopP2pDiscovery()");
        P2PKitClient.getInstance(activity)
                .getDiscoveryServices()
                .removeP2pListener(mP2pDiscoveryListener);
    }

    private void tryEncoding() {
        String questName = "quest name";
        String original = new String("A" + "\u00ea" + "\u00f1" + "\u00fc" + "C");

        try {
            byte[] utf8Bytes = original.getBytes("UTF8");
            byte[] defaultBytes = original.getBytes();

            String roundTrip = new String(utf8Bytes, "UTF8");

            Log.d(TAG, "roundTrip = " + roundTrip);
            printBytes(utf8Bytes, "utf8Bytes");
            printBytes(defaultBytes, "defaultBytes");

        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
    }

    private void printBytes(byte[] array, String name) {
        for (int k = 0; k < array.length; k++) {
            Log.d(TAG, name + "[" + k + "] = " + "0x" +
                    UnicodeFormatter.byteToHex(array[k]));
        }
    }

    private void showToast(String text) {
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(activity, text, duration);
        toast.show();
    }

    private void transferQuest() {
        DuckyDatabaseHandler dbh = DuckyDatabaseHandler.getInstance();
        RealmResults<Quest> results = dbh.getQuests();
        Log.d(TAG, results.toString()   );
        if(!results.isEmpty()) {
            String questText = results.first().getQuestText();
            try {
                byte[] questBytes = questText.getBytes("UTF8");
                printBytes(questBytes, "utf8Bytes");
                //transfer
                publishP2pDiscoveryInfo(questBytes);
            } catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
    }

    private void publishP2pDiscoveryInfo(byte[] data) {
        Logger.i("P2PKitClient", "Publish discovery info");
        try {
            P2PKitClient.getInstance(activity).getDiscoveryServices().setP2pDiscoveryInfo(data);
        } catch (InfoTooLongException e) {
            Logger.e("P2PKitClient", "The discovery info is too long: " + ((data != null) ? data.length : "null") + " bytes");
        }
    }

}