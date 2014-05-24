package unibo.ing.warp.core.service;

import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;

import java.lang.annotation.*;

/**
 * Created by cronic90 on 07/10/13.
 *
 * Custom Annotation, needed for each instance of IWarpService.
 * The Annotation has two fields: the service name and the service type.
 * Warp services can be local, push or pull. This grants full flexibility when
 * a service is invoked.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WarpServiceInfo {
    public enum Type { LOCAL, PUSH, PULL}
    public enum ServiceExecution { DEFAULT, SEQUENTIAL, CONCURRENT}
    public enum Target { ANDROID, JAVA, ALL}
    public enum ServiceCompletion { EXPLICIT, IMPLICIT}
    Type type() default Type.LOCAL;
    ServiceExecution execution() default ServiceExecution.DEFAULT;
    Target target() default Target.ALL;
    ServiceCompletion completion() default ServiceCompletion.IMPLICIT;
    String name();
    String label();
    String [] dependencies() default "";
}
