package unibo.ing.warp.core.service.launcher.android;

import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by lorenzodonini on 26/05/14.
 */
public final class DirectWifiConnectLauncher extends DefaultWarpServiceLauncher {
    //LISTENER
    private String mUserPermissionKey;
    private WarpAccessManager accessManager;
    private WarpServiceInfo chainServiceToCall;
    //SERVICE
    private WifiP2pManager.Channel mChannel;
    private int groupOwnerPriority=1;
    private String mMasterPermissionKey;
    public static final String P2P_CHANNEL_KEY = "p2pChannel";
    public static final String P2P_OWNER_PRIORITY_KEY = "p2pGroupOwnerPriority";

    @Override
    public void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        accessManager = (WarpAccessManager) library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
        groupOwnerPriority = (Integer) library.getResource(permissionKey, P2P_OWNER_PRIORITY_KEY);
        Object obj = library.getResource(permissionKey,WarpResourceLibrary.RES_SERVICE_TO_CALL);
        chainServiceToCall = (obj != null) ? (WarpServiceInfo)obj : null;
        mMasterPermissionKey = permissionKey;
        obj = library.getResource(mMasterPermissionKey,WarpResourceLibrary.RES_USER_PERMISSION_KEY);
        mUserPermissionKey = (obj != null) ? (String)obj : null;
        mChannel = (WifiP2pManager.Channel) library.getResource(permissionKey, P2P_CHANNEL_KEY);
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
        return (chainServiceToCall != null && mUserPermissionKey != null) ? new Object[] {accessManager,
                (DefaultWarpInteractiveDevice) target,chainServiceToCall,mUserPermissionKey}
                : new Object[] {accessManager,(DefaultWarpInteractiveDevice)target};
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
        return new Object[] {target.getWarpDevice(),mChannel,groupOwnerPriority,
                chainServiceToCall != null,mMasterPermissionKey};
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
