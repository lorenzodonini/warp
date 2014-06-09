package unibo.ing.warp.core.service.launcher.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.core.warpable.WarpableString;

/**
 * Created by Lorenzo Donini on 6/6/2014.
 */
public final class DirectWifiPingLauncher extends DefaultWarpServiceLauncher {
    public static final String RES_P2P_GROUP = "p2p.Group";
    public static final String RES_P2P_CONNECT_SERVICE_HANDLER = "p2p.connectServiceHandler";
    private String macAddress;
    private Handler mHandler;
    private WarpDeviceManager mDeviceManager;

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        Context context = (Context) library.getResource(permissionKey,WarpResourceLibrary.RES_CONTEXT);
        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        macAddress=manager.getConnectionInfo().getMacAddress();
    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey)
    {
        WarpAccessManager accessManager = (WarpAccessManager) library.getResource(
                permissionKey, WarpResourceLibrary.RES_ACCESS_MANAGER);
        mDeviceManager = accessManager.getDeviceManager();
        Object handler = library.getResource(permissionKey,RES_P2P_CONNECT_SERVICE_HANDLER);
        if(handler != null)
        {
            mHandler = (Handler) library.getResource(permissionKey, RES_P2P_CONNECT_SERVICE_HANDLER);
        }
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
        return (mHandler != null) ? new Object[] {mDeviceManager,mHandler} : new Object[] {mDeviceManager};
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
        return null;
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return new IWarpable[] {new WarpableString(macAddress)};
    }
}
