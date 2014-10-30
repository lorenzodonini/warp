package unibo.ing.warp.core.service.listener;

import unibo.ing.warp.core.AndroidInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.device.android.AndroidNetworkDevice;
import unibo.ing.warp.core.service.IWarpService;
import java.net.InetAddress;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 7/30/2014.
 */
public class WarpBeaconServiceListener extends DefaultWarpServiceListener {
    private WarpAccessManager mAccessManager;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length == 1)
        {
            mAccessManager = (WarpAccessManager)values[0];
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        //Do nothing
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        Object [] progress = servant.getCurrentProgress();
        if(progress != null && progress.length == 1 && progress[0] instanceof Map<?,?>)
        {
            @SuppressWarnings("unchecked")
            Map<WarpLocation, String[]> nodes = (Map<WarpLocation,String []>)progress[0];
            updateDeviceManager(nodes);
        }
    }

    private void updateDeviceManager(Map<WarpLocation, String []> nodes)
    {
        IWarpInteractiveDevice [] interactiveDevices = new IWarpInteractiveDevice[nodes.size()];
        AndroidNetworkDevice networkDevice;
        WarpDeviceManager deviceManager = mAccessManager.getDeviceManager();
        int i=0;
        for(WarpLocation peerLocation: nodes.keySet())
        {
            networkDevice = new AndroidNetworkDevice(peerLocation);
            interactiveDevices[i++] = new AndroidInteractiveDevice(mAccessManager,
                    IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED,networkDevice);
        }
        deviceManager.addWarpDevices(interactiveDevices,AndroidNetworkDevice.class,true);
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: to implement
    }
}
