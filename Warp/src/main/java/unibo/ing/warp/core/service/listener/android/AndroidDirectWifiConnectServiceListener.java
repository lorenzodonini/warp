package unibo.ing.warp.core.service.listener.android;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.p2p.DirectWifiConnectService;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.view.IWarpDeviceViewAdapter;

/**
 * Created by lorenzodonini on 26/05/14.
 */
public class AndroidDirectWifiConnectServiceListener extends DefaultWarpServiceListener {
    private DefaultWarpInteractiveDevice warpDevice;
    private WarpAccessManager accessManager;
    private WarpServiceInfo chainServiceToCall;

    @Override
    public void putDefaultValues(Object[] values)
    {
        if(values != null && values.length >= 2)
        {
            accessManager = (WarpAccessManager) values[0];
            warpDevice = (DefaultWarpInteractiveDevice) values[1];
            if(values.length == 3)
            {
                chainServiceToCall = (WarpServiceInfo)values[2];
            }
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getResult();
        String message = (String) result[0];
        warpDevice.setOperationProgress(servant.getCurrentPercentProgress());
        warpDevice.setOperationLabel(message);
        if(message.equals(WifiConnectService.CONNECTED))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED);
            if(chainServiceToCall != null)
            {
                startChainService();
            }
            //TODO: implement the rest! Need to start a beacon service to identify network nodes
        }
        else if(message.equals(WifiConnectService.FAILED))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.FAILED);
        }
    }

    private void startChainService()
    {
        IWarpEngine warpDrive = accessManager.getLocalDevice().getWarpEngine();
        IWarpServiceResourcesHandler serviceHandler = warpDrive.getDefaultHandlerForService(chainServiceToCall.name());
        Object [] listenerParams = serviceHandler.getServiceListenerParameters(warpDevice);
        Object [] params = serviceHandler.getServiceParameters(warpDevice);
        if(chainServiceToCall.type() == WarpServiceInfo.Type.LOCAL)
        {
            warpDrive.callLocalService(chainServiceToCall.name(),warpDrive.
                    getDefaultListenerForService(chainServiceToCall.name(), listenerParams),params);
        }
        else
        {
            IWarpable[] remoteParams = serviceHandler.getServiceRemoteParameters();
            if(chainServiceToCall.type() == WarpServiceInfo.Type.PUSH)
            {
                warpDrive.callPushService(chainServiceToCall.name(),warpDevice.getWarpDevice(),
                        warpDrive.getDefaultListenerForService(chainServiceToCall.name(),listenerParams),
                        null,params,remoteParams);
            }
            else
            {
                warpDrive.callPullService(chainServiceToCall.name(),warpDevice.getWarpDevice(),
                        warpDrive.getDefaultListenerForService(chainServiceToCall.name(),listenerParams),
                        null,params,remoteParams);
            }
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        Object [] result = servant.getCurrentProgress();
        String message = (String) result[0];
        if(message.equals(DirectWifiConnectService.CONNECTING))
        {
            warpDevice.setDeviceStatus(IWarpInteractiveDevice.WarpDeviceStatus.CONNECTING);
            warpDevice.setOperationProgress(servant.getCurrentPercentProgress());
        }
    }

    @Override
    public void onServiceAbort(String message)
    {
        warpDevice.setOperationProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
        //TODO: should we add something a bit more verbose?!
    }
}
