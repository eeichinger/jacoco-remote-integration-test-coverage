package jacoco;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

/**
 * @author Erich Eichinger
 * @since 06/06/2014
 */
public abstract class AbstractJacocoControllerHttpProxy {

    private final JacocoController jacocoController = new JacocoController();

    public byte[] getExecutionData(String sessionId, boolean reset) {
        return jacocoController.getExecutionData(sessionId, reset);
    }
}
