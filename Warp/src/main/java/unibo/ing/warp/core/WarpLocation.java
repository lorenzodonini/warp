package unibo.ing.warp.core;

import unibo.ing.warp.utils.WarpUtils;
import java.net.InetAddress;

/**
 * User: lorenzodonini
 * Date: 18/11/13
 *
 * Utility class needed by an IWarpEngine. It contains the node's IPv4 address.
 * The class offers a vast set of Constructors and Setters, in order to be able
 * to set the IPv4 address from either an int, a String, a byte array or an
 * already given InetAddress object.
 *
 * The class is also useful for knowing if the engine is local or not. In case the
 * engine is local, the default Constructor is usually called, which sets the Ipv4
 * address for localhost/127.0.0.1. In case the the engine is remote, the default
 * constructor shouldn't be called, or a setter method with the proper address
 * should be invoked right after instantiating the WarpLocation object.
 */
public class WarpLocation {
    private InetAddress mIPv4Address;
    private String mStringIPv4;
    private byte [] mByteIPv4;
    private int mPort;
    public static final String LOCAL_ADDRESS="localhost";

    public WarpLocation()
    {
        mIPv4Address=null;
    }

    public WarpLocation(InetAddress addr)
    {
        mIPv4Address=addr;
    }

    public WarpLocation(int addr)
    {
        setIPv4Address(addr);
    }

    public WarpLocation(String addr)
    {
        setIPv4Address(addr);
    }

    public WarpLocation(byte addr [])
    {
        setIPv4Address(addr);
    }

    public synchronized void setPort(int port)
    {
        mPort = (port >= 1024 && port < 65536) ? port : -1;
    }

    public synchronized int getPort()
    {
        return mPort;
    }

    /**
     * Sets IPv4 address given the InetAddress object containing the address.
     *
     * @param addr  The InetAddress to be assigned to the address field
     */
    public synchronized void setIPv4Address(InetAddress addr)
    {
        mIPv4Address=addr;
    }

    /**
     * Sets IPv4 address given the 32-bit integer value of the address.
     *
     * @param addr  The int representation of the IP address
     */
    public synchronized void setIPv4Address(int addr)
    {
        setIPv4Address(WarpUtils.getRawIPv4AddressFromInt(addr));
    }

    /**
     * Sets IPv4 address given the byte array value of the address.
     *
     * @param addr  The byte array representation of the IP address
     */
    public synchronized void setIPv4Address(byte addr [])
    {
        mByteIPv4=addr;
    }

    /**
     * Sets IPv4 address given String containing the value of the address.
     *
     * @param addr  The String representation of the IP address (e.g. "192.168.0.1")
     */
    public synchronized void setIPv4Address(String addr)
    {
        mStringIPv4=addr;
    }

    /**
     * Getter method to obtain the current WarpLocation IPv4 address. The address
     * is returned as an InetAddress object.
     *
     * @return  Returns the InetAddress. Returns null if no IPv4 was set
     */
    public synchronized InetAddress getIPv4Address()
    {
        return mIPv4Address;
    }

    public synchronized String getStringIPv4Address() { return mStringIPv4; }

    public synchronized byte [] getRawIPv4Address() { return mByteIPv4; }
}
