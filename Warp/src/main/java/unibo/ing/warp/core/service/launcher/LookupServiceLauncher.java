package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.warpable.IWarpable;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public final class LookupServiceLauncher extends DefaultWarpServiceLauncher {
    //TODO: TO IMPLEMENT
    private Collection<String> servicesNames;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        //Do nothing
    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey)
    {
        WarpAccessManager manager = (WarpAccessManager) library.getResource(
                permissionKey,WarpResourceLibrary.RES_ACCESS_MANAGER);
        servicesNames = manager.getLocalDevice().getWarpEngine().getServicesNames();
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
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target)
    {
        return null;
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {servicesNames};
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }
}
