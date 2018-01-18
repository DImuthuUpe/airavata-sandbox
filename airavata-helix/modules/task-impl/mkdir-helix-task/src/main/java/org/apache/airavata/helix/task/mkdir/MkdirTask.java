package org.apache.airavata.helix.task.mkdir;

import org.apache.airavata.helix.task.api.AbstractTask;
import org.apache.airavata.helix.task.api.annotation.TaskDef;
import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;

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

    public TaskResult onRun() {
        System.out.println("Running mkdir task");
        return new TaskResult(TaskResult.Status.COMPLETED, "Completed");
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
}
