package unibo.ing.warp.core.service.base;

import android.content.Context;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpLighthouseLauncher;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;

/**
 * Created by Lorenzo Donini on 5/17/2014.
 */
@WarpServiceInfo(type= WarpServiceInfo.Type.LOCAL, label="Lighthouse", execution =
        WarpServiceInfo.ServiceExecution.CONCURRENT, name="lightHouse", target = WarpServiceInfo.Target.ANDROID,
        launcher = WarpLighthouseLauncher.class, protocol = WarpServiceInfo.Protocol.NONE)
public class WarpLighthouseService extends DefaultWarpService {
    public static final int LISTEN_PORT=13838;
    public static final int DEFAULT_SOCKET_TIMEOUT=180000;
    public static final byte BEACON_PING = 33;
    public static final int PACKET_SIZE = 4096;
    private static final String MULTICAST_LOCK_TAG = "warp.unibo.it";
    private boolean bEnabled;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,1);
        IWarpEngine warpDrive = (IWarpEngine)params[0];
        setContext(context);
        setEnabled(true);

        byte [] buf = new byte[PACKET_SIZE];
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
        //Setting up the broadcast socket
        DatagramSocket broadcastSocket = new DatagramSocket(LISTEN_PORT);
        broadcastSocket.setBroadcast(true);
        broadcastSocket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
        InetAddress broadcastAddress = InetAddress.getByName(getBroadcastAddress());
        DatagramPacket packet = new DatagramPacket(buf,buf.length);

        //Starting service
        WifiManager wifi = (WifiManager) ((Context)getContext()).getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
        lock.acquire();
        while (isEnabled())
        {
            try {
                //Setting up the Datagram Packet in order to receive on Broadcast Address
                packet.setPort(LISTEN_PORT);
                packet.setAddress(broadcastAddress);
                broadcastSocket.receive(packet);
            }
            catch (InterruptedIOException e)
            {
                continue;
            }
            buf = packet.getData();
            if(buf != null && buf.length == 1 && buf[0]==BEACON_PING)
            {
                //Setting up the Datagram Packet in order to send to a single host (Unicast)
                packet.setData(data);
                packet.setAddress(packet.getAddress());
                packet.setPort(packet.getPort());
                broadcastSocket.send(packet);
            }
        }
        lock.release();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //It's a local service, so no service can be provided!
    }

    private String getBroadcastAddress() throws SocketException
    {
        //TODO: MUTUATED CODE! CHECK IT
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();)
        {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback())
            {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses())
                {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if(broadcast != null)
                    {
                        return broadcast.toString().substring(1);
                    }
                }
            }
        }
        return null;
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
