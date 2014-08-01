package unibo.ing.warp.core.service.launcher;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/29/2014.
 */
public final class WarpResourceLibrary {
    private Map<String, Object> mResources;
    private Map<String, String> mPermissions;
    private static WarpResourceLibrary mSingleton;
    public static final String RES_CONTEXT = "context";
    public static final String RES_ACCESS_MANAGER = "accessManager";
    public static final String RES_DOWNLOAD_DIR = "downloadDirectory";
    public static final String RES_SERVICE_TO_CALL = "chainedService";
    public static final String RES_DEFAULT_APPLICATION = "defaultApplication";
    public static final String RES_USER_PERMISSION_KEY = "userPermissionKey";
    public static final String RES_LOCAL_IP_ADDRESS = "broadcastAddress";

    private WarpResourceLibrary()
    {
        mResources = new HashMap<String, Object>();
        mPermissions = new HashMap<String, String>();
    }

    public static WarpResourceLibrary getInstance()
    {
        if(mSingleton == null)
        {
            mSingleton = new WarpResourceLibrary();
        }
        return mSingleton;
    }

    public synchronized Object getResource(String permissionKey, String resourceName)
    {
        if(resourceName == null)
        {
            return null;
        }
        String permission = mPermissions.get(resourceName);
        return (permission == null || permission.equals(permissionKey)) ? mResources.get(resourceName) : null;
    }

    public synchronized boolean setResource(String resourceName, String permissionKey, Object resource)
    {
        if(resourceName == null)
        {
            return false;
        }
        String permission = mPermissions.get(resourceName);
        if(permission == null)
        {
            if(mResources.containsKey(resourceName))
            {
                return false;
            }
            putResource(resourceName,permissionKey,resource);
            return true;
        }
        else if(permission.equals(permissionKey))
        {
            putResource(resourceName,permissionKey,resource);
            return true;
        }
        return false;
    }

    private synchronized void putResource(String resourceName, String permissionKey, Object resource)
    {
        if(permissionKey != null)
        {
            mPermissions.put(resourceName,permissionKey);
        }
        mResources.put(resourceName,resource);
    }
}
