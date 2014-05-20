package unibo.ing.warp.core;

import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.device.IWarpDeviceRequestHandler;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.view.IViewObserver;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public abstract class DefaultWarpInteractiveDevice implements IWarpInteractiveDevice {
    private Collection<String> mServicesNames;
    private IWarpDeviceRequestHandler mRequestManager;
    private IViewObserver mViewObserver;
    private WarpDeviceStatus mDeviceStatus;
    private int mOperationProgress;
    private String mCurrentLabel;

    public DefaultWarpInteractiveDevice(IWarpDeviceRequestHandler requestManager)
    {
        mRequestManager=requestManager;
        mDeviceStatus=WarpDeviceStatus.DISCONNECTED;
        mServicesNames = new LinkedList<String>();
    }

    public DefaultWarpInteractiveDevice(IWarpDeviceRequestHandler requestManager, WarpDeviceStatus status)
    {
        mRequestManager=requestManager;
        mDeviceStatus=status;
        mServicesNames = new LinkedList<String>();
    }

    public void setViewObserver(IViewObserver observer)
    {
        mViewObserver = observer;
    }

    @Override
    public synchronized Collection<String> getAvailableServicesNames(IWarpServiceListener listener)
    {
        if(mDeviceStatus == WarpDeviceStatus.CONNECTED)
        {
            if(mServicesNames.size()==0)
            {
                //Return only connect service
                WarpServiceInfo info = WarpUtils.getWarpServiceInfo(((DefaultWarpDevice)
                        getWarpDevice()).getDisconnectServiceClass());
                if(info != null)
                {
                    mServicesNames.add(info.name());
                }
                if(mRequestManager != null)
                {
                    mRequestManager.onServicesLookupRequest((DefaultWarpDevice)getWarpDevice(),listener);
                }
            }
        }
        else if(mDeviceStatus == WarpDeviceStatus.DISCONNECTED)
        {
            if(mServicesNames.size()==0)
            {
                //Return only connect service
                WarpServiceInfo info = WarpUtils.getWarpServiceInfo(((DefaultWarpDevice)
                        getWarpDevice()).getConnectServiceClass());
                if(info != null)
                {
                    mServicesNames.add(info.name());
                }
            }
        }
        return mServicesNames;
    }

    @Override
    public synchronized void addAvailableServicesNames(Collection<String> servicesNames)
    {
        mServicesNames = servicesNames;
    }

    @Override
    public void connect(IWarpServiceListener listener)
    {
        DefaultWarpDevice device = (DefaultWarpDevice)getWarpDevice();
        if(mRequestManager != null)
        {
            mRequestManager.onConnectRequest(device,listener);
        }
    }

    @Override
    public void disconnect(IWarpServiceListener listener)
    {
        //TODO: implement thanks!
    }

    @Override
    public void updateDeviceData(IWarpInteractiveDevice newDevice)
    {
        getWarpDevice().updateAbstractDevice(newDevice.getWarpDevice().getAbstractDevice());
        /* NOT SURE IF THIS IS A GOOD IDEA
        setDeviceStatus(newDevice.getDeviceStatus());
        setOperationProgress(newDevice.getDeviceOperationProgress());
        setOperationLabel(newDevice.getDeviceOperationLabel());*/
    }

    @Override
    public synchronized WarpDeviceStatus getDeviceStatus()
    {
        return mDeviceStatus;
    }

    //DEFAULT DEVICE METHODS ONLY

    public synchronized void setDeviceStatus(WarpDeviceStatus status)
    {
        boolean changed = mDeviceStatus != status;
        mDeviceStatus=status;
        if(changed)
        {
            mServicesNames.clear();
            if(mViewObserver != null)
            {
                mViewObserver.onWarpDeviceStatusChanged(this);
            }
        }
    }

    public synchronized void setOperationProgress(int operationProgress)
    {
        boolean changed = mOperationProgress != operationProgress;
        mOperationProgress = operationProgress;
        if(changed && mViewObserver != null)
        {
            mViewObserver.onWarpDeviceOperationProgressChanged(this);
        }
    }

    public synchronized void setOperationLabel(String operationLabel)
    {
        boolean changed = (mCurrentLabel != null && !mCurrentLabel.equals(operationLabel));
        mCurrentLabel = operationLabel;
        if(changed && mViewObserver != null)
        {
            mViewObserver.onWarpDeviceOperationLabelChanged(this);
        }
    }

    @Override
    public synchronized int getDeviceOperationProgress()
    {
        return mOperationProgress;
    }

    @Override
    public synchronized String getDeviceOperationLabel()
    {
        return mCurrentLabel;
    }
}
