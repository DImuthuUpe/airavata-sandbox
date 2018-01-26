package org.apache.airavata.helix.workflow;

import org.apache.airavata.helix.core.AbstractTask;
import org.apache.airavata.helix.core.OutPort;
import org.apache.airavata.helix.task.mkdir.MkdirTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
public class SimpleWorkflow {

    public static void main(String[] args) throws Exception {
        WorkflowManager wm = new WorkflowManager("AiravataDemoCluster", "WorkflowManager", "localhost:2199");

        MkdirTask mkdirTask1 = new MkdirTask();
        mkdirTask1.setDirName("/tmp/newdir");
        mkdirTask1.setComputeResourceId("localhost");
        mkdirTask1.setTaskId("task1");

        MkdirTask mkdirTask2 = new MkdirTask();
        mkdirTask2.setDirName("/tmp/newdir2");
        mkdirTask2.setComputeResourceId("localhost");
        mkdirTask2.setTaskId("task2");

        mkdirTask1.setSuccessPort(new OutPort("task2", mkdirTask1));

        List<AbstractTask> allTasks = new ArrayList<>();
        allTasks.add(mkdirTask2);
        allTasks.add(mkdirTask1);

        wm.launchWorkflow(UUID.randomUUID().toString(), allTasks);
    }
}
