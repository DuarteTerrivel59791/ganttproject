package org.ganttproject.chart.pert;

import biz.ganttproject.core.time.TimeDuration;

import java.util.GregorianCalendar;
import java.util.List;

public interface TaskGraphNode {

    void setType(int type);

    int getType();

    void addSuccessor(TaskGraphNode successor);

    List<TaskGraphNode> getSuccessors();

    String getName();

    TimeDuration getDuration();

    int getID();

    boolean isCritical();

    public String toString();

    GregorianCalendar getEndDate();

    GregorianCalendar getStartDate();

}
