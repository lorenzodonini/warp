package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/31/2014.
 */
public class WarpBeaconLauncher extends DefaultWarpServiceLauncher {
    public static final String BROADCAST_INTERVAL_KEY = "pingBroadcastInterval";
    private Long mInterval;
    private WarpAccessManager mAccessManager;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        mInterval = (Long)library.getResource(permissionKey,BROADCAST_INTERVAL_KEY);
        mAccessManager = (WarpAccessManager)library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
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
        return new Object[] {mAccessManager};
    }

    @Override
    protected Object[] getProvideServiceListenerParameters(IWarpInteractiveDevice target) {
        return null;
    }

    /*############ SERVICE PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target) {
        return new Object[] {mInterval};
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
