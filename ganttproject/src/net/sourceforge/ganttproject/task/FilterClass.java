package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.time.GanttCalendar;

public class FilterClass implements Filter {

    private int minLength;

    private int maxLength;

    private Task.Priority minPriority;

    private Task.Priority maxPriority;

    private int minCompletion;

    private int maxCompletion;

    private GanttCalendar minStartDate;

    private GanttCalendar maxStartDate;

    private GanttCalendar minEndDate;

    private GanttCalendar maxEndDate;

    public FilterClass() {
        this.minLength = -1;
        this.maxLength = -1;
        this.minPriority = null;
        this.maxPriority = null;
        this.minCompletion = -1;
        this.maxCompletion = -1;
        this.minStartDate = null;
        this.maxStartDate = null;
        this.minEndDate = null;
        this.maxEndDate = null;
    }

    public void setMinLength(int min) {
        this.minLength = min;
    }

    public void setMaxLength(int max) {
        this.maxLength = max;
    }

    public void setMinPriority(Task.Priority p) {
        this.minPriority = p;
    }

    public void setMaxPriority(Task.Priority p) {
        this.maxPriority = p;
    }

    public void setMinCompletion(int min) {
        this.minCompletion = min;
    }

    public void setMaxCompletion(int max) {
        this.maxCompletion = max;
    }

    public void setMinStartDate(GanttCalendar minStartDate) {
        this.minStartDate = minStartDate;
    }

    public void setMaxStartDate(GanttCalendar maxStartDate) {
        this.maxStartDate = maxStartDate;
    }

    public void setMinEndDate(GanttCalendar minEndDate) {
        this.minEndDate = minEndDate;
    }

    public void setMaxEndDate(GanttCalendar maxEndDate) {
        this.maxEndDate = maxEndDate;
    }

    public boolean hasMinLength() {
        return minLength >= 0;
    }

    public boolean hasMaxLength() {
        return maxLength >= 0;
    }

    public boolean hasMinPriority() {
        return minPriority != null;
    }

    public boolean hasMaxPriority() {
        return maxPriority != null;
    }

    public boolean hasMinCompletion() {
        return minCompletion >= 0;
    }

    public boolean hasMaxCompletion() {
        return maxCompletion >= 0;
    }

    public boolean hasMinStartDate() {
        return minStartDate != null;
    }

    public boolean hasMaxStartDate() {
        return maxStartDate != null;
    }

    public boolean hasMinEndDate() {
        return minEndDate != null;
    }

    public boolean hasMaxEndDate() {
        return maxEndDate != null;
    }

    public boolean taskWithinParameters(Task t) {
        return !( (hasMinLength() && t.getDuration().getLength() < minLength)
                || (hasMaxLength() && t.getDuration().getLength() > maxLength)
                || (hasMinPriority() && t.getPriority().ordinal() < minPriority.ordinal())
                || (hasMaxPriority() && t.getPriority().ordinal() > maxPriority.ordinal())
                || (hasMinCompletion() && t.getCompletionPercentage() < minCompletion)
                || (hasMaxCompletion() && t.getCompletionPercentage() > maxCompletion)
                || (hasMinStartDate()) && t.getStart().before(minStartDate)
                || (hasMaxStartDate()) && t.getStart().after(maxStartDate)
                || (hasMinEndDate()) && t.getDisplayEnd().before(minEndDate)
                || (hasMaxEndDate()) && t.getDisplayEnd().after(maxEndDate));

    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public Task.Priority getMinPriority() {
        return minPriority;
    }

    public Task.Priority getMaxPriority() {
        return maxPriority;
    }

    public int getMinCompletion() {
        return minCompletion;
    }

    public int getMaxCompletion() {
        return maxCompletion;
    }

    public GanttCalendar getMinStartDate() {
        return minStartDate;
    }

    public GanttCalendar getMaxStartDate() {
        return maxStartDate;
    }

    public GanttCalendar getMinEndDate() {
        return minEndDate;
    }

    public GanttCalendar getMaxEndDate() {
        return maxEndDate;
    }
}