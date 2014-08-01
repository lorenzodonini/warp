package unibo.ing.warp.view;

import unibo.ing.warp.R;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice.WarpDeviceStatus;
import unibo.ing.warp.core.device.android.AndroidNetworkDevice;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/18/2014.
 */
public class AndroidWarpDeviceResourcesLibrary {
    private static Map<WarpDeviceStatus, Map<Class<? extends IWarpDevice>,Integer>> mResourceMapping;

    static {
        //TODO: maybe implement it in another way?! By xml file perhaps?
        mResourceMapping = new EnumMap<WarpDeviceStatus, Map<Class<? extends IWarpDevice>,
                Integer>>(WarpDeviceStatus.class);
        //TODO: remember to change the initial capacity when adding UPnP devices!!!
        Map<Class<? extends IWarpDevice>, Integer> internalMap = new HashMap<Class<? extends IWarpDevice>, Integer>(2);
        internalMap.put(AndroidWifiHotspot.class, R.drawable.wifi_hotspot_disconnected);
        internalMap.put(AndroidP2PDevice.class, R.drawable.p2p_device_disconnected);
        internalMap.put(AndroidNetworkDevice.class, R.drawable.lan_device_disconnected);
        mResourceMapping.put(WarpDeviceStatus.DISCONNECTED, internalMap);

        internalMap = new HashMap<Class<? extends IWarpDevice>, Integer>(2);
        internalMap.put(AndroidWifiHotspot.class, R.drawable.wifi_hotspot_connecting);
        internalMap.put(AndroidP2PDevice.class, R.drawable.p2p_device_connecting);
        internalMap.put(AndroidNetworkDevice.class, R.drawable.lan_device_connecting);
        mResourceMapping.put(WarpDeviceStatus.CONNECTING, internalMap);

        internalMap = new HashMap<Class<? extends IWarpDevice>, Integer>(2);
        internalMap.put(AndroidWifiHotspot.class, R.drawable.wifi_hotspot_connected);
        internalMap.put(AndroidP2PDevice.class, R.drawable.p2p_device_connected);
        internalMap.put(AndroidNetworkDevice.class, R.drawable.lan_device_connected);
        mResourceMapping.put(WarpDeviceStatus.CONNECTED, internalMap);

        internalMap = new HashMap<Class<? extends IWarpDevice>, Integer>(2);
        internalMap.put(AndroidWifiHotspot.class, R.drawable.wifi_hotspot_refused);
        internalMap.put(AndroidP2PDevice.class, R.drawable.p2p_device_refused);
        internalMap.put(AndroidNetworkDevice.class, R.drawable.lan_device_refused);
        mResourceMapping.put(WarpDeviceStatus.FAILED, internalMap);
    }

    public static Integer getDrawableResourceId(WarpDeviceStatus key, Class<? extends IWarpDevice> deviceClass)
    {
        Map<Class<? extends IWarpDevice>, Integer> internalMap = mResourceMapping.get(key);
        return (internalMap != null) ? internalMap.get(deviceClass) : null;
    }
}
