package unibo.ing.warp.core.service.listener.android;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.wifi.WifiDisconnectService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class WifiDisconnectServiceListener extends DefaultWarpServiceListener {
    private DefaultWarpInteractiveDevice warpDevice;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length==1)
        {
            warpDevice = (DefaultWarpInteractiveDevice) values[0];
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getResult();
        String message = (String) result[0];
        warpDevice.setOperationProgress(servant.getCurrentPercentProgress());
        warpDevice.setOperationLabel(message);
        if(message.equals(WifiDisconnectService.DISCONNECTED))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED);
        }
        else
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.FAILED);
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        Object [] result = servant.getCurrentProgress();
        String message = (String) result[0];
        if(message.equals(WifiDisconnectService.DISCONNECTING))
        {
            warpDevice.setOperationProgress(servant.getCurrentPercentProgress());
        }
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: handle!
    }
}
