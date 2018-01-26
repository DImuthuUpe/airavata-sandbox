package org.apache.airavata.helix.task.command;

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
@TaskDef(name = "CommandTask")
public class CommandTask extends AbstractTask {

    @TaskParam(name = "Command")
    private String command;

    @TaskParam(name = "Working Directory")
    private String workingDirectory;

    @TaskParam(name = "Compute Resource")
    private String computeResource;

    @TaskOutPort(name = "Success Port")
    private OutPort successOutPort;

    @TaskOutPort(name = "Fail Port")
    private OutPort failOutPort;

    public TaskResult onRun(TaskHelper helper) {
        System.out.println("Running command " + command + " on compute resource " + computeResource);
        try {
            helper.getAdaptorSupport().executeCommand(command, workingDirectory, computeResource);
            return successOutPort.invoke(new TaskResult(TaskResult.Status.COMPLETED, "Task completed"));
        } catch (Exception e) {
            publishErrors(e);
            return failOutPort.invoke(new TaskResult(TaskResult.Status.FAILED, "Task failed"));
        }
    }

    public void onCancel() {

    }

    public String getCommand() {
        return command;
    }

    public CommandTask setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public CommandTask setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public String getComputeResource() {
        return computeResource;
    }

    public CommandTask setComputeResource(String computeResource) {
        this.computeResource = computeResource;
        return this;
    }

    public OutPort getSuccessOutPort() {
        return successOutPort;
    }

    public CommandTask setSuccessOutPort(OutPort successOutPort) {
        this.successOutPort = successOutPort;
        return this;
    }

    public OutPort getFailOutPort() {
        return failOutPort;
    }

    public CommandTask setFailOutPort(OutPort failOutPort) {
        this.failOutPort = failOutPort;
        return this;
    }

    public static void main(String args[]) {
        try {
            HelixParticipant<CommandTask> participant = new HelixParticipant<>("application.properties",
                    CommandTask.class, CommandTask.class.getAnnotation(TaskDef.class).name());
            new Thread(participant).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
