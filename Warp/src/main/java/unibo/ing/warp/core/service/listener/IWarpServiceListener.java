package unibo.ing.warp.core.service.listener;

import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by cronic90 on 11/10/13.
 *
 * This interface needs to implemented in case an IWarpEngine object wanted to receive a result
 * from a service that was just called. As for every callListener, it is possible to either implement
 * the whole interface and dispatch the requests inside the callback method (e.g. using a switch),
 * or by implementing the method inline for each called service.
 */
public interface IWarpServiceListener {
    /**
     * This is the only method that needs to be implemented by the callListener. Its' purpose is to
     * asynchronously perform a callback action on the calling object, once the service is
     * completed. After the callListener is triggered, it is possible to retrieve the result
     * Objects from the IWarpService, passed as an argument.
     * Note that retrieving the result from the service object should always be done by implementing
     * this method, in order to be sure that the service's procedure is finished and not still
     * running.
     *
     * @param servant The service object that just finished running
     */
    public void onServiceCompleted(IWarpService servant);

    /**
     *
     * @param servant
     */
    public void onServiceProgressUpdate(IWarpService servant);

    public void onServiceAbort(String message);
}
