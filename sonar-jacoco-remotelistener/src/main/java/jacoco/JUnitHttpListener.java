package jacoco;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * A JUnit {@link RunListener} implementation to capture coverage info from a remote application, exposed by 
 * either {@link JacocoAgentProxyController} or {@link JacocoAgentProxyServletFilter}.
 * The listener expects 2 system properties to be set:
 * "targeturl":  
 * "destfile": set to the path of Jacoco's execution coverage output file.
 *
 *  
 * @author Erich Eichinger
 * @since 06/06/2014
 */
public class JUnitHttpListener extends RunListener {

    @Override
    public void testStarted(Description description) throws Exception {
        onTestStart(getName(description));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        onTestFinish(getName(description));
    }
    
    public void onTestStart(String name) throws Exception {
        System.out.println("JacocoController: onTestStart(" + name + ")");
        dumpFromRemote("");
    }
    
    public void onTestFinish(String name) throws Exception {
        System.out.println("JacocoController: onTestFinish(" + name + ")");
        dumpFromRemote(name);
    }

    private void dumpFromRemote(String name) throws Exception {
//        URI targeturl = new URI(System.getProperty("targeturl", "http://localhost:8080/jacoco"));
        URI targeturl = new URI(System.getProperty("targeturl"));
        String destfile = System.getProperty("destfile", "./target/jacoco-it.exec");
        byte[] data = fetchBytes(targeturl.resolve("dump?sessionid=" + URLEncoder.encode(name, "utf-8") + "&reset=true"));
        final File file = new File(destfile); // "../target/jacoco-it.exec"
        System.out.println("JacocoController: dump(" + name + ") to " + file.getAbsolutePath());
        save(file, true, data);
    }

    private static String getName(Description description) {
        return description.getClassName() + " " + description.getMethodName();
    }

    private byte[] fetchBytes(URI url) throws IOException {
        return IOUtils.readFully((InputStream) url.toURL().getContent(), -1, true);
    }

    public void save(final File file, final boolean append, byte[] data) throws IOException {
        final File folder = file.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }
        final FileOutputStream fileStream = new FileOutputStream(file, append);
        // Avoid concurrent writes from other processes:
        fileStream.getChannel().lock();
        try (OutputStream bufferedStream = new BufferedOutputStream(fileStream)) {
            bufferedStream.write(data);
            bufferedStream.flush();
        }
    }

}
