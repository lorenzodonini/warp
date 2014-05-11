package unibo.ing.warp.utils;

import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/1/2014.
 */
public class WarpUtils {
    private static Map<String,String> serviceLabels = new HashMap<String, String>();
    private static String filePath = "/temp";

    /**
     * Utility method that is needed every time a Service is invoked. Reflection
     * on the IWarpService is used in order to get the WarpServiceInfo annotation, which
     * holds important information on the service itself.
     *
     * @param serviceClass  The called service from which we need to get the info.
     * @return  Returns the WarpServiceInfo Annotation put on the given IWarpService.
     * Returns null if no annotation of WarpServiceInfo type was found, or if the WarpServiceInfo
     * annotation doesn't have a name value.
     */
    public static WarpServiceInfo getWarpServiceInfo(Class<? extends IWarpService> serviceClass)
    {
        if(serviceClass==null)
        {
            return null;
        }
        WarpServiceInfo result=null;
        Annotation annotations [] = serviceClass.getAnnotations();
        for(Annotation a: annotations)
        {
            if(a instanceof WarpServiceInfo)
            {
                result=(WarpServiceInfo)a;
                break;
            }
        }
        return result;
    }

    public static void addUserServiceLabel(String serviceName, String serviceLabel)
    {
        serviceLabels.put(serviceName,serviceLabel);
    }

    public static String getUserServiceLabel(String serviceName)
    {
        return serviceLabels.get(serviceName);
    }

    public static String getFilePath()
    {
        return filePath;
    }
}
