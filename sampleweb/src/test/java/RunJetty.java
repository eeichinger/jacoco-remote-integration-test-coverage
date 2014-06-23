import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.JarResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.SpringServletContainerInitializer;
import org.springframework.web.WebApplicationInitializer;
import uk.co.postoffice.spike.esi.helloworld.HelloWorldWebInitializer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * @author Erich Eichinger
 * @since 26/08/2013
 */
public class RunJetty {

    public static void main(String[] args) throws Exception {
        String currentDir = System.getProperty("user.dir");
        final Properties properties = System.getProperties();
        System.out.println("Current Dir: " + properties.getProperty("user.dir"));

        Server server = new Server();
        ServerConnector scc = new ServerConnector(server);
        scc.setPort(Integer.parseInt(System.getProperty("jetty.port", "8080")));
        server.setConnectors(new Connector[] { scc });

        server.setAttribute("org.eclipse.jetty.webapp.configuration" ,"");

        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        context.setResourceBase("src/main/webapp");
        context.setWar("src/main/webapp");
//        context.getMetaData().setWebInfClassesDirs(Arrays.asList(context.newResource("target/classes")));
/*
        URLClassLoader classLoader = (URLClassLoader)WebApplicationInitializer.class.getClassLoader();
        final URL[] urLs = classLoader.getURLs();

        ArrayList<Resource> dirResources = new ArrayList<>();
        MetaData metaData = context.getMetaData();
        for(URL url:urLs) {
            final Resource classpathResource = context.newResource(url);
            if (!classpathResource.isDirectory()) {
                metaData.addContainerResource(classpathResource);
            } else {
                dirResources.add(classpathResource);
            }
        }
        context.getMetaData().setWebInfClassesDirs(dirResources);
*/

        context.setConfigurations(new Configuration[]{
                new AnnotationConfiguration() {
                    @Override
                    public void preConfigure(WebAppContext context) throws Exception {
                        ClassInheritanceMap map = new ClassInheritanceMap();
                        map.put(WebApplicationInitializer.class.getName(), new ConcurrentHashSet<String>() {{
                           add(HelloWorldWebInitializer.class.getName());
                        }});
                        context.setAttribute(CLASS_INHERITANCE_MAP, map);
                    }
                },
                new WebXmlConfiguration(),
                new WebInfConfiguration(),
                // new TagLibConfiguration(),
                new PlusConfiguration(),
                new MetaInfConfiguration(),
                new FragmentConfiguration(),
                new EnvConfiguration()
        });

        server.setHandler(context);

        try {
            System.out.println(">>> STARTING EMBEDDED JETTY SERVER, PRESS ANY KEY TO STOP");
            System.out.println(String.format(">>> open http://localhost:%s/", scc.getPort()));
            server.start();
            while (System.in.available() == 0) {
                Thread.sleep(5000);
            }
            server.stop();
            server.join();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(100);
        }

    }
}
