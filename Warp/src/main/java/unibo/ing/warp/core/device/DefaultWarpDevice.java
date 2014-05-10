package unibo.ing.warp.core.device;

import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public abstract class DefaultWarpDevice implements IWarpDevice {
    private boolean bConnected;
    private Collection<String> mServicesNames;
    private WarpAccessManager mAccessManager;

    public DefaultWarpDevice(WarpAccessManager accessManager)
    {
        mAccessManager=accessManager;
    }

    protected WarpAccessManager getAccessManager()
    {
        return mAccessManager;
    }

    @Override
    public Collection<String> getAvailableServicesNames(IWarpServiceListener listener)
    {
        if(mServicesNames != null)
        {
            return mServicesNames;
        }
        WarpServiceInfo lookupInfo = WarpUtils.getWarpServiceInfo(LookupService.class);
        IWarpEngine localEngine = mAccessManager.getLocalDevice().getWarpEngine();
        localEngine.callLocalService(lookupInfo.name(),listener,null);
        return null;
    }

    @Override
    public void setAvailableServicesNames(Collection<String> servicesNames)
    {
        mServicesNames = servicesNames;
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

    public abstract Class<? extends IWarpService> getConnectServiceClass();
    public abstract Class<? extends IWarpService> getDisconnectServiceClass();
}
