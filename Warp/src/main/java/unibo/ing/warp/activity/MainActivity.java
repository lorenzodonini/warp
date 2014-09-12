package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import unibo.ing.warp.R;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidLocalDevice;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;
import unibo.ing.warp.core.service.IWarpService.ServiceOperation;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.core.service.base.WarpBeaconService;
import unibo.ing.warp.core.service.base.WarpLighthouseService;
import unibo.ing.warp.core.service.launcher.*;
import unibo.ing.warp.core.service.launcher.android.*;
import unibo.ing.warp.core.service.launcher.android.DirectWifiConnectLauncher;
import unibo.ing.warp.core.service.launcher.android.DirectWifiDiscoverLauncher;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.view.AndroidDeviceAdapter;
import unibo.ing.warp.view.AndroidRemoteServicesPopup;

public class MainActivity extends Activity {
    private AndroidDeviceAdapter mListAdapter;
    private BroadcastReceiver mReceiver;
    private String mAccessKey = "MASTER_KEY";
    private String mUserPermissionKey = "USER_KEY";
    private AndroidRemoteServicesPopup mServicesPopup;
    private WarpResourceLibrary mLibrary;
    private String mWifiP2PDiscoverName;
    private static final long DEFAULT_DISCOVER_INTERVAL = 30000; //Every 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        final WifiP2pManager p2pManager = (WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        final WifiP2pManager.Channel channel = p2pManager.initialize(this, getMainLooper(), null);

        //Adding some service names
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(DirectWifiDiscoverService.class);
        mWifiP2PDiscoverName = info.name();

        //Setting default resources
        mLibrary = WarpResourceLibrary.getInstance();
        mLibrary.setResource(WarpResourceLibrary.RES_ACCESS_MANAGER, mAccessKey, manager);
        mLibrary.setResource(WarpResourceLibrary.RES_CONTEXT, null, this);
        mLibrary.setResource(WarpResourceLibrary.RES_USER_PERMISSION_KEY, mAccessKey, mUserPermissionKey);
        mLibrary.setResource(WifiScanLauncher.DISCOVER_INTERVAL_KEY, mAccessKey, DEFAULT_DISCOVER_INTERVAL);
        mLibrary.setResource(WifiScanLauncher.WIFI_FORCE_ENABLE_KEY, mAccessKey, true);
        mLibrary.setResource(DirectWifiDiscoverLauncher.DISCOVERY_INTERVAL_KEY, mAccessKey,
                DEFAULT_DISCOVER_INTERVAL+10000);
        mLibrary.setResource(WarpBeaconLauncher.BROADCAST_INTERVAL_KEY,mAccessKey,
                DEFAULT_DISCOVER_INTERVAL*2);
        mLibrary.setResource(DirectWifiDiscoverLauncher.P2P_CHANNEL_KEY, mAccessKey, channel);
        mLibrary.setResource(DirectWifiConnectLauncher.P2P_OWNER_PRIORITY_KEY, mAccessKey, 15);
        //TODO: Add any additional resource that may be needed

        mReceiver = new AndroidNetworkStateManager(manager,wifiManager,p2pManager,mAccessKey);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(mReceiver,filter);

        //Creating Local Device. IT IS IMPORTANT THAT THE LIBRARY HAS ALREADY BEEN INITIALIZED!!
        manager.setLocalDevice(new AndroidLocalDevice(this,null,mAccessKey));
        manager.getLocalDevice().getWarpEngine().startEngine();

        //Graphics
        GridView gridView = (GridView)findViewById(R.id.deviceView);
        mListAdapter = new AndroidDeviceAdapter(this);
        gridView.setAdapter(mListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IWarpInteractiveDevice device = mListAdapter.getItem(position);
                onDeviceClick(device);
            }
        });
        manager.getDeviceManager().setViewObserver(mListAdapter);

        //Checking if already connected to a Network
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if(netInfo != null && netInfo.isConnected())
            {
                if(!manager.isServiceActive(WarpBeaconService.class,mAccessKey))
                {
                    manager.startWarpService(WarpBeaconService.class,mAccessKey,ServiceOperation.CALL,null);
                }
                if(!manager.isServiceActive(WarpLighthouseService.class,mAccessKey))
                {
                    manager.startWarpService(WarpLighthouseService.class, mAccessKey, ServiceOperation.CALL, null);
                }
            }
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mReceiver != null)
        {
            //TODO: remember to uncomment this!
            //unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mReceiver != null)
        {
            //TODO: remember to uncomment this!
            /*IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mReceiver,filter);*/
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_wifi:
                toggleWifi(item);
                break;
            case R.id.action_p2p:
                toggleWifiDirect(item);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    //DEVICE INTERACTION
    private void onDeviceClick(IWarpInteractiveDevice device)
    {
        WarpAccessManager accessManager = WarpAccessManager.getInstance(mAccessKey);

        mServicesPopup = new AndroidRemoteServicesPopup(MainActivity.this, (View)device.getView(),
                accessManager.getLocalDevice().getWarpEngine(),mAccessKey,device);
        if(mServicesPopup.getServicesAmount() == 0)
        {
            return;
        }
        mServicesPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                mServicesPopup.callService(item.getTitle().toString());
                return true;
            }
        });
        mServicesPopup.show();
    }

    //ACTIONS HANDLING
    private void toggleWifi(MenuItem item)
    {
        WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        if(manager.isServiceActive(WifiScanService.class,mAccessKey))
        {
            //Stop service
            manager.stopWarpService(WifiScanService.class,mAccessKey);
            manager.getDeviceManager().removeWarpDevices(AndroidWifiHotspot.class);
            item.setIcon(R.drawable.ic_action_wifi_disabled);
        }
        else
        {
            //Start service
            manager.startWarpService(WifiScanService.class,mAccessKey,ServiceOperation.CALL,null);
            item.setIcon(R.drawable.ic_action_wifi);
        }
    }

    //TODO: edit this handler
    private void toggleWifiDirect(MenuItem item)
    {
        WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        IWarpEngine warpDrive = manager.getLocalDevice().getWarpEngine();
        long [] runningServicesIds = warpDrive.getActiveServicesIdsByName(mWifiP2PDiscoverName);
        if(runningServicesIds == null)
        {
            //Create service!
            IWarpServiceLauncher p2pDiscoveryLauncher = warpDrive.getLauncherForService(mWifiP2PDiscoverName);
            p2pDiscoveryLauncher.initializeService(mLibrary,mAccessKey, ServiceOperation.CALL);
            IWarpServiceListener p2pDiscoveryListener = warpDrive.getListenerForService(
                    mWifiP2PDiscoverName,p2pDiscoveryLauncher.getServiceListenerParameters(null,
                    ServiceOperation.CALL), ServiceOperation.CALL);
            warpDrive.callLocalService(mWifiP2PDiscoverName, p2pDiscoveryListener,
                    p2pDiscoveryLauncher.getServiceParameters(null, ServiceOperation.CALL));
            item.setIcon(R.drawable.ic_action_p2p);
        }
        else
        {
            //Stop service!
            warpDrive.stopService(runningServicesIds[0]);
            manager.getDeviceManager().removeWarpDevices(AndroidP2PDevice.class);
            item.setIcon(R.drawable.ic_action_p2p_disabled);
        }
    }
}
