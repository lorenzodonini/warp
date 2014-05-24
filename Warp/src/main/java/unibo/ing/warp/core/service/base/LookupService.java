package unibo.ing.warp.core.service.base;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.core.warpable.WarpableInteger;
import unibo.ing.warp.core.warpable.WarpableString;
import java.util.Collection;
/**
 * User: lorenzodonini
 * Date: 23/11/13
 * Time: 18:52
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.PULL, name = "lookup", label = "Lookup")
public class LookupService extends DefaultWarpService {
    private String [] result;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        IWarpable serviceName = new WarpableString();
        IWarpable serviceNum = new WarpableInteger();

        warpBeam.receiveWarpable(serviceNum);
        if(serviceNum.getValue()==null)
        {
            setPercentProgress(-1);
            throw new Exception("LookupService.callService: expected a serviceNum, received null!");
        }
        result=new String[(Integer)serviceNum.getValue()];
        for(int i=0; i<result.length; i++)
        {
            warpBeam.receiveWarpable(serviceName);
            if(serviceName.getValue()==null)
            {
                result[i]=(String)serviceName.getValue();
            }
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        IWarpable serviceName = new WarpableString();
        IWarpable serviceNum = new WarpableInteger();
        Collection<String> services = warpBeam.getLocalWarpEngine().getServicesNames();

        serviceNum.setValue(services.size());
        warpBeam.beamWarpable(serviceNum);

        for(String service: services)
        {
            serviceName.setValue(service);
            warpBeam.beamWarpable(serviceName);
        }
    }

    @Override
    public Object[] getResult()
    {
        //Returns a result only to the direct caller
        return new Object[] {result};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return null;
    }
}
