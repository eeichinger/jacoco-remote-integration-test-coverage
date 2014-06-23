package jacoco;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This is the ServletFilter-equivalent to {@link org.sonar.java.jacoco.JUnitListener}
 * 
 * @author Erich Eichinger
 * @since 23/06/2014
 */
public class JacocoAgentProxyServletFilter implements Filter {
    
    private final JacocoController jacocoController = new JacocoController();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // ignore
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest)request, (HttpServletResponse)response, chain);
    }
    
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String strSessionId = request.getParameter("sessionid");
        if (strSessionId == null) strSessionId = "";
        String strReset = request.getParameter("reset");
        
        boolean reset = "true".equalsIgnoreCase(strReset) 
                || "1".equals(strReset);
        
        byte[] data = jacocoController.getExecutionData(strSessionId, reset);
        response.setContentType("application/octet-stream");
        response.getOutputStream().write(data);
        response.flushBuffer();
    }

    @Override
    public void destroy() {
        // ignore
    }
}
