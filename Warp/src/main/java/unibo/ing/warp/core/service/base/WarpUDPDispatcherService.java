package unibo.ing.warp.core.service.base;

import org.json.JSONException;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.service.DefaultWarpService;
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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, label = "Dispatch", execution =
        WarpServiceInfo.ServiceExecution.CONCURRENT, name = "udpServiceDispatcher",
        launcher = WarpDispatcherLauncher.class)
public class WarpUDPDispatcherService extends DefaultWarpService {
    private IWarpEngine warpDrive;
    private boolean bEnabled;
    private Object [] mParams;
    private String mUserPermissionKey;
    public static final int LISTEN_PORT=13837;
    public static final int DEFAULT_SOCKET_TIMEOUT=1800000;
    public static final String SERVICE_REQUEST_ACCEPTED="OK";
    public static final String SERVICE_REQUEST_REFUSED="ABORT";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,2);
        warpDrive = (IWarpEngine)params[0];
        mUserPermissionKey = (String)params[1];
        setContext(context);
        setEnabled(true);

        //Dispatching incoming requests
        DatagramSocket socket = new DatagramSocket(LISTEN_PORT);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
        byte [] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

        while (isEnabled())
        {
            try {
                socket.receive(packet);
            }
            catch (InterruptedIOException e)
            {
                continue;
            }
            EnumSet<WarpFlag> flags = EnumSet.noneOf(WarpFlag.class);
            WarpFlag master = WarpFlag.MASTER;
            master.setValue(false);
            flags.add(master);

            /*Sequential operation, because we do not want to overuse dedicated threads, even
            in case we were using a thread pool. The handshake process should be quick anyway. */
            DatagramSocket serviceDedicatedSocket = new DatagramSocket();
            WarpLocation location = new WarpLocation(packet.getAddress());
            location.setPort(packet.getPort());
            IBeam beam = new WarpBeamUDP(serviceDedicatedSocket,location,flags);
            WarpServiceInfo info = performHandshake(beam,packet,serviceDedicatedSocket.getLocalPort());
            if(info != null)
            {
                if(info.type() == WarpServiceInfo.Type.PUSH)
                {
                    //TODO: add proper listeners!!
                    warpDrive.callPushService(info.name(),null,null,warpBeam,mParams,null);
                }
                else if(info.type() == WarpServiceInfo.Type.PULL)
                {
                    warpDrive.callPullService(info.name(),null,null,warpBeam,mParams,null);
                }
            }
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing
    }

    @Override
    public synchronized void stopService()
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

    @Override
    public Object[] getResult() {
        return new Object[0];
    }

    @Override
    public Object[] getCurrentProgress() {
        return new Object[0];
    }

    private WarpServiceInfo performHandshake(IBeam warpBeam, DatagramPacket packet, int newPort)
            throws JSONException, IOException
    {
        IWarpable serviceName = new WarpableString();
        IWarpable responseMessage;
        WarpableUDPResponse response;
        WarpServiceInfo info=null;
        serviceName.warpFrom(packet.getData());

        Class<? extends IWarpService> serviceToStart = warpDrive.getServiceByName
                ((String)serviceName.getValue());
        if(serviceToStart == null)
        {
            response = new WarpableUDPResponse(SERVICE_REQUEST_REFUSED,-1);
            response.setValue(SERVICE_REQUEST_REFUSED);
            warpBeam.beamWarpable(response);
            shutdownBeam(warpBeam);
        }
        else
        {
            info = WarpUtils.getWarpServiceInfo(serviceToStart);
            if(info == null || info.type() == WarpServiceInfo.Type.LOCAL)
            {
                return null;
            }
            response = new WarpableUDPResponse(SERVICE_REQUEST_ACCEPTED,newPort);
            warpBeam.beamWarpable(response);
            mParams = obtainExtraParameters(warpBeam);
            if(mParams == null)
            {
                responseMessage = new WarpableString(SERVICE_REQUEST_REFUSED);
                warpBeam.beamWarpable(responseMessage);
                shutdownBeam(warpBeam);
                return null;
            }
            responseMessage = new WarpableString(SERVICE_REQUEST_ACCEPTED);
            warpBeam.beamWarpable(responseMessage);
        }
        return info;
    }

    private Object [] obtainExtraParameters(IBeam warpBeam) throws IOException, JSONException
    {
        IWarpable extra = new WarpableUDPRequest();
        warpBeam.receiveWarpable(extra);

        Object value = extra.getValue();
        if(value == null)
        {
            return null;
        }
        WarpableParameter [] parameters = (WarpableParameter [])value;
        Object [] result = new Object[parameters.length];
        IWarpable parameter;
        for(int i=0; i<parameters.length; i++)
        {
            parameter = (IWarpable) parameters[i].getValue();
            result[i]=parameter.getValue();
        }
        return result;
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
