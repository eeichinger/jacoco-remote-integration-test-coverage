package uk.co.postoffice.spike.esi.helloworld;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

/**
 * @author Erich Eichinger
 * @since 03/06/2014
 */
public class HomeControllerIT {

    @Test
    public void edit_should_return_editview() throws Exception {
        assertThat(new HomeController().edit(), equalTo("edit"));
    }
}
