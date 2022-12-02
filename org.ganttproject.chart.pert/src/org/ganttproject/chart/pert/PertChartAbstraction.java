package org.ganttproject.chart.pert;

import javafx.scene.chart.Chart;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySlice;

public class PertChartAbstraction extends ChartAbstraction {

    public PertChartAbstraction(TaskManager taskManager) {
        super(taskManager);
    }

    /**
     * Loads data from task manager into pert chart abstraction. It creates all
     * TaskGraphNodes.
     */
    protected void load() {
        Task[] tasks = myTaskManager.getTasks();
        for (int i = 0; i < tasks.length; i++) {
            Task task = tasks[i];
            TaskGraphNode tgn = getTaskGraphNode(task);
            TaskDependencySlice dependencies = task.getDependenciesAsDependee();
            TaskDependency[] relationship = dependencies.toArray();
            for (int j = 0; j < relationship.length; j++) {
                Task successor = relationship[j].getDependant();
                tgn.addSuccessor(getTaskGraphNode(successor));
            }
        }
    }

    /**
     * @param task
     *          The task from which we want the <code>TaskGraphNode</code>
     * @return The <code>TaskGraphNode</code> corresponding of the given
     *         <code>task</code>.
     */
    protected TaskGraphNode getTaskGraphNode(Task task) {
        PertTaskGraphNode res = (PertTaskGraphNode) getTaskGraphNodeByID(task.getTaskID());
        if (res == null) {
            res = new PertTaskGraphNode(task);
            if (task.isMilestone()) {
                res.setType(ChartAbstraction.Type.MILESTONE);
            } else if (myTaskManager.getTaskHierarchy().getNestedTasks(task).length == 0) {
                res.setType(ChartAbstraction.Type.NORMAL);
            } else {
                res.setType(ChartAbstraction.Type.SUPER);
            }
            myTaskGraph.add(res);
        }
        return res;
    }
}
