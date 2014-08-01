package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/31/2014.
 */
public class WarpLighthouseLauncher extends DefaultWarpServiceLauncher {
    IWarpEngine mWarpDrive;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        WarpAccessManager access = (WarpAccessManager) library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
        mWarpDrive = access.getLocalDevice().getWarpEngine();
        /*The service merely works in background and does not provide results, therefore
        we use a DefaultEmptyWarpServiceListener as listener! */
    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey)
    {
        //Do nothing
    }

    /*############ LISTENER PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceListenerParameters(IWarpInteractiveDevice target) {
        return null;
    }

    @Override
    protected Object[] getProvideServiceListenerParameters(IWarpInteractiveDevice target) {
        return null;
    }

    /*############ SERVICE PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target) {
        return new Object[] {mWarpDrive};
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target) {
        return null;
    }

    @Override
    public IWarpable[] getServiceRemoteParameters() {
        return null;
    }
}
