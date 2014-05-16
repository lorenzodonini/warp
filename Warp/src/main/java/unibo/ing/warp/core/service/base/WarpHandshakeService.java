package unibo.ing.warp.core.service.base;

import org.json.JSONException;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.IHandler;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.core.warpable.WarpableInteger;
import unibo.ing.warp.core.warpable.WarpableString;
import unibo.ing.warp.utils.WarpUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumSet;

/**
 * User: lorenzodonini
 * Date: 16/11/13
 * Time: 01:34
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, target = WarpServiceInfo.Target.ALL, name="warpHandshake")
public class WarpHandshakeService extends DefaultWarpService {
    private IBeam mWarpBeam =null;
    private String mErrorMessage = "TEST";
    private static final String HANDSHAKE_FAILED = "WarpHandshakeService.callService: Handshaking failed!";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        /*Note: the params passed here are the actual parameters needed by the
        service that is going to be called by the Handshake service, plus some
         parameters needed in order to establish a connection to the remote node*/
        checkOptionalParameters(params,4);

        IWarpEngine localWarpDrive = (IWarpEngine)params[0];
        WarpLocation remoteWarpDeviceLocation = (WarpLocation)params[1];
        IWarpService serviceToCall = (IWarpService)params[2];
        IWarpable serviceParams [] = (IWarpable [])params[3];

        Socket socket;
        InetAddress address = remoteWarpDeviceLocation.getIPv4Address();
        if(address == null)
        {
            String stringAddress = remoteWarpDeviceLocation.getStringIPv4Address();
            byte [] rawAddress = remoteWarpDeviceLocation.getRawIPv4Address();
            if(stringAddress != null)
            {
                address = InetAddress.getByName(stringAddress);
            }
            else if(rawAddress != null)
            {
                address = InetAddress.getByAddress(rawAddress);
            }
            else
            {
                //TODO: needs to be a specific exception
                throw new Exception("WarpHandshakeService.callService: no IP address is set!");
            }
        }

        socket = new Socket(address,WarpDispatcherService.LISTEN_PORT);

        EnumSet<WarpFlag> flags = EnumSet.noneOf(WarpFlag.class);
        WarpFlag master = WarpFlag.MASTER;
        master.setValue(true);
        flags.add(master);

        mWarpBeam = new WarpBeam(socket,localWarpDrive,flags);

        if(performServiceRequest(mWarpBeam, serviceToCall))
        {
            //Launching actual service!
            if(!provideExtraParameters(mWarpBeam, serviceParams))
            {
                throw new Exception(HANDSHAKE_FAILED);
            }
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //NOTHING TO BE IMPLEMENTED SINCE THIS IS A LOCAL SERVICE
    }

    @Override
    public Object[] getResult()
    {
        return (mWarpBeam != null)? new Object[] {mWarpBeam} : new Object[] {mErrorMessage};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return null;
    }

    @Override
    public void setWarpServiceHandler(IHandler handler)
    {
        //DO NOTHING
    }

    private boolean performServiceRequest(IBeam beam, IWarpService remoteService) throws IOException, JSONException
    {
        IWarpable serviceName=null, response;

        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(remoteService.getClass());
        if(info != null)
        {
            serviceName= new WarpableString(info.name());
        }
        else
        {
            return false;
        }

        beam.beamWarpable(serviceName);
        response=new WarpableString();
        beam.receiveWarpable(response);
        return (response.getValue().equals(WarpDispatcherService.SERVICE_REQUEST_REFUSED));
    }

    private boolean provideExtraParameters(IBeam beam, IWarpable params [])
            throws IOException, JSONException {
        IWarpable paramClass = new WarpableString();
        IWarpable paramNum = new WarpableInteger();
        IWarpable extra = new WarpableString();
        IWarpable object, response;

        if(params==null || params.length==0)
        {
            //NO PARAMETERS
            extra.setValue(WarpDispatcherService.NO_EXTRA_PARAMETERS);
            beam.beamWarpable(extra);
        }
        else
        {
            //EXTRA PARAMETERS TO BE SENT
            extra.setValue(WarpDispatcherService.EXTRA_PARAMETERS);
            beam.beamWarpable(extra);

            paramNum.setValue(params.length);
            beam.beamWarpable(paramNum);

            for (IWarpable param : params)
            {
                object = param;
                paramClass.setValue(object.getValue().getClass().getName()); //TODO: HANDLE CLASS NAME
                beam.beamWarpable(paramClass);
                beam.beamWarpable(object);
            }
        }

        response = new WarpableString();
        beam.receiveWarpable(response);
        return(response.getValue()!=null &&
                (response.getValue()).equals(WarpDispatcherService.SERVICE_REQUEST_ACCEPTED));
    }
}
