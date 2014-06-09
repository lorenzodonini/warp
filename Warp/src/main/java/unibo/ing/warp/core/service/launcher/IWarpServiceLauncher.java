package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public interface IWarpServiceLauncher {
    public void initializeService(WarpResourceLibrary library, String permissionKey,
                                  IWarpService.ServiceOperation operation);
    public Object [] getServiceListenerParameters(IWarpInteractiveDevice target,
                                                  IWarpService.ServiceOperation operation);
    public Object [] getServiceParameters(IWarpInteractiveDevice target,
                                          IWarpService.ServiceOperation operation);
    public IWarpable [] getServiceRemoteParameters();

    public interface IWarpServiceInitializationListener {
        public void onInitializationCompleted();
    }
}
