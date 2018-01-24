package org.apache.airavata.helix.core;

import org.apache.helix.HelixManager;
import org.apache.helix.HelixManagerFactory;
import org.apache.helix.InstanceType;
import org.apache.helix.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class WorkflowManager {

    private static final String WORKFLOW_PREFIX = "Workflow_of_process_";
    private TaskDriver taskDriver;

    public static void main(String args[]) throws Exception {
        /*WorkflowManager workflowManager = new WorkflowManager("AiravataDemoCluster", "WorkflowManager", "localhost:2199");
        List<AbstractTask> taskDatas = new ArrayList<>();
        MkdirTask data = new MkdirTask();
        data.setComputeResourceId("Comp 1")
                .setDirName("/tmp")
                .setTaskId(UUID.randomUUID().toString())
                .setWorkflowId("workflow 1");

        taskDatas.add(data);
        workflowManager.launchWorkflow(UUID.randomUUID().toString(), taskDatas);*/
    }

    public WorkflowManager(String helixClusterName, String instanceName, String zkConnectionString) throws Exception {

        HelixManager helixManager = HelixManagerFactory.getZKHelixManager(helixClusterName, instanceName,
                InstanceType.SPECTATOR, zkConnectionString);
        helixManager.connect();

        Runtime.getRuntime().addShutdownHook(
                new Thread() {
                    @Override
                    public void run() {
                        helixManager.disconnect();
                    }
                }
        );

        taskDriver = new TaskDriver(helixManager);
    }

    private void launchWorkflow(String processId, List<AbstractTask> tasks) throws Exception {

        Workflow.Builder workflowBuilder = new Workflow.Builder(WORKFLOW_PREFIX + processId).setExpiry(0);

        for (int i = 0; i < tasks.size(); i++) {
            AbstractTask data = tasks.get(i);
            TaskConfig.Builder taskBuilder = new TaskConfig.Builder().setTaskId("Task_" + data.getTaskId())
                    .setCommand("MkdirTask");
            Map<String, String> paramMap = org.apache.airavata.helix.core.util.TaskUtil.serializeTaskData(data);
            paramMap.forEach(taskBuilder::addConfig);

            List<TaskConfig> taskBuilds = new ArrayList<>();
            taskBuilds.add(taskBuilder.build());

            JobConfig.Builder job = new JobConfig.Builder()
                    .addTaskConfigs(taskBuilds)
                    .setFailureThreshold(0)
                    .setMaxAttemptsPerTask(3);
            //.setInstanceGroupTag(data.getTaskType());

            workflowBuilder.addJob(("JOB_" + data.getTaskId()), job);
            if (i > 0) {
                workflowBuilder.addParentChildDependency("JOB_" + tasks.get(i - 1).getTaskId(), "JOB_" + data.getTaskId()); // get parent job
            }
        }

        WorkflowConfig.Builder config = new WorkflowConfig.Builder().setFailureThreshold(0);
        workflowBuilder.setWorkflowConfig(config.build());
        Workflow workflow = workflowBuilder.build();

        taskDriver.start(workflow);

        //TODO : Do we need to monitor workflow status? If so how do we do it in a scalable manner? For example,
        // if the hfac that monitors a particular workflow, got killed due to some reason, who is taking the responsibility

        TaskState taskState = taskDriver.pollForWorkflowState(workflow.getName(),
                TaskState.COMPLETED, TaskState.FAILED, TaskState.STOPPED, TaskState.ABORTED);

    }
}