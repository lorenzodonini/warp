package unibo.ing.warp.core;

import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.warpable.IWarpable;

import java.util.Collection;

/**
 * Created by cronic90 on 06/10/13.
 *
 * This basic interface needs to be implemented by the main WarpEngine, which will be
 * used to call services and warp IWarpable Objects from one endpoint to another.
 * The IWarpEngine must contain the services available on the local endpoint.
 *
 * In order to perform complex operations, this interface relies on another interface,
 * called IWarpService. A WarpEngine contains a variable number of services.
 * Each service needs to implement the required behaviour, and can then be called
 * just by knowing the Service's name.
 *
 * IWarpEngine can in fact warp IWarpable's from one endpoint to another, without
 * knowing the underlying network structure or communication means used; those will be
 * implemented inside the IWarpService's and the classes they access.
 */
public interface IWarpEngine {
    //TODO: UPDATE ALL DOC!!
    public Object getContext();

    /**
     * Returns the List of all Service Names this IWarpEngine offers, much like a server.
     *
     * @return  Returns the List of the Service names active on the endpoint.
     * Works for both local and remote IWarpEngines
     */
    public Collection<String> getServicesNames();

    public Collection<Class<? extends IWarpService>> getServiceList();

    /**
     * If an implementation of IWarpEngine doesn't add specific services in the
     * constructor, these can be added at runtime calling this method.
     *
     * @param serviceClass  The new service to add to the list of the services the Warpable Object
     *                 offers
     */
    public void addWarpService(Class<? extends IWarpService> serviceClass);

    /**
     * Calls a Local Service, which doesn't need to know the other endpoint since it won't
     * be used. To standardize, Local Services are IWarpServices as well, in order to
     * be able to subclass services and make them remote without too much effort.
     *
     * @param serviceName  The name of the service that needs to be invoked
     * @param listener  The optional callListener that needs to be set inside the IWarpService,
     *                  in order to perform an asynchronous callback once the service has ended
     * @param params  The optional paramaters passed to the service
     */
    public void callLocalService(String serviceName, IWarpServiceListener listener,
                                 Object[] params);

    /**
     * Returns the List of all Services this IWarpEngine offers. Needs to be used locally,
     * since it returns the actual IWarpService instance pool stored inside the IWarpEngine.
     *
     * @return  Returns the List of the Services base instances on the local endpoint. All
     * Services are returned, Local, Pull and Push. Returns null if the IWarpEngine is remote
     */
    public long [] getActiveServicesIdsByName(String serviceName);
    /**
     * Returns the IWarpService identified by the name passed as a parameter. The lookup
     * procedure is performed on the Services data structure contained in the IWarpEngine.
     *
     * @param name  The name of the service, as it appears in the IWarpServiceInfo annotation
     * @return  Returns the IWarpService referenced by the input. Returns null in case
     * the service doesn't exist
     */
    public Class<? extends IWarpService> getServiceByName(String name);

    /**
     * Calls a Push Service, identified by a sevice name that needs to be passed as
     * a parameter. Being a Push Service, the IWarpService that needs to be called must
     * be a Push Service, with the IWarpEngine destination passed as a parameter working as
     * a passive endpoint, while the source endpoint makes a request.
     *
     * @param serviceName  The name of the service that needs to be invoked
     * @param to  Destination IWarpDevice Object, needed for the Push operation
     * @param listener  The optional callListener that needs to be set inside the IWarpService,
     *                  in order to perform an asynchronous callback once the service has ended
     * @param params  The optional paramaters passed to the service
     */
    public void callPushService(String serviceName, IWarpDevice to, IWarpServiceListener listener,
                                IBeam warpBeam, Object[] params, IWarpable[] remoteParams);

    /**
     * Calls a Pull Service, indentified by a service name that needs to be passed as
     * a parameter. Being a Pull Service, the IWarpService that needs to be called must
     * be a Pull Service, with the IWarpEngine source passed as a parameter and working as
     * an active endpoint that makes a request to the passive destination.
     *
     * @param serviceName  The name of the service that needs to be invoked
     * @param from  Source IWarpDevice Object, needed in order to perform a Pull
     *              operation towards the current IWarpEngine Object
     * @param listener  The optional callListener that needs to be set inside the IWarpService,
     *                  in order to perform an asynchronous callback once the service has ended
     * @param params  The optional paramaters passed to the service
     */
    public void callPullService(String serviceName, IWarpDevice from, IWarpServiceListener listener,
                                IBeam warpBeam, Object[] params, IWarpable[] remoteParams);

    public void stopService(long serviceId);

    public void startEngine();
    public void stopEngine();

    public IWarpServiceListener getListenerForService(String serviceName, Object[] values,
                                               IWarpService.ServiceOperation operation);

    public IWarpServiceLauncher getLauncherForService(String serviceName);
}
