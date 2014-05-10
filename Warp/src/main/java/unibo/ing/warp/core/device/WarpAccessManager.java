package unibo.ing.warp.core.device;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class WarpAccessManager {
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
}
