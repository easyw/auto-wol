package net.cmikavac.autowol.services;

import java.util.Calendar;
import java.util.List;

import net.cmikavac.autowol.data.DbProvider;
import net.cmikavac.autowol.data.SharedPreferencesProvider;
import net.cmikavac.autowol.models.DeviceModel;
import net.cmikavac.autowol.utils.TimeUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiReceiver extends BroadcastReceiver {
    private SharedPreferencesProvider mSharedPreferencesProvider = null;
    private DbProvider mDbProvider = null;
    private Context mContext = null;

    public WifiReceiver() {
    }

    private void setContext(Context context) {
        mContext = context;
        mDbProvider = new DbProvider(context);
        mDbProvider.open();
        mSharedPreferencesProvider = new SharedPreferencesProvider(context);
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        setContext(context);
        handleNetworkStateChange(intent);
    }

    private void handleNetworkStateChange(Intent intent) {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            WifiManager wifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State networkState = networkInfo.getState();

            if(networkState == NetworkInfo.State.CONNECTED) {
                onWifiConnected(wifiManager);
            } else if (networkState == NetworkInfo.State.DISCONNECTED) {
                onWifiDisconnected();
            }
        }
    }

    private void onWifiConnected(WifiManager wifiManager) {
        String ssid = wifiManager.getConnectionInfo().getSSID().replace("\"", "");
        mSharedPreferencesProvider.setLastSSID(ssid);
        wakeDevices(ssid);
    }

    private void onWifiDisconnected() {
        String ssid = mSharedPreferencesProvider.getLastSSID();
        mDbProvider.updateDevicesLastDisconnected(ssid, Calendar.getInstance().getTimeInMillis());
        mDbProvider.close();
    }
    
    private void wakeDevices(String ssid) {
        List<DeviceModel> devices = mDbProvider.getDevicesBySSID(ssid);

        for (DeviceModel device : devices) {
            wakeDevice(device);
        }

        mDbProvider.close();
    }
    
    private void wakeDevice(DeviceModel device) {
        Boolean isNowBetweenQuietHours = false;
        Boolean hasIdleTimePassed = true;

        if (device.getQuietHoursFrom() != null) {
            isNowBetweenQuietHours = TimeUtil.isNowBetweenQuietHours(device.getQuietHoursFrom(), device.getQuietHoursTo());
        }
        
        if (device.getIdleTime() != null) {
            hasIdleTimePassed = TimeUtil.hasIdleTimePassed(device.getIdleTime(), device.getLastDisconnected());
        }

        if (device.getQuietHoursFrom() != null) {
            if (!isNowBetweenQuietHours && hasIdleTimePassed) {
                new WolService(mContext).execute(device);
            }
        } else if (hasIdleTimePassed) {
            new WolService(mContext).execute(device);
        }
    }
}
