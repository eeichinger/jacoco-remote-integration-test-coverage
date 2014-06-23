package jacoco;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Erich Eichinger
 * @since 06/06/2014
 */
@Controller
@RequestMapping("/jacoco")
public class JacocoControllerHttpProxy {

    private final JacocoController jacocoController = new JacocoController();
    
    @RequestMapping("/dump")
    @ResponseBody
    public byte[] getExecutionData(@RequestParam("sessionid") String sessionId, @RequestParam("reset") boolean reset) {
        return jacocoController.getExecutionData(sessionId, reset);
    }
}
