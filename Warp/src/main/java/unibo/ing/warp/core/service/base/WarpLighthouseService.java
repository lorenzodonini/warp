package unibo.ing.warp.core.service.base;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Lorenzo Donini on 5/17/2014.
 */
@WarpServiceInfo(type= WarpServiceInfo.Type.LOCAL, label="Lighthouse", execution =
        WarpServiceInfo.ServiceExecution.CONCURRENT,name="lightHouse")
public class WarpLighthouseService extends DefaultWarpService {
    public static final int LISTEN_PORT=13837;
    public static final int DEFAULT_SOCKET_TIMEOUT=1800000;
    public static final byte BEACON_PING = 33;
    public static final int PACKET_SIZE = 4096;
    private boolean bEnabled;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //TODO: TO TEST
        checkOptionalParameters(params,1);
        IWarpEngine warpDrive = (IWarpEngine)params[0];
        setEnabled(true);
        byte [] buf = new byte[4096];
        byte [] data;
        StringBuilder builder = new StringBuilder();
        Collection<String> activeServiceNames = warpDrive.getServicesNames();
        for(String serviceName : activeServiceNames)
        {
            builder.append(serviceName);
            builder.append(";");
        }
        builder.setLength(PACKET_SIZE);
        data = builder.toString().getBytes("UTF-8");
        DatagramPacket packet = new DatagramPacket(buf,buf.length);
        DatagramSocket socket = new DatagramSocket(LISTEN_PORT);

        //Starting service
        while (isEnabled())
        {
            try {
                socket.receive(packet);
            }
            catch (InterruptedIOException e)
            {
                continue;
            }
            buf = packet.getData();
            if(buf != null && buf.length >= 1 && buf[0]==BEACON_PING)
            {
                packet.setData(data);
                socket.send(packet);
            }
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //It's a local service, so no service can be provided!
    }

    private synchronized void setEnabled(boolean enabled)
    {
        bEnabled=enabled;
    }

    private synchronized boolean isEnabled()
    {
        return bEnabled;
    }

    public void stopService()
    {
        setEnabled(false);
    }

    @Override
    public Object[] getResult()
    {
        return null;
    }

    @Override
    public synchronized Object[] getCurrentProgress()
    {
        return null;
    }
}
