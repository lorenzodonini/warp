package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/31/2014.
 */
public final class WarpDispatcherLauncher extends DefaultWarpServiceLauncher {
    private WarpAccessManager mWarpAccessManager;
    private String mUserPermissionKey;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        mWarpAccessManager = (WarpAccessManager) library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
        mUserPermissionKey = (String) library.getResource(permissionKey,
                WarpResourceLibrary.RES_USER_PERMISSION_KEY);
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
        return null;
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
        return new Object[] {mWarpAccessManager.getLocalDevice().getWarpEngine(),mUserPermissionKey};
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
