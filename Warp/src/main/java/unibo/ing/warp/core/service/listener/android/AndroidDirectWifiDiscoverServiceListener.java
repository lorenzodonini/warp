package unibo.ing.warp.core.service.listener.android;

import android.net.wifi.p2p.WifiP2pDevice;
import unibo.ing.warp.core.AndroidInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public class AndroidDirectWifiDiscoverServiceListener extends DefaultWarpServiceListener {
    private WarpAccessManager warpAccessManager;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length == 1)
        {
            warpAccessManager = (WarpAccessManager)values[0];
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        /*Actually don't need to do anything since results are asynchronous and
        automatically provided by the onServiceProgressUpdate callback method */
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        Object [] result = servant.getResult();
        if(result != null && result.length == 1 && result[0] instanceof Collection<?>)
        {
            @SuppressWarnings("unchecked")
            Collection<WifiP2pDevice> p2pDevices = (Collection<WifiP2pDevice>) result[0];
            updateDeviceManager(p2pDevices);
        }
    }

    private void updateDeviceManager(Collection<WifiP2pDevice> p2pDevices)
    {
        if(warpAccessManager == null)
        {
            return;
        }
        IWarpInteractiveDevice [] interactiveDevices = new IWarpInteractiveDevice[p2pDevices.size()];
        int i=0;
        for(WifiP2pDevice p2pDevice: p2pDevices)
        {
            //TODO: beware! no WarpLocation set right now :(
            interactiveDevices[i++] = new AndroidInteractiveDevice(warpAccessManager,
                    new AndroidP2PDevice(null,p2pDevice));
        }
        warpAccessManager.getDeviceManager().addWarpDevices(
                interactiveDevices,AndroidP2PDevice.class,true);
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: implement something cool
    }
}
