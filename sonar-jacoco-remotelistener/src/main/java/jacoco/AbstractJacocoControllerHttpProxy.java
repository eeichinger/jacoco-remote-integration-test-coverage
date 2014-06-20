package jacoco;

import org.jacoco.agent.rt.IAgent;
import org.jacoco.agent.rt.RT;

/**
 * @author Erich Eichinger
 * @since 06/06/2014
 */
public abstract class AbstractJacocoControllerHttpProxy {

//    IAgent agent;

//    public JacocoControllerHttpProxy() {
//        agent = getAgent();
//    }

    private IAgent getAgent() {
        try {
            return RT.getAgent();
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public byte[] getExecutionData(String sessionId, boolean reset) {
        IAgent agent = getAgent();
        if (agent != null) {
            System.out.println("JacocoAgent setting sessionid '" + sessionId + "'");
            agent.setSessionId(sessionId);
            return agent.getExecutionData(true);
        }
        return new byte[0];
    }
}
