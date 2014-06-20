package uk.co.postoffice.spike.esi.helloworld;

import org.junit.Before;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.*;
import java.net.URL;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Erich Eichinger
 * @since 05/06/2014
 */
public class HomeControllerAT {

    @Test
    public void home_should_render_helloFromHOME() throws Exception {
        final String strUrl = "http://localhost:8080/home";
        final byte[] bytes = fetchBytes(strUrl);

        String content = new String(bytes);

        assertThat(content, containsString("Hello from HOME"));
    }

    private byte[] fetchBytes(String strUrl) throws IOException {
        URL url = new URL(strUrl);
        return IOUtils.readFully((InputStream) url.getContent(), -1, true);
    }

}
