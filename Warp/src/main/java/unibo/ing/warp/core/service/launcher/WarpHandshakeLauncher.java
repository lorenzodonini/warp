package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/31/2014.
 */
public final class WarpHandshakeLauncher extends DefaultWarpServiceLauncher {
    //SERVICE
    private WarpLocation mRemoteLocation;
    private IWarpable [] mRemoteParams;
    private WarpServiceInfo mServiceToLaunchDescriptor;

    public void setRemoteParameters(IWarpable [] params)
    {
        mRemoteParams = params;
    }

    public void setRemoteDeviceLocation(WarpLocation location)
    {
        mRemoteLocation = location;
    }

    public void setServiceToLaunchDescriptor(WarpServiceInfo info)
    {
        mServiceToLaunchDescriptor=info;
    }

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        //Do nothing
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
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {mRemoteLocation,mServiceToLaunchDescriptor,mRemoteParams};
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target) {
        return null;
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }
}
