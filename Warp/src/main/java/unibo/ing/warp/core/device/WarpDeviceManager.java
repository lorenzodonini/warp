package unibo.ing.warp.core.device;

import java.util.*;

/**
 * User: lorenzodonini
 * Date: 27/11/13
 * Time: 13:46
 */
public class WarpDeviceManager {
    private Map<String,IWarpDevice> mWarpDevices;

    public WarpDeviceManager()
    {
        mWarpDevices=new HashMap<String, IWarpDevice>();
    }

    /**
     * Adds a single IWarpDevice to the WarpDeviceManager inner structure. Since
     * the inner structure is a Map, there will be no duplicates. Using the deviceName
     * property as the key inside the Map, in case the key already exists, the value
     * will be overwritten with the new one.
     *
     * @param device  The IWarpDevice that needs to be added
     */
    public synchronized void addWarpDevice(IWarpDevice device)
    {
        mWarpDevices.put(device.getDeviceName(),device);
    }

    /**
     * Adds a collection of IWarpDevices to the WarpDeviceManager inner structure.
     * The collection of devices passed to this method is usually homogeneous, being
     * them WifiDevices, P2PDevices or other. The inner structure is heterogeneous,
     * so the method doesn't discern between one type or another.
     * This method should be called after a successful discovery/scan operation.
     *
     * The method converts the collection into an array, then invokes the
     * addWarpDeviceCollection(IWarpDevice [] devices) method.
     *
     * @param devices  The collection of newly discovered IWarpDevices
     * @param removeOlder  A flag which determines whether old elements of the same type
     *                     should be automatically removed or not
     */
    public synchronized void addWarpDeviceCollection(Collection<IWarpDevice> devices, boolean removeOlder)
    {
        IWarpDevice [] array = (IWarpDevice[]) devices.toArray();
        addWarpDeviceCollection(array,removeOlder);
    }

    /**
     * Adds an array of IWarpDevices to the WarpDeviceManager inner structure.
     * The array of devices passed to this method is usually homogeneous, being
     * them WifiDevices, P2PDevices or other. The inner structure is heterogeneous,
     * so the method doesn't discern between one type or another.
     * This method should be called after a successful discovery/scan operation.
     *
     * The method adds all the elements of the array to the inner structure,
     * without generating any duplicates. Then, if requested, it performs an update
     * procedure, which checks whether there are any older elements inside the inner
     * structure of the same type as the ones passed to the method. In case there are
     * any older elements found, these are automatically removed thanks to the
     * autoUpdate(...) private method.
     *
     * @param devices  The array of newly discovered IWarpDevices
     * @param removeOlder  A flag which determines whether old elements of the same type
     *                     should be automatically removed or not
     */
    public synchronized void addWarpDeviceCollection(IWarpDevice [] devices, boolean removeOlder)
    {
        for(IWarpDevice d: devices)
        {
            addWarpDevice(d);
        }
        if(removeOlder)
        {
            autoUpdate(devices,devices[0].getClass());
        }
    }

    private void autoUpdate(IWarpDevice [] newDevices, Class<?> deviceClass)
    {
        boolean found;

        for(String s: mWarpDevices.keySet())
        {
            found=false;
            if(mWarpDevices.get(s).getClass()==deviceClass)
            {
                for(IWarpDevice d: newDevices)
                {
                    if(d.getDeviceName().equals(s))
                    {
                        found=true;
                        break;
                    }
                }
                if(!found)
                {
                    mWarpDevices.remove(s);
                }
            }
        }
    }

    public synchronized Set<String> getWarpDevicesNames()
    {
        return mWarpDevices.keySet();
    }

    public synchronized Collection<IWarpDevice> getWarpDevices()
    {
        return mWarpDevices.values();
    }

    public synchronized IWarpDevice getWarpDeviceByName(String deviceName)
    {
        return mWarpDevices.get(deviceName);
    }
}
