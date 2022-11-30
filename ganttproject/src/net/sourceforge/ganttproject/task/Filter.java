package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.time.GanttCalendar;

public interface Filter {
    public void setMinLength(int min);

    public void setMaxLength(int max);

    public void setMinPriority(Task.Priority p);

    public void setMaxPriority(Task.Priority p);

    public void setMinCompletion(int min);

    public void setMaxCompletion(int max);

    public void setMinStartDate(GanttCalendar minStartDate);

    public void setMaxStartDate(GanttCalendar maxStartDate);

    public void setMinEndDate(GanttCalendar minEndDate);

    public void setMaxEndDate(GanttCalendar maxEndDate);

    public boolean hasMinLength();

    public boolean hasMaxLength();

    public boolean hasMinPriority();

    public boolean hasMaxPriority();

    public boolean hasMinCompletion();

    public boolean hasMaxCompletion();

    public boolean hasMinStartDate();

    public boolean hasMaxStartDate();

    public boolean hasMinEndDate();

    public boolean hasMaxEndDate();

    public boolean taskWithinParameters(Task t);
}
