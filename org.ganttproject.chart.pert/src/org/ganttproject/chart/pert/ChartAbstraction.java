package org.ganttproject.chart.pert;

import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;

import java.util.ArrayList;
import java.util.List;

public abstract class ChartAbstraction {
    protected final TaskManager myTaskManager;

    protected final List<TaskGraphNode> myTaskGraph;

    ChartAbstraction(TaskManager taskManager){
        this.myTaskManager = taskManager;
        myTaskGraph = new ArrayList<>();
    }

    protected static String i18n(String key) {
        return GanttLanguage.getInstance().getText(key);
    }

    public List<TaskGraphNode> getAncestor(TaskGraphNode tgn) {
        List<TaskGraphNode> ancestors = new ArrayList<>();
        for (TaskGraphNode tnode : myTaskGraph) {
            List<TaskGraphNode> successor = tnode.getSuccessors();
            if (successor.contains(tgn)) {
                ancestors.add(tnode);
            }
        }
        return ancestors;
    }


    /** @return The list of <code>TaskGraphNodes</code>. */
    public List<TaskGraphNode> getTaskGraphNodes() {
        return myTaskGraph;
    }

    /**
     * @param id
     *          The task ID from which we want the <code>TaskGraphNode</code>
     * @return The <code>TaskGraphNode</code> corresponding to the given task ID.
     */
    public TaskGraphNode getTaskGraphNodeByID(int id) {
        for (TaskGraphNode tgn : myTaskGraph) {
            if (tgn.getID() == id) {
                return tgn;
            }
        }
        return null;
    }

    /**
     * Type of the node: NORMAL, SUPER (for super tasks) and MILESTONE.
     *
     * @author bbaranne
     *
     */
    static class Type {
        public static final int NORMAL = 0;

        public static final int SUPER = 1;

        public static final int MILESTONE = 2;
    }


}
