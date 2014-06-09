package unibo.ing.warp.core.service.launcher;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by Lorenzo Donini on 5/30/2014.
 */
public abstract class DefaultWarpServiceLauncher implements IWarpServiceLauncher {
    private boolean bListenerSet = false;
    private IWarpServiceInitializationListener mListener;

    public final void setInitializationListener(IWarpServiceInitializationListener listener)
    {
        if(!bListenerSet)
        {
            mListener = listener;
            bListenerSet = true;
        }
    }

    public final void onServiceInitialized()
    {
        mListener.onInitializationCompleted();
    }

    @Override
    public void initializeService(WarpResourceLibrary library, String permissionKey,
                                  IWarpService.ServiceOperation operation)
    {
        if(operation == IWarpService.ServiceOperation.CALL)
        {
            initializeCallService(library,permissionKey);
        }
        else if(operation == IWarpService.ServiceOperation.PROVIDE)
        {
            initializeProvideService(library,permissionKey);
        }
    }

    @Override
    public Object [] getServiceListenerParameters(IWarpInteractiveDevice target,
                                                  IWarpService.ServiceOperation operation)
    {
        if(operation == IWarpService.ServiceOperation.CALL)
        {
            return getCallServiceListenerParameters(target);
        }
        else if(operation == IWarpService.ServiceOperation.PROVIDE)
        {
            return getProvideServiceListenerParameters(target);
        }
        return null;
    }

    @Override
    public Object [] getServiceParameters(IWarpInteractiveDevice target,
                                                  IWarpService.ServiceOperation operation)
    {
        if(operation== IWarpService.ServiceOperation.CALL)
        {
            return getCallServiceParameters(target);
        }
        else if(operation== IWarpService.ServiceOperation.PROVIDE)
        {
            return getProvideServiceParameters(target);
        }
        return null;
    }

    protected abstract void initializeCallService(WarpResourceLibrary library, String permissionKey);
    protected abstract void initializeProvideService(WarpResourceLibrary library, String permissionKey);
    protected abstract Object [] getCallServiceListenerParameters(IWarpInteractiveDevice target);
    protected abstract Object [] getProvideServiceListenerParameters(IWarpInteractiveDevice target);
    protected abstract Object [] getCallServiceParameters(IWarpInteractiveDevice target);
    protected abstract Object [] getProvideServiceParameters(IWarpInteractiveDevice target);
}
