package org.ganttproject.chart.pert;

import biz.ganttproject.core.time.TimeDuration;

import java.util.GregorianCalendar;
import java.util.List;

public interface TaskGraphNode {
    public void setType(int type);

    public int getType();

    public void addSuccessor(TaskGraphNode successor);

    public List<TaskGraphNode> getSuccessors();

    public String getName();

    public TimeDuration getDuration();

    public int getID();

    public boolean isCritical();

    public String toString();

    public GregorianCalendar getEndDate();

    public GregorianCalendar getStartDate();
}
