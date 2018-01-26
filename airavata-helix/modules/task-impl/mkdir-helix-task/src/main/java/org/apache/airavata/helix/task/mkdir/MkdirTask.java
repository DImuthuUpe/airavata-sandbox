package org.apache.airavata.helix.task.mkdir;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskOutPort;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.task.TaskResult;

import java.io.IOException;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@TaskDef(name = "MkdirTask")
public class MkdirTask extends AbstractTask {

    @TaskParam(name = "directoryName")
    private String dirName;

    @TaskParam(name = "computeResourceId")
    private String computeResourceId;

    @TaskOutPort(name = "Success")
    private OutPort successPort;

    @TaskOutPort(name = "Fail")
    private OutPort failOutPort;

    @Override
    public TaskResult onRun(TaskHelper helper) {
        System.out.println("Running mkdir task");
        try {
            System.out.println("Creating directory " + dirName);
            helper.getAdaptorSupport().createDirectory(dirName, computeResourceId);
            return successPort.invoke(new TaskResult(TaskResult.Status.COMPLETED, "Completed"));
        } catch (Exception e) {
            publishErrors(e);
            return failOutPort.invoke(new TaskResult(TaskResult.Status.FAILED, e.getMessage()));
        }
    }

    public void onCancel() {

    }

    public String getDirName() {
        return dirName;
    }

    public MkdirTask setDirName(String dirName) {
        this.dirName = dirName;
        return this;
    }

    public String getComputeResourceId() {
        return computeResourceId;
    }

    public MkdirTask setComputeResourceId(String computeResourceId) {
        this.computeResourceId = computeResourceId;
        return this;
    }

    public OutPort getSuccessPort() {
        return successPort;
    }

    public MkdirTask setSuccessPort(OutPort successPort) {
        this.successPort = successPort;
        return this;
    }

    public OutPort getFailOutPort() {
        return failOutPort;
    }

    public MkdirTask setFailOutPort(OutPort failOutPort) {
        this.failOutPort = failOutPort;
        return this;
    }

    public static void main(String args[]) {
        try {
            HelixParticipant<MkdirTask> participant = new HelixParticipant<>("application.properties",
                    MkdirTask.class, MkdirTask.class.getAnnotation(TaskDef.class).name());
            new Thread(participant).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
