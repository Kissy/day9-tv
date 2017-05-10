package tv.day9.apk.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import tv.day9.apk.worker.DownloadWorker;

public class WiFiBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = WiFiBroadcastReceiver.class.getSimpleName();

    /**
     * @inheritDoc
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            NetworkInfo info = (NetworkInfo)intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.DetailedState state = info.getDetailedState();

            if (state == NetworkInfo.DetailedState.CONNECTED
                    || state == NetworkInfo.DetailedState.DISCONNECTED
                    || state == NetworkInfo.DetailedState.SUSPENDED
                    || state == NetworkInfo.DetailedState.IDLE) {
                DownloadWorker.checkForRunningStatus(context);
            }
        }
    }

}