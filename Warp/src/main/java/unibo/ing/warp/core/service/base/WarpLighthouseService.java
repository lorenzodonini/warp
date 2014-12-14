package unibo.ing.warp.core.service.base;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpLighthouseLauncher;
import unibo.ing.warp.utils.WarpUtils;
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
    public static final int DEFAULT_SOCKET_TIMEOUT=60000;
    public static final byte BEACON_PING = 33;
    public static final int PACKET_SIZE = 4096;
    public static final byte JOIN_PING = 32;
    private static final String MULTICAST_LOCK_TAG = "warp.unibo.it";
    private boolean bEnabled;
    private MulticastSocket mBroadcastSocket;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,1);
        IWarpEngine warpDrive = (IWarpEngine)params[0];
        setContext(context);
        setEnabled(true);

        byte [] ping = new byte[1]; //Broadcast Packet
        byte [] data;  //Unicast Packet
        InetAddress ipAddress, reverseIp;
        StringBuilder builder = new StringBuilder();
        Collection<String> activeServiceNames = warpDrive.getServicesNames();
        for(String serviceName : activeServiceNames)
        {
            builder.append(serviceName);
            builder.append(';');
        }
        builder.append('#');
        builder.setLength(PACKET_SIZE);
        data = builder.toString().getBytes("UTF-8");

        //Configuring multicast lock
        WifiManager wifi = (WifiManager) ((Context)getContext()).getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
        ipAddress = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromInt(
                wifi.getConnectionInfo().getIpAddress()));
        reverseIp = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromIntReversed(
                wifi.getConnectionInfo().getIpAddress()));

        //Setting up the broadcast socket
        mBroadcastSocket = new MulticastSocket(LISTEN_PORT);
        mBroadcastSocket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
        DatagramPacket broadcastPacket = new DatagramPacket(ping,ping.length);
        DatagramPacket unicastPacket = new DatagramPacket(data,PACKET_SIZE);

        //Starting service
        lock.acquire();
        while (isEnabled())
        {
            try {
                //Setting up the Datagram Packet in order to receive on Broadcast Address
                mBroadcastSocket.receive(broadcastPacket);
                Log.d("WARP.DEBUG","WarpBaconService: Broadcast received from "
                        +broadcastPacket.getAddress().getHostAddress());
            }
            catch (InterruptedIOException e)
            {
                continue;
            }
            catch (Exception e)
            {
                Log.d("WARP.DEBUG",Thread.currentThread().getName()+" - Lighthouse socket was forcefully closed!");
            }
            if(isEnabled())
            {
                String senderAddress = broadcastPacket.getAddress().getHostAddress();
                if(senderAddress.equals(ipAddress.getHostAddress()) ||
                        senderAddress.equals(reverseIp.getHostAddress()))
                {
                    //We don't want to answer our own request!!
                    Log.d("WARP.DEBUG",
                            Thread.currentThread().getName()+" - Dropped Incoming packet from "+senderAddress);
                    continue;
                }
                if(broadcastPacket.getLength() == 1)
                {
                    if(broadcastPacket.getData()[0]==BEACON_PING)
                    {
                        //Setting up the Datagram Packet in order to send to a single host (Unicast)
                        unicastPacket.setAddress(broadcastPacket.getAddress());
                        unicastPacket.setPort(broadcastPacket.getPort());
                        mBroadcastSocket.send(unicastPacket);
                        Log.d("WARP.DEBUG","WarpBaconService: Unicast response sent to "
                                +unicastPacket.getAddress().getHostAddress());
                    }
                    else if(broadcastPacket.getData()[0]==JOIN_PING)
                    {
                        //TODO: THINK OF A PLAUSIBLE SOLUTION! GIMP!
                    }
                }
            }
        }
        lock.release();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //It's a local service, so no service can be provided!
    }

    /*private String getBroadcastAddress(String ipAddress, String reverseIpAddress) throws SocketException
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();)
        {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback())
            {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses())
                {
                    //The found interface must be the one relative to the passed IP address
                    String address = interfaceAddress.getAddress().getHostAddress();
                    if(address.equals(ipAddress) || address.equals(reverseIpAddress))
                    {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if(broadcast != null)
                        {
                            return broadcast.toString().substring(1);
                        }
                    }
                }
            }
        }
        return null;
    }*/

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
        mBroadcastSocket.close();
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
