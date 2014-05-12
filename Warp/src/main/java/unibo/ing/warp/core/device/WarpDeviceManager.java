package unibo.ing.warp.core.device;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.view.IViewObserver;
import java.util.*;

/**
 * User: lorenzodonini
 * Date: 27/11/13
 * Time: 13:46
 */
public class WarpDeviceManager {
    private IViewObserver mViewObserver;
    private Map<Class<? extends IWarpDevice>, Vector<IWarpInteractiveDevice>> mWarpDevices;

    public WarpDeviceManager()
    {
        mWarpDevices = new HashMap<Class<? extends IWarpDevice>, Vector<IWarpInteractiveDevice>>();
    }

    public void setViewObserver(IViewObserver observer)
    {
        mViewObserver = observer;
    }

    //NEW STUFF
    /**
     * Adds a collection of IWarpDevices to the WarpDeviceManager inner structure.
     * The collection of devices passed to this method is usually homogeneous, being
     * them WifiDevices, P2PDevices or other. The inner structure is heterogeneous,
     * so the method doesn't discern between one type or another.
     * This method should be called after a successful discovery/scan operation.
     *
     * The method converts the collection into an array, then invokes the
     * addWarpDeviceCollection(IWarpDevice [] devices) method.
     */
    public synchronized void addWarpDevices(Collection<IWarpInteractiveDevice> devices,
                                    Class<? extends IWarpDevice> deviceClass, boolean removeOlder)
    {
        IWarpInteractiveDevice newDevices [] = (IWarpInteractiveDevice[]) devices.toArray();
        addWarpDevices(newDevices,deviceClass,removeOlder);
    }

    /**
     * Adds an array of IWarpDevices to the WarpDeviceManager inner structure.
     * The array of devices passed to this method is usually homogeneous, being
     * them WifiDevices, P2PDevices or other. The inner structure is heterogeneous,
     * so the method doesn't discern between one type or another.
     * This method should be called after a successful discovery/scan operation.
     */
    public synchronized void addWarpDevices(IWarpInteractiveDevice [] devices,
                                    Class<? extends IWarpDevice> devicesClass, boolean removeOlder)
    {
        if(devicesClass == null || devices == null)
        {
            return;
        }
        Vector<IWarpInteractiveDevice> oldDevices = mWarpDevices.get(devicesClass);
        if(oldDevices == null && devices.length > 0)
        {
            oldDevices = new Vector<IWarpInteractiveDevice>();
        }
        if(removeOlder)
        {
            removeOlderDevices(oldDevices,devices);
        }
        addNewerDevices(oldDevices,devices);
    }

    private synchronized void removeOlderDevices(Vector<IWarpInteractiveDevice> oldDevices,
                                    IWarpInteractiveDevice [] newDevices)
    {
        boolean found;
        for(IWarpInteractiveDevice oldDevice : oldDevices)
        {
            found=false;
            for(IWarpInteractiveDevice newDevice : newDevices)
            {
                if(newDevice.getWarpDevice().getDeviceName().equals(oldDevice.getWarpDevice().getDeviceName()))
                {
                    found=true;
                    break;
                }
            }
            if(!found)
            {
                oldDevices.remove(oldDevice);
                if(mViewObserver != null)
                {
                    mViewObserver.onWarpDeviceRemoved(oldDevice);
                }
            }
        }
    }

    private synchronized void addNewerDevices(Vector<IWarpInteractiveDevice> oldDevices,
                                    IWarpInteractiveDevice [] newDevices)
    {
        boolean found;
        for(IWarpInteractiveDevice newDevice : newDevices)
        {
            found=false;
            for(IWarpInteractiveDevice oldDevice : oldDevices)
            {
                if(newDevice.getWarpDevice().getDeviceName().equals(oldDevice.getWarpDevice().getDeviceName()))
                {
                    found=true;
                    oldDevice.getWarpDevice().updateAbstractDevice(newDevice.getWarpDevice().getAbstractDevice());
                    break;
                }
            }
            if(!found)
            {
                oldDevices.add(newDevice);
                if(mViewObserver != null)
                {
                    mViewObserver.onWarpDeviceAdded(newDevice);
                }
            }
        }
    }

    public synchronized Set<String> getWarpDevicesNames()
    {
        Set<String> deviceNames = new HashSet<String>();
        for(Vector<IWarpInteractiveDevice> devices : mWarpDevices.values())
        {
            for(IWarpInteractiveDevice device: devices)
            {
                deviceNames.add(device.getWarpDevice().getDeviceName());
            }
        }
        return deviceNames;
    }

    public synchronized Collection<IWarpDevice> getWarpDevices()
    {
        List<IWarpDevice> warpDevices = new ArrayList<IWarpDevice>();
        for(Vector<IWarpInteractiveDevice> devices : mWarpDevices.values())
        {
            for(IWarpInteractiveDevice device: devices)
            {
                warpDevices.add(device.getWarpDevice());
            }
        }
        return warpDevices;
    }

    public synchronized IWarpDevice getWarpDeviceByName(String deviceName)
    {
        for(Vector<IWarpInteractiveDevice> devices : mWarpDevices.values())
        {
            for(IWarpInteractiveDevice device: devices)
            {
                if(device.getWarpDevice().getDeviceName().equals(deviceName))
                {
                    return device.getWarpDevice();
                }
            }
        }
        return null;
    }

    public synchronized Vector<IWarpInteractiveDevice> getWarpDevicesByClass(Class<? extends IWarpDevice> devicesClass)
    {
        return mWarpDevices.get(devicesClass);
    }
}
