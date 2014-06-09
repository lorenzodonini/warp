package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/31/2014.
 */
public final class PushFileLauncher extends DefaultWarpServiceLauncher {
    //TODO: TO IMPLEMENT!

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey) {

    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey) {

    }

    /*############ LISTENER PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceListenerParameters(IWarpInteractiveDevice target) {
        return new Object[0];
    }

    @Override
    protected Object[] getProvideServiceListenerParameters(IWarpInteractiveDevice target) {
        return new Object[0];
    }

    /*############ SERVICE PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target) {
        return new Object[0];
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target) {
        return new Object[0];
    }

    @Override
    public IWarpable[] getServiceRemoteParameters() {
        return new IWarpable[0];
    }
}
