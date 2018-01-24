package org.apache.airavata.helix.task.mkdir;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.participant.HelixParticipant;
import org.apache.airavata.helix.task.api.TaskHelper;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
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

    @Override
    public TaskResult onRun(TaskHelper helper) {
        System.out.println("Running mkdir task");
        try {
            helper.getAdaptorSupport().createDirectory(dirName, computeResourceId);
            return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
        } catch (Exception e) {
            publishErrors(e);
            return new TaskResult(TaskResult.Status.FAILED, e.getMessage());
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

    public static void main(String args[]) {
        try {
            HelixParticipant<MkdirTask> participant = new HelixParticipant<>("application.properties", MkdirTask.class);
            new Thread(participant).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
