package jacoco;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * By simply linking this JAR into your web.xml-less Servlet 3.0 application, a JacocoAgentProxyServletFilter will be
 * installed to listen on /jacoco/* requests.
 * 
 * @author Erich Eichinger
 * @since 23/06/2014
 */
public class JacocoServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        FilterRegistration.Dynamic reg = ctx.addFilter("jacocoFilter", new JacocoAgentProxyServletFilter());
        reg.addMappingForUrlPatterns(null, false, "/jacoco/*");
    }
}
