package unibo.ing.warp.core.android;

import unibo.ing.warp.core.IWarpBeamObserver;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/2/2014.
 */
public class AndroidSequentialHandler extends AndroidHandler {

    public AndroidSequentialHandler(IWarpServiceListener listener, IWarpBeamObserver observer)
    {
        super(listener,observer);
    }

    @Override
    protected void onServiceCompleted(IWarpService servant)
    {
        IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            listener.onServiceCompleted(servant);
        }
    }

    @Override
    protected void onServiceAbort(String message)
    {
        IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            listener.onServiceAbort(message);
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            listener.onServiceProgressUpdate(servant);
        }
    }

    @Override
    public void onServiceStatusChanged(IWarpService servant)
    {
        //TODO: THINK OF SOMETHING
    }
}
