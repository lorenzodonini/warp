package unibo.ing.warp.core.device;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class WarpAccessManager implements IWarpDeviceRequestHandler {
    private IWarpLocalDevice mLocalDevice;
    private WarpDeviceManager mDeviceManager;
    private static String mAccessKey;
    private static WarpAccessManager mAccessManager;

    private WarpAccessManager(String accessKey)
    {
        mAccessKey=accessKey;
        mDeviceManager=new WarpDeviceManager();
    }

    public static WarpAccessManager getInstance(String accessKey)
    {
        if(mAccessManager == null)
        {
            if(accessKey != null)
            {
                mAccessManager = new WarpAccessManager(accessKey);
            }
        }
        return (accessKey != null && accessKey.equals(mAccessKey)) ? mAccessManager : null;
    }

    public void setLocalDevice(IWarpLocalDevice localDevice)
    {
        mLocalDevice=localDevice;
    }

    public IWarpLocalDevice getLocalDevice()
    {
        return mLocalDevice;
    }

    public WarpDeviceManager getDeviceManager()
    {
        return mDeviceManager;
    }

    //Single Device Request Handlers
    @Override
    public void onConnectRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener)
    {
        DefaultWarpDevice warpDevice = (DefaultWarpDevice) device.getWarpDevice();
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpDevice.getConnectServiceClass());
        if(info != null)
        {
            IWarpServiceResourcesHandler handler = mLocalDevice.getWarpEngine().getDefaultHandlerForService(info.name());
            mLocalDevice.getWarpEngine().callLocalService(info.name(), listener,
                    handler.getServiceParameters(device));
        }
    }

    @Override
    public void onDisconnectRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener)
    {
        DefaultWarpDevice warpDevice = (DefaultWarpDevice) device.getWarpDevice();
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpDevice.getDisconnectServiceClass());
        if(info != null)
        {
            IWarpServiceResourcesHandler handler = mLocalDevice.getWarpEngine().getDefaultHandlerForService(info.name());
            mLocalDevice.getWarpEngine().callLocalService(info.name(), listener,
                    handler.getServiceParameters(device));
        }
    }

    @Override
    public void onServicesLookupRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(LookupService.class);
        if(info != null)
        {
            //TODO: take a look here!
            //mLocalDevice.getWarpEngine().callPullService(info.name(),device,listener,null,null,null);
        }
    }
}
