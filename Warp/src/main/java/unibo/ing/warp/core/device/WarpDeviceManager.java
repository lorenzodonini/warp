package unibo.ing.warp.core.device;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.view.IViewLifecycleObserver;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: lorenzodonini
 * Date: 27/11/13
 * Time: 13:46
 */
public class WarpDeviceManager {
    private IViewLifecycleObserver mViewObserver;
    private ConcurrentMap<Class<? extends IWarpDevice>, Vector<IWarpInteractiveDevice>> mWarpDevices;
    private ConcurrentMap<Class<? extends IWarpDevice>, List<String>> mBlackListDevices; //Names
    private int mSize;

    public WarpDeviceManager()
    {
        mWarpDevices = new ConcurrentHashMap<Class<? extends IWarpDevice>, Vector<IWarpInteractiveDevice>>();
        mBlackListDevices = new ConcurrentHashMap<Class<? extends IWarpDevice>, List<String>>();
        mSize=0;
    }

    public void setViewObserver(IViewLifecycleObserver observer)
    {
        mViewObserver = observer;
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
        if(oldDevices == null)
        {
            if(devices.length > 0)
            {
                oldDevices = new Vector<IWarpInteractiveDevice>();
                mWarpDevices.put(devicesClass, oldDevices);
            }
            else
            {
                return;
            }
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
        IWarpInteractiveDevice devicesArray [] = oldDevices.toArray(new IWarpInteractiveDevice[oldDevices.size()]);
        for(IWarpInteractiveDevice oldDevice : devicesArray)
        {
            if(isOnBlackList(oldDevice))
            {
                oldDevices.remove(oldDevice);
                mSize--;
                if(mViewObserver != null)
                {
                    mViewObserver.onWarpDeviceRemoved(oldDevice);
                    ((DefaultWarpInteractiveDevice)oldDevice).setViewObserver(null);
                }
                continue;
            }
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
                mSize--;
                if(mViewObserver != null)
                {
                    mViewObserver.onWarpDeviceRemoved(oldDevice);
                    ((DefaultWarpInteractiveDevice)oldDevice).setViewObserver(null);
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
            if(isOnBlackList(newDevice))
            {
                continue;
            }
            found=false;
            for(IWarpInteractiveDevice oldDevice : oldDevices)
            {
                if(newDevice.getWarpDevice().getDeviceName().equals(oldDevice.getWarpDevice().getDeviceName()))
                {
                    found=true;
                    oldDevice.updateDeviceData(newDevice);
                    ((DefaultWarpInteractiveDevice)oldDevice).setDeviceStatus(newDevice.getDeviceStatus());
                    break;
                }
            }
            if(!found)
            {
                oldDevices.add(newDevice);
                mSize++;
                if(mViewObserver != null)
                {
                    mViewObserver.onWarpDeviceAdded(newDevice);
                    ((DefaultWarpInteractiveDevice)newDevice).setViewObserver(mViewObserver);
                }
            }
        }
    }

    public synchronized void addBlackListDevice(Class<? extends IWarpDevice> deviceClass, String deviceName)
    {
        List<String> blackList = mBlackListDevices.get(deviceClass);
        if(blackList == null)
        {
            blackList = new LinkedList<String>();
            mBlackListDevices.put(deviceClass,blackList);
        }
        blackList.add(deviceName);
    }

    public synchronized void removeBlackListDevice(Class<? extends IWarpDevice> deviceClass, String deviceName)
    {
        List<String> blackList = mBlackListDevices.get(deviceClass);
        if(blackList != null)
        {
            if(deviceName == null)
            {
                mBlackListDevices.remove(deviceClass);
                return;
            }
            blackList.remove(deviceName);
            if(blackList.size() == 0)
            {
                mBlackListDevices.remove(deviceClass);
            }
        }
    }

    public synchronized boolean isOnBlackList(IWarpInteractiveDevice device)
    {
        Class<? extends IWarpDevice> deviceClass = device.getWarpDevice().getClass();
        List<String> blackList = mBlackListDevices.get(deviceClass);
        if(blackList == null)
        {
            return false;
        }
        return blackList.contains(device.getWarpDevice().getDeviceName());
    }

    public synchronized Collection<IWarpDevice> getWarpDevices()
    {
        List<IWarpDevice> warpDevices = new ArrayList<IWarpDevice>(mSize);
        for(Vector<IWarpInteractiveDevice> devices : mWarpDevices.values())
        {
            for(IWarpInteractiveDevice device: devices)
            {
                warpDevices.add(device.getWarpDevice());
            }
        }
        return warpDevices;
    }

    public synchronized Collection<IWarpInteractiveDevice> getInteractiveDevices()
    {
        List<IWarpInteractiveDevice> warpInteractiveDevices = new ArrayList<IWarpInteractiveDevice>(mSize);
        for(Vector<IWarpInteractiveDevice> devices : mWarpDevices.values())
        {
            for(IWarpInteractiveDevice device: devices)
            {
                warpInteractiveDevices.add(device);
            }
        }
        return warpInteractiveDevices;
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

    public synchronized IWarpInteractiveDevice getWarpDeviceByName(String deviceName,
                    Class<? extends IWarpDevice> deviceClass)
    {
        Vector<IWarpInteractiveDevice> devices = mWarpDevices.get(deviceClass);
        if(devices == null)
        {
            return null;
        }
        for(IWarpInteractiveDevice device : devices)
        {
            if(device.getWarpDevice().getDeviceName().equals(deviceName))
            {
                return device;
            }
        }
        return null;
    }

    public synchronized void removeWarpDevices(Class<? extends IWarpDevice> deviceClass)
    {
        Vector<IWarpInteractiveDevice> devices = mWarpDevices.remove(deviceClass);
        if(devices != null && mViewObserver != null)
        {
            for(IWarpInteractiveDevice device : devices)
            {
                mViewObserver.onWarpDeviceRemoved(device);
            }
        }
    }

    public synchronized IWarpInteractiveDevice [] getInteractiveDevicesByClass(
                    Class<? extends IWarpDevice> devicesClass)
    {
        Vector<IWarpInteractiveDevice> devices = mWarpDevices.get(devicesClass);
        return (devices != null) ? devices.toArray(new IWarpInteractiveDevice[devices.size()]) : null;
    }
}
