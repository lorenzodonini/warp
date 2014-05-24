package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
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
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.core.service.handler.*;
import unibo.ing.warp.core.service.handler.android.DirectWifiDiscoverServiceHandler;
import unibo.ing.warp.core.service.handler.android.WifiConnectHandler;
import unibo.ing.warp.core.service.handler.android.WifiScanHandler;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.view.AndroidDeviceAdapter;
import unibo.ing.warp.view.AndroidServicesPopup;

public class MainActivity extends Activity {
    private AndroidDeviceAdapter mListAdapter;
    private BroadcastReceiver mReceiver;
    private String mAccessKey = "TEMP_KEY";
    private AndroidServicesPopup mServicesPopup;
    private String mWifiScanName;
    private String mWifiP2PDiscoverName;
    private static final long DEFAULT_DISCOVER_INTERVAL = 30000; //Every 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        final WifiP2pManager p2pManager = (WifiP2pManager)getSystemService(WIFI_P2P_SERVICE);
        //Adding some service names
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(WifiScanService.class);
        mWifiScanName = info.name();
        info = WarpUtils.getWarpServiceInfo(DirectWifiDiscoverService.class);
        mWifiP2PDiscoverName = info.name();

        //Creating Local Device
        manager.setLocalDevice(new AndroidLocalDevice(this,null));

        //ADDING CORE SERVICE HANDLERS
        WarpServiceHandlerManager handlerManager = manager.getLocalDevice().getWarpEngine()
                .getServiceHandlerManager();
        //WifiConnectHandler
        info = WarpUtils.getWarpServiceInfo(WifiConnectService.class);
        handlerManager.addServiceHandler(info.name(), new WifiConnectHandler(manager));
        //WifiScanHandler
        info = WarpUtils.getWarpServiceInfo(WifiScanService.class);
        WifiScanHandler scanHandler = new WifiScanHandler(manager,wifiManager);
        scanHandler.addServiceParameter(WifiScanHandler.DISCOVER_INTERVAL_KEY,DEFAULT_DISCOVER_INTERVAL);
        scanHandler.addServiceParameter(WifiScanHandler.WIFI_FORCE_ENABLE_KEY,true);
        handlerManager.addServiceHandler(info.name(), scanHandler);
        //DirectWifiDiscoverHandler
        info = WarpUtils.getWarpServiceInfo(DirectWifiDiscoverService.class);
        DirectWifiDiscoverServiceHandler discoverHandler = new DirectWifiDiscoverServiceHandler(manager);
        discoverHandler.addServiceParameter(DirectWifiDiscoverServiceHandler.DISCOVERY_INTERVAL_KEY,
                DEFAULT_DISCOVER_INTERVAL+10000);
        handlerManager.addServiceHandler(info.name(),discoverHandler);

        //TODO: Add Non-core Services

        mReceiver = new AndroidNetworkStateManager(manager,wifiManager,p2pManager);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver,filter);

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
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if(mReceiver != null)
        {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(mReceiver != null)
        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mReceiver,filter);
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

        mServicesPopup = new AndroidServicesPopup(MainActivity.this,
                (View)device.getView(),accessManager.getLocalDevice().getWarpEngine(),device);
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
        IWarpEngine warpDrive = manager.getLocalDevice().getWarpEngine();
        IWarpServiceHandler wifiScanHandler = warpDrive.getDefaultHandlerForService(mWifiScanName);
        long [] runningServicesIds = warpDrive.getActiveServicesIdsByName(mWifiScanName);
        if(runningServicesIds == null)
        {
            //Create service!
            IWarpServiceListener wifiScanListener = warpDrive.getDefaultListenerForService(
                    mWifiScanName, wifiScanHandler.getServiceListenerParameters(null));
            warpDrive.callLocalService(mWifiScanName, wifiScanListener,
                    wifiScanHandler.getServiceParameters(null));
            item.setIcon(R.drawable.ic_action_wifi);
        }
        else
        {
            //Stop service!
            warpDrive.stopService(runningServicesIds[0]);
            manager.getDeviceManager().removeWarpDevices(AndroidWifiHotspot.class);
            item.setIcon(R.drawable.ic_action_wifi_disabled);
        }
    }

    private void toggleWifiDirect(MenuItem item)
    {
        WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        IWarpEngine warpDrive = manager.getLocalDevice().getWarpEngine();
        IWarpServiceHandler p2pDiscoveryHandler = warpDrive.getDefaultHandlerForService(mWifiP2PDiscoverName);
        long [] runningServicesIds = warpDrive.getActiveServicesIdsByName(mWifiP2PDiscoverName);
        if(runningServicesIds == null)
        {
            //Create service!
            IWarpServiceListener p2pDiscoveryListener = warpDrive.getDefaultListenerForService(
                    mWifiP2PDiscoverName,p2pDiscoveryHandler.getServiceListenerParameters(null));
            warpDrive.callLocalService(mWifiP2PDiscoverName, p2pDiscoveryListener,
                    p2pDiscoveryHandler.getServiceParameters(null));
            //TODO: set icon!
        }
        else
        {
            //Stop service!
            warpDrive.stopService(runningServicesIds[0]);
            manager.getDeviceManager().removeWarpDevices(AndroidP2PDevice.class);
            //TODO: set icon!
        }
    }
}
