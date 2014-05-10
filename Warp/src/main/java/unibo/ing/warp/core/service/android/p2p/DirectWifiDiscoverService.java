package unibo.ing.warp.core.service.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

import java.util.Collection;

/**
 * User: lorenzodonini
 * Date: 25/11/13
 * Time: 01:11
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, name="p2pDiscovery",
        execution = WarpServiceInfo.ServiceExecution.CONCURRENT, target=WarpServiceInfo.Target.ANDROID)
public class DirectWifiDiscoverService extends DefaultWarpService {
    private boolean bEnabled=false;
    private BroadcastReceiver mReceiver;
    private Channel mChannel;
    private WifiP2pManager mDirectWifiManager;
    private PeerListListener mPeerListListener;
    private Collection<WifiP2pDevice> result;

    //TODO: STOP SERVICE?!
    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,2);
        setContext(context);
        long interval = (Long)params[0];
        mChannel = (Channel)params[1]; //Channel has already been initialized

        //Logic starts now
        setEnabled(true);
        initializePeerListener();
        mReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION))
                {
                    mDirectWifiManager.requestPeers(mChannel,mPeerListListener);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        Context androidContext = (Context)getContext();
        androidContext.registerReceiver(mReceiver, intentFilter);
        mDirectWifiManager=(WifiP2pManager)androidContext.getSystemService(Context.WIFI_P2P_SERVICE);
        discoveryOperation(interval);
    }

    private void initializePeerListener()
    {
        if(mPeerListListener==null)
        {
            mPeerListListener=new PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers)
                {
                    result=peers.getDeviceList();
                    onDiscoveredPeersHandler();
                }
            };
        }
    }

    private void discoveryOperation(long discoverInterval) throws Exception
    {
        ActionListener actionListener = new ActionListener() {
            @Override
            public void onSuccess()
            {
                //Discovery successful
            }

            @Override
            public void onFailure(int reason)
            {
                //Discovery failed
            }
        };
        WifiManager wifiManager = (WifiManager)((Context)getContext()).
                getSystemService(Context.WIFI_SERVICE);
        while (isEnabled())
        {
            if(!wifiManager.isWifiEnabled())
            {
                throw new Exception("DirectWifiDiscoverService.discoveryOperation: " +
                        "Wifi module is currently inactive!");
            }
            mDirectWifiManager.discoverPeers(mChannel,actionListener);
            Thread.sleep(discoverInterval);
        }
    }

    /**
     * Handles the success of a peer discovery and notifies the WarpServiceListener
     * interface. The method is only called after an onPeersAvailable(...) has been
     * triggered inside a proper PeerListListener.
     */
    private void onDiscoveredPeersHandler()
    {
        if(isEnabled())
        {
            getWarpServiceHandler().onServiceProgressUpdate(this);
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //DO NOTHING SINCE SERVICE IS LOCAL
    }

    @Override
    public Object[] getResult()
    {
        return getCurrentProgress();
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {result};
    }

    private synchronized boolean isEnabled()
    {
        return bEnabled;
    }

    private synchronized void setEnabled(boolean enabled)
    {
        bEnabled=enabled;
    }

    /**
     * Private class used as a background thread. While the thread is running and WiFi
     * is active, it automatically performs a WifiP2pManager.discoverPeers(...), in order
     * to check if there any available P2P devices nearby. The thread is not responsible
     * for handling the success/failure of the discovery process, neither does it call
     * a procedure. Thus a BroadcastReceiver must be registered somewhere else.
     *
     * The discovery is performed on a timely basis, given a Long type time value to the
     * constructor. The thread simply invokes the discovery method and then goes to sleep
     * for the amount specified in the discoverInterval field.
     */
}
