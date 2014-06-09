package unibo.ing.warp.core.service.listener.android;

import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.Message;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiConnectService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

import java.net.InetAddress;

/**
 * Created by Lorenzo Donini on 6/7/2014.
 */
public final class DirectWifiPingProvideServiceListener extends DefaultWarpServiceListener {
    private Handler mConnectServiceHandler;
    private WarpDeviceManager mDeviceManager;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length >= 1)
        {
            mDeviceManager = (WarpDeviceManager)values[0];
            if(values.length == 2)
            {
                mConnectServiceHandler = (Handler)values[0];
            }
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getResult();
        InetAddress ipv4Address = (InetAddress) result[0];
        String macAddress = (String) result[1];
        IWarpInteractiveDevice [] devices = mDeviceManager.getInteractiveDevicesByClass(AndroidP2PDevice.class);
        boolean found = false;

        for(IWarpInteractiveDevice d : devices)
        {
            IWarpDevice device = d.getWarpDevice();
            WifiP2pDevice p2pDevice = (WifiP2pDevice) device.getAbstractDevice();
            if(p2pDevice.deviceAddress.equals(macAddress))
            {
                device.getDeviceLocation().setIPv4Address(ipv4Address);
                found=true;
                break;
            }
        }
        if(!found)
        {
            return;
        }
        //Notifying in case the handler exists
        if(mConnectServiceHandler != null)
        {
            Message message = new Message();
            message.what=DirectWifiConnectService.PING_RECEIVED;
            mConnectServiceHandler.sendMessage(message);
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        //No progress update
    }

    @Override
    public void onServiceAbort(String message)
    {
        //No abort handling
    }
}
