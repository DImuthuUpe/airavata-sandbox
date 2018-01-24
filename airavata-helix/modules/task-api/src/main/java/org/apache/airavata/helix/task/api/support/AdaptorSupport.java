package org.apache.airavata.helix.task.api.support;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public interface AdaptorSupport {
    public void initializeAdaptor();

    public void executeCommand(String command, String workingDirectory, String computeResourceId) throws Exception;

    public void createDirectory(String path, String computeResourceId) throws Exception;

    public void copyFile(String sourceFile, String destinationFile, String computeResourceId) throws Exception;
}
