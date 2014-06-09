package unibo.ing.warp.core.service.android.p2p;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.android.DirectWifiPingLauncher;
import unibo.ing.warp.core.service.listener.android.DirectWifiPingProvideServiceListener;
import java.net.InetAddress;

/**
 * Created by Lorenzo Donini on 6/3/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.PUSH, execution = WarpServiceInfo.ServiceExecution.CONCURRENT,
        name = "directWifiPing", label = "Ping", protocol = WarpServiceInfo.Protocol.UDP,
        launcher = DirectWifiPingLauncher.class, provideListener = DirectWifiPingProvideServiceListener.class)
public class DirectWifiPingService extends DefaultWarpService {
    private InetAddress mPeerInetAddress;
    private String mPeerMacAddress;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Everyone that is not the group owner should be doing
        // this once a directWifi connection has been established!

        //Nothing more needs to be done since the ping is completed once
        // the service parameters are passed to the group owner.
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Only the group owner provides this service, waiting for incoming pings
        checkOptionalParameters(params,1);
        mPeerMacAddress = (String) params[0];

        mPeerInetAddress = warpBeam.getPeerWarpLocation().getIPv4Address();
        getWarpServiceHandler().onServiceCompleted(this);
    }

    @Override
    public Object[] getResult()
    {
        return new Object[] {mPeerInetAddress, mPeerMacAddress};
    }

    @Override
    public Object[] getCurrentProgress() {
        return null;
    }
}
