package unibo.ing.warp.core.android;

import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.base.PushFileService;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/8/2014.
 */
public class AndroidLabelMapping {
    private static Map<Class<? extends IWarpService>, String> serviceLabelMapping;

    static {
        serviceLabelMapping = new HashMap<Class<? extends IWarpService>, String>();
        serviceLabelMapping.put(WifiConnectService.class, "Connect");
        serviceLabelMapping.put(PushFileService.class, "Send File");
    }

    public static void addUserServiceLabel(Class<? extends IWarpService> serviceClass, String serviceLabel)
    {
        serviceLabelMapping.put(serviceClass,serviceLabel);
    }

    public static String getUserServiceLabel(Class<? extends IWarpService> serviceClass)
    {
        return serviceLabelMapping.get(serviceClass);
    }
}
