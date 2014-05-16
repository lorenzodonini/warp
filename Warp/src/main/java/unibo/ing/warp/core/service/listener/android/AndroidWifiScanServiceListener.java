package unibo.ing.warp.core.service.listener.android;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.AndroidInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public class AndroidWifiScanServiceListener extends DefaultWarpServiceListener {
    private WarpAccessManager warpAccessManager;
    private WifiManager wifiManager;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length == 2)
        {
            warpAccessManager = (WarpAccessManager)values[0];
            wifiManager = (WifiManager)values[1];
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
        Object [] result = servant.getCurrentProgress();
        if(result != null && result.length == 1 && result[0] instanceof List<?>)
        {
            @SuppressWarnings("unchecked")
            List<ScanResult> scanResults = (List<ScanResult>) result[0];
            updateDeviceManager(scanResults);
        }
    }

    private void updateDeviceManager(List<ScanResult> scanResults)
    {
        if(warpAccessManager == null)
        {
            return;
        }
        IWarpInteractiveDevice interactiveDevices [] = new IWarpInteractiveDevice[scanResults.size()];
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        AndroidWifiHotspot device;
        int i=0;
        for(ScanResult scanResult : scanResults)
        {
            device = new AndroidWifiHotspot(warpAccessManager,scanResult);
            //Checking if already connected to the found device
            if(connectionInfo != null && connectionInfo.getSSID().equals("\""+device.getDeviceName()+"\""))
            {
                device.setConnected(true);
            }
            interactiveDevices[i++] = new AndroidInteractiveDevice(device);
        }
        warpAccessManager.getDeviceManager().addWarpDevices(
                interactiveDevices, AndroidWifiHotspot.class, true);
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: to implement
    }
}
