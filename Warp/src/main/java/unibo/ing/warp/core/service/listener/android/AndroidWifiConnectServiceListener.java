package unibo.ing.warp.core.service.listener.android;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/17/2014.
 */
public class AndroidWifiConnectServiceListener extends DefaultWarpServiceListener {
    private DefaultWarpInteractiveDevice warpDevice;
    private WarpAccessManager accessManager;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length == 2)
        {
            accessManager = (WarpAccessManager) values[0];
            warpDevice = (DefaultWarpInteractiveDevice) values[1];
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getCurrentProgress();
        String message = (String) result[0];
        if(message.equals(WifiConnectService.CONNECTED))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
            //TODO: implement the rest! Need to start a beacon service to identify network nodes
        }
        else if(message.equals(WifiConnectService.FAILED))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.FAILED);
        }
        warpDevice.setOperationProgress(servant.getCurrentPercentProgress());
        warpDevice.setOperationLabel(message);
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        Object [] result = servant.getCurrentProgress();
        String message = (String) result[0];
        if(message.equals(WifiConnectService.CONNECTING))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTING);

        }
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: handle!
    }
}
