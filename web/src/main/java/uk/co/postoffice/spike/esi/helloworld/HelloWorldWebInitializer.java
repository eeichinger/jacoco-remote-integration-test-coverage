package uk.co.postoffice.spike.esi.helloworld;

import jacoco.JacocoAgentProxyServletFilter;
import org.springframework.core.Conventions;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.*;
import java.util.EnumSet;

/**
 * @author Erich Eichinger
 * @since 26/08/2013
 */
public class HelloWorldWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[] { };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { HelloWorldConfiguration.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        super.onStartup(servletContext);

        registerJacocoFilter(servletContext);
        
    }

    private void registerJacocoFilter(ServletContext servletContext) {
        Filter filter = new JacocoAgentProxyServletFilter();
        String filterName = Conventions.getVariableName(filter);
        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filter);
        registration.setAsyncSupported(isAsyncSupported());
        registration.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE),
                false,
                "/jacoco/*");
    }
}
