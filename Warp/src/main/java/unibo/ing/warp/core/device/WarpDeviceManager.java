package unibo.ing.warp.core.device;

import java.util.*;

/**
 * User: lorenzodonini
 * Date: 27/11/13
 * Time: 13:46
 */
public class WarpDeviceManager {
    private Map<String,IWarpDevice> mWarpDevices;
    private Map<Class<? extends IWarpDevice>, Collection<IWarpDevice>> mTypeMapping;

    public WarpDeviceManager()
    {
        mWarpDevices=new HashMap<String, IWarpDevice>();
        mTypeMapping=new HashMap<Class<? extends IWarpDevice>, Collection<IWarpDevice>>();
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
        if(device==null)
        {
            return;
        }
        IWarpDevice oldDevice = mWarpDevices.get(device.getDeviceName());
        if(oldDevice != null)
        {
            oldDevice.updateAbstractDevice(device.getAbstractDevice());
        }
        else
        {
            mWarpDevices.put(device.getDeviceName(),device);
            Collection<IWarpDevice> mapping = mTypeMapping.get(device.getClass());
            if(mapping != null)
            {
                mapping.add(device);
            }
            else
            {
                mapping = new ArrayList<IWarpDevice>();
                mapping.add(device);
                mTypeMapping.put(device.getClass(),mapping);
            }
        }
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
    public synchronized void addHomogeneousWarpDeviceCollection(Collection<IWarpDevice> devices,
                                        Class<? extends IWarpDevice> devicesClass, boolean removeOlder)
    {
        IWarpDevice [] array = (IWarpDevice[]) devices.toArray();
        addHomogeneousWarpDeviceCollection(array,devicesClass,removeOlder);
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
    public synchronized void addHomogeneousWarpDeviceCollection(IWarpDevice [] devices,
                                        Class<? extends IWarpDevice> devicesClass, boolean removeOlder)
    {
        if(devices == null)
        {
            return;
        }
        if(removeOlder)
        {
            removeOlderDevices(devicesClass,devices);
        }
        for(IWarpDevice device: devices)
        {
            update(mWarpDevices.get(device.getDeviceName()),device);
        }
    }

    private synchronized void update(IWarpDevice oldDevice, IWarpDevice device)
    {
        if(oldDevice != null)
        {
            oldDevice.updateAbstractDevice(device.getAbstractDevice());
        }
        else
        {
            mWarpDevices.put(device.getDeviceName(),device);
            Collection<IWarpDevice> oldDevices = mTypeMapping.get(device.getClass());
            if(oldDevices != null)
            {
                oldDevices.add(device);
            }
            else
            {
                oldDevices = new ArrayList<IWarpDevice>();
                oldDevices.add(device);
                mTypeMapping.put(device.getClass(),oldDevices);
            }
        }
    }

    private synchronized void removeOlderDevices(Class<? extends IWarpDevice> deviceClass, IWarpDevice [] devices)
    {
        IWarpDevice foundDevice;
        Collection<IWarpDevice> oldDevices = mTypeMapping.get(deviceClass);
        for(IWarpDevice oldDevice: oldDevices)
        {
            foundDevice=null;
            for(IWarpDevice device: devices)
            {
                if(device.getDeviceName().equals(oldDevice.getDeviceName()))
                {
                    foundDevice=oldDevice;
                    break;
                }
            }
            if(foundDevice==null)
            {
                oldDevices.remove(oldDevice);
                mWarpDevices.remove(oldDevice.getDeviceName());
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

    public synchronized Collection<IWarpDevice> getWarpDevicesByClass(Class<? extends IWarpDevice> devicesClass)
    {
        return mTypeMapping.get(devicesClass);
    }
}
