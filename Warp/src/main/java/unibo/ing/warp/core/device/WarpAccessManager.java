package unibo.ing.warp.core.device;

import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class WarpAccessManager implements IWarpDeviceRequestManager{
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
    public void onConnectRequest(IWarpDevice device, IWarpServiceListener listener)
    {
        if(!device.isConnected())
        {
            WarpServiceInfo info = WarpUtils.getWarpServiceInfo(device.getConnectServiceClass());
            if(info != null)
            {
                mLocalDevice.getWarpEngine().callLocalService(info.name(), listener, null);
            }
        }
    }

    @Override
    public void onDisconnectRequest(IWarpDevice device, IWarpServiceListener listener)
    {
        //TODO: implement please!
    }

    @Override
    public void onServicesLookupRequest(IWarpDevice device, IWarpServiceListener listener)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(LookupService.class);
        if(info != null)
        {
            mLocalDevice.getWarpEngine().callLocalService(info.name(), listener, null);
        }
    }
}
