package org.ganttproject.chart.pert;

import biz.ganttproject.core.time.TimeDuration;
import net.sourceforge.ganttproject.task.Task;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class PertTaskGraphNode implements TaskGraphNode {
    private List<TaskGraphNode> successors;

    private int type;

    private Task myTask;

    PertTaskGraphNode(Task task) {
        successors = new ArrayList<TaskGraphNode>();
        myTask = task;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public void addSuccessor(TaskGraphNode successor) {
        this.successors.add(successor);
    }

    public List<TaskGraphNode> getSuccessors() {
        return this.successors;
    }

    public String getName() {
        return myTask.getName();
    }

    public TimeDuration getDuration() {
        return myTask.getDuration();
    }

    public int getID() {
        return myTask.getTaskID();
    }

    public boolean isCritical() {
        return myTask.isCritical();
    }

    @Override
    public String toString() {
        return "{" + getName() + ", " + getDuration() + /* ", " + successors + */"}";
    }

    public GregorianCalendar getEndDate() {
        return myTask.getDisplayEnd();
    }

    public GregorianCalendar getStartDate() {
        return myTask.getStart();
    }
}
