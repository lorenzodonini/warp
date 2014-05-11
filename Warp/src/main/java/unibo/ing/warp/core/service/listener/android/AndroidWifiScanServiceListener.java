package unibo.ing.warp.core.service.listener.android;

import android.net.wifi.ScanResult;
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
        IWarpDevice devices [] = new IWarpDevice[scanResults.size()];
        int i=0;
        for(ScanResult scanResult : scanResults)
        {
            devices[i++] = new AndroidWifiHotspot(warpAccessManager,scanResult);
        }
        warpAccessManager.getDeviceManager().addHomogeneousWarpDeviceCollection(
                devices,AndroidWifiHotspot.class,true);
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: to implement
    }
}
