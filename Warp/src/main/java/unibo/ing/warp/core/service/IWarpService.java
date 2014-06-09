package unibo.ing.warp.core.service;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IHandler;

/**
 * Created by cronic90 on 06/10/13.
 *
 * The WarpServiceInfo annotation needs to be specified in order to be used properly by
 * IWarpEngine implementations. If no type is specified inside the annotation, the
 * service will be considered a local one by default.
 */
public interface IWarpService {
    /**
     * Calls a custom client service, that takes an IBeam Object as a parameters in order
     * to perform an endpoint to endpoint operation. The method is always invoked on the side
     * requesting the service, meaning that it is an user-triggered action.
     * The method only needs to implement the service logic, without having to worry about the
     * connection, which has already been established by the two IWarpEngines, nor about the
     * communication policy, since the IBeam Object will handle the low-level calls.
     *
     * Local Services always call this method when a service is invoked.
     *
     * By default, the IWarpEngine creates an AsyncTask (calling HandshakeService), whenever a remote
     * service needs to be invoked. This means that remote procedures are implicitly handled asynchronously,
     * while Local services are not handled in any way, thus the developer needs to implement Threads
     * on his own in case the Local service happened to be a long task.
     *
     * @param warpBeam  The IBeam responsible for calling the communication primitives
     * @param context  The Application Context, needed by services who want to access system services etc
     * @param params  The optional parameters passed to the service
     */
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception;

    /**
     * Calls a custom servant service, that takes an IBeam Object representing the communication
     * channel, needed to perform an endpoint to endpoint operation. The method is always invoked
     * on the side providing the service, which executes this procedure passively, without
     * direct user intervention.
     * When this procedure is called, the connection to the client has already been established,
     * thus the method only needs to implement the service logic, without having to worry about the
     * connection nor the communication policy.
     *
     * Depending on the implementation of the IWarpEngine, this invocation should already be done
     * inside a background AsyncTask, meaning no further Threading needs to be used. This is the
     * default behaviour, therefore the call is performed asynchronously.
     *
     * Local Services don't need to implement any logic in this method, since the only service they
     * offer is a local one, invoked directly by the user.
     *
     * THREADS????
     *
     * @param warpBeam  The IBeam responsible for calling the communication primitives
     * @param context  The Application Context, needed by services who want to access system services etc
     * @param params  The optional parameters passed to the service
     */
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception;

    public void stopService();

    /**
     * Sets an IWarpServiceListener Object inside the IWarpService. This callListener object
     * will automatically receive a callback from the service once the procedure is
     * completed. This serves the purpose of giving the caller object the possibility of
     * retrieving an asynchronous result from the IWarpService object itself.
     * In case the service doesn't need to return a result, the callListener
     * can simply be left empty, and no callback will be triggered.
     *
     * @param handler The Object implementing the IWarpServiceListener interface
     */
    public void setWarpServiceHandler(IHandler handler);
    /**
     * Returns the result of any kind, according to the service's implementation. The service
     * doesn't necessarily have to return something, but in case it does, the user should retrieve
     * the result by calling this method asynchronously.
     * The method should be called after receiving an IWarpServiceListener notification, previously
     * set via the appropriate method.
     *
     * @return  Returns any result the service may have produced during it's subroutine. In case
     * the service didn't produce any results, null is returned.
     */
    public IHandler getWarpServiceHandler();

    public Object [] getResult();

    public Object [] getCurrentProgress();

    public int getCurrentPercentProgress();

    public ServiceStatus getServiceStatus();

    public enum ServiceStatus {
        UNKNOWN,
        INACTIVE,
        HANDSHAKING,
        RUNNING,
        ABORTED,
        COMPLETED
    }

    public enum ServiceOperation {
        CALL,
        PROVIDE
    }
}
