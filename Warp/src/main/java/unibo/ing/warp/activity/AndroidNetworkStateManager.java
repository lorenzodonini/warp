package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;

import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public class AndroidNetworkStateManager extends BroadcastReceiver {
    private WarpAccessManager mWarpAccessManager;
    private WifiManager mWifiManager;
    private WifiP2pManager mP2pManager;

    public AndroidNetworkStateManager(WarpAccessManager accessManager, WifiManager wifiManager,
                                      WifiP2pManager p2pManager)
    {
        mWarpAccessManager = accessManager;
        mWifiManager = wifiManager;
        mP2pManager = p2pManager;
    }

    private void disconnectWifiHotspot()
    {
        IWarpInteractiveDevice [] devices = mWarpAccessManager.
                getDeviceManager().getInteractiveDevicesByClass(AndroidWifiHotspot.class);
        if(devices == null)
        {
            return;
        }
        for(IWarpInteractiveDevice device : devices)
        {
            if(device.getDeviceStatus()== IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED)
            {
                ((DefaultWarpInteractiveDevice)device).setDeviceStatus(
                        IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED);
                return;
            }
        }
    }

    private void connectWifiHotspot(String ssid)
    {
        IWarpInteractiveDevice [] devices = mWarpAccessManager.
                getDeviceManager().getInteractiveDevicesByClass(AndroidWifiHotspot.class);
        if(devices == null)
        {
            return;
        }
        for(IWarpInteractiveDevice device : devices)
        {
            if(("\""+device.getWarpDevice().getDeviceName()+"\"").equals(ssid) &&
                    device.getDeviceStatus()== IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED)
            {
                ((DefaultWarpInteractiveDevice)device).setDeviceStatus(
                        IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
                return;
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action == null)
        {
            return;
        }
        if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
        {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(info != null)
            {
                if(info.getState()== NetworkInfo.State.CONNECTED)
                {
                    connectWifiHotspot(info.getExtraInfo());
                }
                else if(info.getState()== NetworkInfo.State.DISCONNECTED)
                {
                    disconnectWifiHotspot();
                }
            }
        }
    }
}
