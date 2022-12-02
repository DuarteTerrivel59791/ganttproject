package org.ganttproject.chart.pert;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.TimeDuration;
import biz.ganttproject.core.time.TimeDurationImpl;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.task.Task;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

public class AONTaskGraphNode implements TaskGraphNode {
    private List<TaskGraphNode> successors;

    private int type;

    private Task myTask;

    private TimeDuration slack;

    private GregorianCalendar LST;

    private GregorianCalendar LFT;

    public AONTaskGraphNode(Task task) {
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

    public TimeDuration getDuration() { return myTask.getDuration(); }

    public int getID() {
        return myTask.getTaskID();
    }

    public boolean isCritical() {
        return slack.getLength() == 0;
    }

    @Override
    public String toString() {
        return "{" + getName() + ", " + getDuration() + "}";
    }

    public GregorianCalendar getEndDate() {
        return myTask.getDisplayEnd();
    }

    public GregorianCalendar getStartDate() {
        return myTask.getStart();
    }

    public TimeDuration getSlack() {
        return slack;
    }

    public GregorianCalendar getLST() {
        return LST;
    }

    public GregorianCalendar getLFT() {
        return LFT;
    }

    public void calculateLateDates() {
        calculateLateFinish();
        if (LFT != null){
            calculateSlack();
            calculateLateStart();
        }
    }

    private void calculateLateFinish() {
        if (successors.size() == 0){
            LFT = getEndDate();
            return;
        }

        Iterator<TaskGraphNode> it = successors.iterator();
        AONTaskGraphNode chosen = (AONTaskGraphNode) it.next();
        if (chosen.getLST() == null)
            return;

        while(it.hasNext()) {
            AONTaskGraphNode aux = (AONTaskGraphNode) it.next();
            if (aux.getLST() == null)
                return;
            if(aux.getLST().before(chosen.getLST()))
                chosen = aux;
        }

        Task t = chosen.myTask.unpluggedClone();
        t.setStart(CalendarFactory.createGanttCalendar(chosen.getLST().getTime()));
        t.setEnd(CalendarFactory.createGanttCalendar(chosen.getLFT().getTime()));
        t.shift(new TimeDurationImpl(GPTimeUnitStack.DAY, -1));

        LFT = t.getStart();
    }

    private void calculateLateStart(){
        Task auxTask = myTask.unpluggedClone();
        auxTask.shift(getSlack());

        LST = auxTask.getStart();
    }

    private void calculateSlack() {
        int days = 0;
        Task aux = myTask.unpluggedClone();

        while(!aux.getDisplayEnd().equals(LFT)) {
            aux.shift(new TimeDurationImpl(GPTimeUnitStack.DAY, 1));
            days++;
        }

        slack = new TimeDurationImpl(GPTimeUnitStack.DAY, days);

    }

}
