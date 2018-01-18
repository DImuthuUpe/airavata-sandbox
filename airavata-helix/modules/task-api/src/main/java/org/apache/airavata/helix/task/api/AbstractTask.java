package org.apache.airavata.helix.task.api;

import org.apache.airavata.helix.task.api.annotation.TaskParam;
import org.apache.helix.HelixManager;
import org.apache.helix.task.Task;
import org.apache.helix.task.TaskCallbackContext;
import org.apache.helix.task.TaskResult;
import org.apache.helix.task.UserContentStore;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public abstract class AbstractTask extends UserContentStore implements Task {

    private static final String NEXT_JOB = "next-job";
    private static final String WORKFLOW_STARTED = "workflow-started";

    @TaskParam(name = "taskId")
    private String taskId;

    @TaskParam(name = "workflowId")
    private String workflowId;

    private TaskCallbackContext callbackContext;

    @Override
    public void init(HelixManager manager, String workflowName, String jobName, String taskName) {
        super.init(manager, workflowName, jobName, taskName);
        try {
            TaskUtil.deserializeTaskData(this, this.callbackContext.getTaskConfig().getConfigMap());
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public final TaskResult run() {
        boolean isThisNextJob = getUserContent(WORKFLOW_STARTED, Scope.WORKFLOW) == null ||
                this.callbackContext.getJobConfig().getJobId()
                        .equals(this.callbackContext.getJobConfig().getWorkflow() + "_" + getUserContent(NEXT_JOB, Scope.WORKFLOW));
        if (isThisNextJob) {
            return onRun();
        } else {
            return new TaskResult(TaskResult.Status.COMPLETED, "Not a target job");
        }
    }

    @Override
    public final void cancel() {
        onCancel();
    }

    public abstract TaskResult onRun();

    public abstract void onCancel();

    public String getTaskId() {
        return taskId;
    }

    public AbstractTask setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public AbstractTask setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
        return this;
    }

    public TaskCallbackContext getCallbackContext() {
        return callbackContext;
    }

    public AbstractTask setCallbackContext(TaskCallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        return this;
    }
}
