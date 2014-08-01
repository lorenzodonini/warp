package unibo.ing.warp.utils;

import android.util.Log;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import java.lang.annotation.Annotation;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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

    public static byte [] getRawIPv4AddressFromInt(int address)
    {
        byte bytes [] = BigInteger.valueOf(address).toByteArray();
        byte inetAddrBytes [];
        if(bytes.length==5)
        {
            inetAddrBytes=new byte[4];
            for(int i=1; i<bytes.length; i++)
            {
                inetAddrBytes[i-1]=bytes[i];
            }
        }
        else
        {
            inetAddrBytes=bytes;
        }
        return inetAddrBytes;
    }

    public static void checkNetworkInterfaces()
    {
        try
        {
            Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
            while(ifs.hasMoreElements())
            {
                NetworkInterface i = ifs.nextElement();
                List<InterfaceAddress> addresses = i.getInterfaceAddresses();
                Log.d("NetworkInterface", i.getName() + " - " + i.getDisplayName());
                for(InterfaceAddress a: addresses)
                {
                    InetAddress broadcast = a.getBroadcast();
                    Log.d("NetworkAddress",a.getAddress().getHostAddress());
                    if(broadcast != null)
                    {
                        Log.d("NetworkBroadcastAddress",broadcast.getHostAddress());
                    }
                }
            }
        }
        catch (SocketException e)
        {
            e.printStackTrace();
        }
    }
}
