package uk.co.postoffice.spike.esi.helloworld;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Erich Eichinger
 * @since 26/08/2013
 */
@Controller
public class HomeController {

    @RequestMapping("/home")
    public String home() {
        return "home";
    }

    @RequestMapping("/edit")
    public String edit() {
        return "edit";
    }
}
