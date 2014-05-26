package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;

import java.nio.channels.Channel;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public class AndroidNetworkStateManager extends BroadcastReceiver {
    private WarpAccessManager mWarpAccessManager;
    private WifiManager mWifiManager;
    private WifiP2pManager mP2pManager;
    private WifiP2pManager.Channel mChannel;

    public AndroidNetworkStateManager(WarpAccessManager accessManager, WifiManager wifiManager,
                                      WifiP2pManager p2pManager, WifiP2pManager.Channel channel)
    {
        mWarpAccessManager = accessManager;
        mWifiManager = wifiManager;
        mP2pManager = p2pManager;
        mChannel = channel;
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
        else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
        {
            NetworkInfo info = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
            if(p2pInfo != null && p2pInfo.groupFormed)
            {
                mP2pManager.requestGroupInfo(mChannel,new WifiP2pManager.GroupInfoListener() {
                    @Override
                    public void onGroupInfoAvailable(WifiP2pGroup group) {
                        Collection<WifiP2pDevice> devices = group.getClientList();
                    }
                });
            }
        }
    }
}
