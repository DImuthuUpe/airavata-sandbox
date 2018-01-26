package org.apache.airavata.helix.task.scp;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
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
@TaskDef(name = "SCPTask")
public class ScpTask extends AbstractTask {

    @TaskParam(name = "Source File")
    private String sourceFile;

    @TaskParam(name = "Destination File")
    private String destinationFile;

    @TaskParam(name = "Compute Resource")
    private String computeResource;

    @TaskParam(name = "Success Port")
    private OutPort successOutPort;

    @TaskParam(name = "Fail port")
    private OutPort failOutPort;

    public TaskResult onRun(TaskHelper helper) {
        try {
            System.out.println("copying file from " + sourceFile + " to " + destinationFile);
            helper.getAdaptorSupport().copyFile(sourceFile, destinationFile, computeResource);
            return successOutPort.invoke(new TaskResult(TaskResult.Status.COMPLETED, "Successfully copied"));
        } catch (Exception e) {
            publishErrors(e);
            return failOutPort.invoke(new TaskResult(TaskResult.Status.FAILED, "Failed to copy"));
        }
    }

    public void onCancel() {

    }

    public static void main(String args[]) {
        try {
            HelixParticipant<ScpTask> participant = new HelixParticipant<>("application.properties",
                    ScpTask.class, ScpTask.class.getAnnotation(TaskDef.class).name());
            new Thread(participant).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
