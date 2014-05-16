package unibo.ing.warp.core.device;

import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public abstract class DefaultWarpDevice implements IWarpDevice {
    private Collection<String> mServicesNames;
    private IWarpDeviceRequestManager mRequestManager;
    private boolean bConnected;

    public DefaultWarpDevice(IWarpDeviceRequestManager requestManager)
    {
        mRequestManager=requestManager;
    }

    protected IWarpDeviceRequestManager getWarpRequestManager()
    {
        return mRequestManager;
    }

    @Override
    public Collection<String> getAvailableServicesNames(IWarpServiceListener listener)
    {
        if(mServicesNames != null)
        {
            return mServicesNames;
        }
        if(mRequestManager != null)
        {
            mRequestManager.onServicesLookupRequest(this,listener);
        }
        return null;
    }

    @Override
    public synchronized boolean isConnected()
    {
        return bConnected;
    }

    @Override
    public synchronized void setConnected(boolean connected)
    {
        bConnected=connected;
    }

    @Override
    public void setAvailableServicesNames(Collection<String> servicesNames)
    {
        mServicesNames = servicesNames;
    }

    @Override
    public synchronized void updateAbstractDevice(Object abstractDevice) {}
}
