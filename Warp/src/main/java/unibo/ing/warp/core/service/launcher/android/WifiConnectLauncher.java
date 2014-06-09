package unibo.ing.warp.core.service.launcher.android;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public final class WifiConnectLauncher extends DefaultWarpServiceLauncher {
    //LISTENER
    private WarpAccessManager mAccessManager;
    private WarpServiceInfo mChainedServiceToCall;
    private String mUserPermissionKey;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        mAccessManager = (WarpAccessManager) library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
        mChainedServiceToCall = (WarpServiceInfo) library.getResource(permissionKey,
                WarpResourceLibrary.RES_SERVICE_TO_CALL);
        mUserPermissionKey = (String) library.getResource(permissionKey,
                WarpResourceLibrary.RES_USER_PERMISSION_KEY);
        //Not calling onServiceInitialized on purpose since this core service will be handled in a different way
    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey)
    {
        //Do nothing
    }

    /*############ LISTENER PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return (mChainedServiceToCall != null && mUserPermissionKey != null) ? new Object[] {mAccessManager,
                target, mChainedServiceToCall, mUserPermissionKey} : new Object[] {mAccessManager,target};
    }

    @Override
    protected Object[] getProvideServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return null;
    }

    /*############ SERVICE PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {target.getWarpDevice()};
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target)
    {
        return null;
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }
}
