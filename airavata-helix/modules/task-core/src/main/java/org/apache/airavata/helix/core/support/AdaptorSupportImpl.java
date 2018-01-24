package org.apache.airavata.helix.core.support;

import org.apache.airavata.agents.api.AgentAdaptor;
import org.apache.airavata.agents.api.AgentException;
import org.apache.airavata.agents.api.AgentStore;
import org.apache.airavata.helix.task.api.support.AdaptorSupport;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class AdaptorSupportImpl implements AdaptorSupport {

    private static AdaptorSupportImpl INSTANCE;

    private final AgentStore agentStore = new AgentStore();

    private AdaptorSupportImpl() {}

    public synchronized static AdaptorSupportImpl getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AdaptorSupportImpl();
        }
        return INSTANCE;
    }

    public void initializeAdaptor() {
    }

    public void executeCommand(String command, String workingDirectory, String computeResourceId) throws AgentException {
        getAgentAdaptorFroComputeResource(computeResourceId).executeCommand(command, workingDirectory);
    }

    public void createDirectory(String path, String computeResourceId) throws AgentException {
        getAgentAdaptorFroComputeResource(computeResourceId).createDirectory(path);
    }

    public void copyFile(String sourceFile, String destinationFile, String computeResourceId) throws AgentException {
        getAgentAdaptorFroComputeResource(computeResourceId).copyFile(sourceFile, destinationFile);
    }

    private AgentAdaptor getAgentAdaptorFroComputeResource(String computeResource) throws AgentException {
         return agentStore.fetchAdaptor(computeResource);
    }
}
