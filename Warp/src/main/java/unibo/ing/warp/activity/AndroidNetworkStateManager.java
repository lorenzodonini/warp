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
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.IWarpService.ServiceOperation;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.p2p.DirectWifiPingService;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.service.launcher.android.DirectWifiDiscoverLauncher;
import unibo.ing.warp.core.service.launcher.android.DirectWifiPingLauncher;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;

import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public class AndroidNetworkStateManager extends BroadcastReceiver {
    private WarpAccessManager mWarpAccessManager;
    private WifiManager mWifiManager;
    private WifiP2pManager mP2pManager;
    private String mMasterKey;
    private WifiP2pManager.Channel mChannel;

    public AndroidNetworkStateManager(WarpAccessManager accessManager, WifiManager wifiManager,
                                      WifiP2pManager p2pManager, String masterKey)
    {
        mWarpAccessManager = accessManager;
        mWifiManager = wifiManager;
        mP2pManager = p2pManager;
        mMasterKey = masterKey;
        mChannel = (WifiP2pManager.Channel) WarpResourceLibrary.getInstance().getResource(mMasterKey,
                DirectWifiDiscoverLauncher.P2P_CHANNEL_KEY);
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
            if(("\""+device.getWarpDevice().getDeviceName()+"\"").equals(ssid))
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
            if(info != null && info.getState() == NetworkInfo.State.CONNECTED)
            {
                WifiP2pInfo p2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                if(p2pInfo != null && p2pInfo.groupFormed)
                {
                    mP2pManager.requestGroupInfo(mChannel,new WarpGroupInfoListener(p2pInfo));
                }
            }
            else if(info != null && info.getState() == NetworkInfo.State.DISCONNECTED)
            {
                mWarpAccessManager.getDeviceManager().removeBlackListDevice(AndroidWifiHotspot.class,null);
                IWarpInteractiveDevice p2pDevices [] = mWarpAccessManager.
                        getDeviceManager().getInteractiveDevicesByClass(AndroidP2PDevice.class);
                if(p2pDevices == null)
                {
                    return;
                }
                for (IWarpInteractiveDevice p2pDevice : p2pDevices)
                {
                    if (p2pDevice.getDeviceStatus()== IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED)
                    {
                        ((DefaultWarpInteractiveDevice)p2pDevice).setDeviceStatus(
                                IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED);
                    }
                }
            }
        }
    }

    private class WarpGroupInfoListener implements WifiP2pManager.GroupInfoListener {
        private WifiP2pInfo mInfo;

        public WarpGroupInfoListener(WifiP2pInfo info)
        {
            mInfo = info;
        }

        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group)
        {
            WarpDeviceManager deviceManager = mWarpAccessManager.getDeviceManager();
            deviceManager.addBlackListDevice(AndroidWifiHotspot.class,
                    group.getNetworkName());
            WarpResourceLibrary.getInstance().setResource(DirectWifiPingLauncher.RES_P2P_GROUP,
                    mMasterKey,group);

            //Updating group OWNER with Location, Status
            if(group.isGroupOwner())
            {
                //I am group owner, therefore I don't know any other IP address.
                //The group owner hopes to get pings from other peers, in order to
                //obtain their IP address.
            }
            else
            {
                //I'm not group owner, I know the owner's IP address but not the other IPs.
                WifiP2pDevice owner = group.getOwner();
                DefaultWarpInteractiveDevice ownerDevice = (DefaultWarpInteractiveDevice)
                        deviceManager.getWarpDeviceByName(owner.deviceName,AndroidP2PDevice.class);
                if(ownerDevice == null)
                {
                    WarpLocation location = new WarpLocation(mInfo.groupOwnerAddress);
                    ownerDevice = new AndroidInteractiveDevice(mWarpAccessManager,
                            new AndroidP2PDevice(location,owner));
                    ownerDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
                }
                else
                {
                    ownerDevice.getWarpDevice().updateAbstractDevice(owner);
                    ownerDevice.getWarpDevice().getDeviceLocation().setIPv4Address(mInfo.groupOwnerAddress);
                    ownerDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
                }
                startDirectWifiPing(ownerDevice); //Need to ping the group owner, so he can know our IP
            }

            //Adding or updating other clients
            Collection<WifiP2pDevice> devices = group.getClientList();
            if(devices != null && devices.size() > 0)
            {
                DefaultWarpInteractiveDevice newDevices [] = new DefaultWarpInteractiveDevice[devices.size()];
                int i=0;
                for(WifiP2pDevice device: devices)
                {
                    newDevices[i] = new AndroidInteractiveDevice(mWarpAccessManager,
                            new AndroidP2PDevice(null,device));
                    newDevices[i++].setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
                }
                mWarpAccessManager.getDeviceManager().addWarpDevices(newDevices,AndroidP2PDevice.class,true);
            }
        }
    }

    private void startDirectWifiPing(IWarpInteractiveDevice device)
    {
        IWarpEngine warpDrive = mWarpAccessManager.getLocalDevice().getWarpEngine();
        WarpServiceInfo pingDescriptor = WarpUtils.getWarpServiceInfo(DirectWifiPingService.class);
        if(pingDescriptor == null)
        {
            return;
        }
        IWarpServiceLauncher launcher = warpDrive.getLauncherForService(pingDescriptor.name());
        launcher.initializeService(WarpResourceLibrary.getInstance(), mMasterKey, ServiceOperation.CALL);
        IWarpServiceListener listener = warpDrive.getListenerForService(pingDescriptor.name(),
                launcher.getServiceListenerParameters(device, ServiceOperation.CALL), ServiceOperation.CALL);
        warpDrive.callPushService(pingDescriptor.name(),device.getWarpDevice(),listener,null,
                launcher.getServiceParameters(device, ServiceOperation.CALL),
                launcher.getServiceRemoteParameters());
    }
}
