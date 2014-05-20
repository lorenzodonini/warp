package unibo.ing.warp.core.service.listener.android;

import android.net.wifi.WifiConfiguration;
import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;

/**
 * Created by Lorenzo Donini on 5/18/2014.
 */
public class AndroidConnectionSetupServiceListener extends DefaultWarpServiceListener {
    private WarpAccessManager mAccessManager;
    private DefaultWarpInteractiveDevice warpDevice;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length == 2)
        {
            mAccessManager = (WarpAccessManager) values[0];
            warpDevice = (DefaultWarpInteractiveDevice) values[1];
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getResult();
        int networkId = (Integer)result[0];
        if(networkId < 0)
        {
            return;
        }
        WifiConfiguration configuration = (WifiConfiguration) result[1];

        WarpServiceInfo connectInfo = WarpUtils.getWarpServiceInfo(WifiConnectService.class);
        if(mAccessManager != null)
        {
            IWarpEngine warpDrive = mAccessManager.getLocalDevice().getWarpEngine();
            Object [] listenerParams = new Object [] {mAccessManager,warpDevice};
            Object [] serviceParams = new Object [] {configuration,networkId};
            warpDrive.callLocalService(connectInfo.name(),warpDrive.getDefaultListenerForService(
                    connectInfo.name(),listenerParams),serviceParams);
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        //Never gets called. No need to implement anything.
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: to implement proper error handling
    }
}
