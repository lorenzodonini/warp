package unibo.ing.warp.core.android;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpBeamObserver;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.IBeamHandler;
import unibo.ing.warp.core.IHandler;

import java.io.IOException;

/**
 * Created by Lorenzo Donini on 4/17/2014.
 */
public abstract class AndroidHandler implements IHandler, IBeamHandler {
    private IWarpServiceListener mListener;
    private IWarpBeamObserver mContainer;
    private boolean bCreated;
    private IWarpService.ServiceStatus mStatus;

    public AndroidHandler(IWarpServiceListener listener, IWarpBeamObserver observer)
    {
        mListener=listener;
        mContainer=observer;
        bCreated=false;
        mStatus= IWarpService.ServiceStatus.INACTIVE;
    }

    @Override
    public void onBeamCreate(IBeam warpBeam, IWarpService service)
    {
        if(!bCreated)
        {
            bCreated=true;
            if(warpBeam!=null && warpBeam.isEnabled())
            {
                mContainer.onBeamCreate(warpBeam,service);
            }
        }
    }

    @Override
    public void completeService(IBeam warpBeam, IWarpService servant)
    {
        if(mStatus != IWarpService.ServiceStatus.COMPLETED)
        {
            setHandledServiceStatus(IWarpService.ServiceStatus.COMPLETED);
            if(warpBeam != null && warpBeam.isEnabled())
            {
                try {
                    warpBeam.disable();
                    mContainer.onBeamDestroy(warpBeam,servant);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            onServiceCompleted(servant);
        }
    }

    @Override
    public void abortService(IBeam warpBeam, IWarpService servant, String message)
    {
        if(mStatus != IWarpService.ServiceStatus.ABORTED)
        {
            setHandledServiceStatus(IWarpService.ServiceStatus.ABORTED);
            if(warpBeam != null && warpBeam.isEnabled())
            {
                try {
                    warpBeam.disable();
                    mContainer.onBeamDestroy(warpBeam,servant);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            onServiceAbort(message);
        }
    }

    @Override
    public synchronized IWarpService.ServiceStatus getHandledServiceStatus()
    {
        return (mStatus!=null)? mStatus : IWarpService.ServiceStatus.UNKNOWN;
    }

    public synchronized void setHandledServiceStatus(IWarpService.ServiceStatus status)
    {
        mStatus=status;
    }

    protected IWarpServiceListener getServiceListener()
    {
        return mListener;
    }

    protected abstract void onServiceCompleted(final IWarpService servant);
    protected abstract void onServiceAbort(final String message);
}
