package unibo.ing.warp.core.service.base;

import org.json.JSONException;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.IHandler;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpDispatcherLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.warpable.*;
import unibo.ing.warp.utils.WarpUtils;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * User: lorenzodonini
 * Date: 15/11/13
 * Time: 19:14
 */
@WarpServiceInfo(type= WarpServiceInfo.Type.LOCAL, label = "Dispatch", execution =
        WarpServiceInfo.ServiceExecution.CONCURRENT, name="tcpServiceDispatcher",
        launcher = WarpDispatcherLauncher.class, protocol = WarpServiceInfo.Protocol.NONE)
public class WarpTCPDispatcherService extends DefaultWarpService {
    private IWarpEngine warpDrive;
    private String mUserPermissionKey;
    private Object [] mParams;
    private boolean bEnabled;
    public static final int LISTEN_PORT=13837;
    public static final int DEFAULT_SOCKET_TIMEOUT=180000;
    public static final String SERVICE_REQUEST_ACCEPTED="OK";
    public static final String SERVICE_REQUEST_REFUSED="ABORT";
    public static final String EXTRA_PARAMETERS="EXTRA";
    public static final String NO_EXTRA_PARAMETERS="NO_EXTRA";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,2);
        warpDrive=(IWarpEngine)params[0];
        mUserPermissionKey = (String)params[1];
        setContext(context);
        setEnabled(true);

        //Dispatching incoming requests
        Socket request;
        ServerSocket socket = new ServerSocket(LISTEN_PORT);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);

        while(isEnabled())
        {
            try {

                request = socket.accept(); //Receiving incoming connection
            }
            catch (InterruptedIOException e)
            {
                continue;
            }
            EnumSet<WarpFlag> flags = EnumSet.noneOf(WarpFlag.class);
            WarpFlag master = WarpFlag.MASTER;
            master.setValue(false);
            flags.add(master);

            IBeam beam = new WarpBeamTCP(request,flags);
            /*Sequential operation, because we do not want to overuse dedicated threads, even
            in case we were using a thread pool. The handshake process should be quick anyway. */
            WarpServiceInfo info = performHandshake(beam);
            if (info != null)
            {
                startRequestedService(info,beam);
            }
        }
        socket.close();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //LOCAL SERVICE, NO IMPLEMENTATION
    }

    @Override
    public void setWarpServiceHandler(IHandler handler)
    {
        /* The Dispatcher Service doesn't need to return anything, meaning it doesn't need
        to set a local callListener. The called services may need to return something at the
        end, even if they run in background without the user noticing. An example could
        be a Toast message, but since nobody can actually set this callListener, it is disabled
        by default. The optional results will need to be handled inside the service logic.
         */
    }

    @Override
    public Object[] getResult()
    {
        return null;
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[0];  //TODO: To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stopService()
    {
        bEnabled = false;
    }

    private synchronized boolean isEnabled()
    {
        return bEnabled;
    }

    private synchronized void setEnabled(boolean enabled)
    {
        bEnabled=enabled;
    }

    private WarpServiceInfo performHandshake(IBeam warpBeam) throws IOException, JSONException
    {
        IWarpable serviceName = new WarpableString();
        IWarpable response = new WarpableString();
        Class<? extends IWarpService> serviceToStart;
        WarpServiceInfo info = null;

        warpBeam.receiveWarpable(serviceName);
        serviceToStart = warpDrive.getServiceByName((String)serviceName.getValue());
        if(serviceToStart == null)
        {
            /* Theoretically we should start a procedure involving a dynamic retrieval
                of the missing service from the caller, in order to load those classes at runtime
                and still provide the service. For the time being this won't be the case, since
                it is an invasive procedure and definitely not fit for a beta.
                Default behaviour right now is a refusal message sent through the socket.
                 */
            response.setValue(SERVICE_REQUEST_REFUSED);
            warpBeam.beamWarpable(response);
            shutdownBeam(warpBeam);
        }
        else
        {
            /* Services don't run by default on a well known port, since the only way to
                    perform an operation should be through the IWarpEngine. The only thing that
                    still needs to be implemented is a way to obtain the parameters to pass to the
                    remote service.
                     */
            info = WarpUtils.getWarpServiceInfo(serviceToStart);
            if(info == null || info.type() == WarpServiceInfo.Type.LOCAL)
            {
                return null;
            }
            response.setValue(SERVICE_REQUEST_ACCEPTED);
            warpBeam.beamWarpable(response);
            mParams=obtainExtraParameters(warpBeam);
            if(mParams==null)
            {
                response.setValue(SERVICE_REQUEST_REFUSED);
                warpBeam.beamWarpable(response);
                shutdownBeam(warpBeam);
                return null;
            }
            response.setValue(SERVICE_REQUEST_ACCEPTED);
            warpBeam.beamWarpable(response);
        }
        return info;
    }

    private Object [] obtainExtraParameters(IBeam warpBeam) throws IOException, JSONException
    {
        IWarpable extra = new WarpableString();
        IWarpable paramNumber = new WarpableInteger();
        IWarpable paramClass = new WarpableString();
        IWarpable object=null;
        Object params [];

        warpBeam.receiveWarpable(extra);
        if(extra.getValue().equals(EXTRA_PARAMETERS))
        {
            warpBeam.receiveWarpable(paramNumber);
            params=new Object[(Integer)paramNumber.getValue()];
            for(int i=0; i<params.length; i++)
            {
                warpBeam.receiveWarpable(paramClass);
                try {
                    //REFLECTION
                    object = (IWarpable) Class.forName((String)paramClass.getValue()).newInstance();
                }
                catch (InstantiationException e)
                {
                    //NO DEFAULT EMPTY CONSTRUCTOR!
                    return null;
                }
                catch (IllegalAccessException e)
                {
                    e.printStackTrace();
                }
                catch (ClassNotFoundException e)
                {
                    //THE CLASS COULDN'T BE FOUND LOCALLY!
                    return null;
                }
                if(object == null)
                {
                    return null;
                }
                warpBeam.receiveWarpable(object);
                params[i]=object.getValue();
            }
            return params;
        }
        else if(extra.getValue().equals(NO_EXTRA_PARAMETERS))
        {
            params = new Object[0];
            return params;
        }
        return null;
    }

    private void startRequestedService(WarpServiceInfo descriptor, IBeam beam)
    {
        IWarpServiceLauncher launcher = warpDrive.getLauncherForService(descriptor.name());
        launcher.initializeService(WarpResourceLibrary.getInstance(),
                mUserPermissionKey,ServiceOperation.PROVIDE);
        Collection<Object> params = new ArrayList<Object>();
        params.addAll(Arrays.asList(mParams));
        Object [] additionalParameters = launcher.getServiceParameters(null,ServiceOperation.PROVIDE);
        if(additionalParameters != null)
        {
            params.addAll(Arrays.asList(additionalParameters));
        }
        IWarpServiceListener listener = warpDrive.getListenerForService(descriptor.name(),
                launcher.getServiceListenerParameters(null,ServiceOperation.PROVIDE),
                ServiceOperation.PROVIDE);
        if(descriptor.type() == WarpServiceInfo.Type.PUSH)
        {
            warpDrive.callPushService(descriptor.name(),null,listener,beam,params.toArray(
                    new Object[params.size()]),null);
        }
        else if(descriptor.type() == WarpServiceInfo.Type.PULL)
        {
            warpDrive.callPullService(descriptor.name(),null,listener,beam,params.toArray(
                    new Object[params.size()]),null);
        }
    }

    private void shutdownBeam(IBeam warpBeam)
    {
        try{
            if(warpBeam != null)
            {
                warpBeam.disable();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
