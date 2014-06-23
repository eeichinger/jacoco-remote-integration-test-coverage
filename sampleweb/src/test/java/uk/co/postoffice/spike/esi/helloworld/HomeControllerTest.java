package uk.co.postoffice.spike.esi.helloworld;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
* @author Erich Eichinger
* @since 04/06/2014
*/
public class HomeControllerTest {

    @Test
    public void home_should_return_homeview() throws Exception {
        assertThat( new HomeController().home(), equalTo("home") );
    }
}
