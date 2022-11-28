/*
Copyright 2003-2012 Dmitry Barashev, GanttProject Team

This file is part of GanttProject, an opensource project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ganttproject.chart.pert;

import java.util.*;

import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;

import biz.ganttproject.core.time.TimeDurationImpl;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySlice;

public class PertChartAbstraction {

  private final TaskManager myTaskManager;

  private final List<TaskGraphNode> myTaskGraph;

  public PertChartAbstraction(TaskManager taskManager) {
    myTaskManager = taskManager;
    myTaskGraph = new ArrayList<TaskGraphNode>();
    load();
  }

  /**
   * Loads data from task manager into pert chart abstraction. It creates all
   * TaskGraphNodes.
   */
  private void load() {
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
    createDummyFinishNode();
    calculateLateDates();
  }


  /**
   * @param task
   *          The task from which we want the <code>TaskGraphNode</code>
   * @return The <code>TaskGraphNode</code> corresponding of the given
   *         <code>task</code>.
   */
  private TaskGraphNode getTaskGraphNode(Task task) {
    TaskGraphNode res = getTaskGraphNodeByID(task.getTaskID());
    if (res == null) {
      res = new TaskGraphNode(task);
      if (task.isMilestone()) {
        res.setType(Type.MILESTONE);
      } else if (myTaskManager.getTaskHierarchy().getNestedTasks(task).length == 0) {
        res.setType(Type.NORMAL);
      } else {
        res.setType(Type.SUPER);
      }
      myTaskGraph.add(res);
    }
    return res;
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

  public void calculateLateDates(){
        Iterator<TaskGraphNode> it = myTaskGraph.iterator();
        List<TaskGraphNode> queue = new ArrayList<>();
        do {
            queue.clear();
            while (it.hasNext()) {
                TaskGraphNode aux = it.next();
                aux.calculateLateDates();
                GregorianCalendar LFT = aux.getLFT();
                if (LFT == null) {
                    queue.add(aux);
                }
            }
            List<TaskGraphNode> copy = new ArrayList<>();
            copy.addAll(queue);
            it = copy.iterator();
        } while(!queue.isEmpty());
  }

  protected TaskGraphNode createDummyFinishNode(){

        Task task = myTaskManager.getRootTask().unpluggedClone();
        GanttCalendar calendar = CalendarFactory.createGanttCalendar(myTaskManager.getProjectEnd());
        task.setStart(calendar);
        task.setEnd(calendar);
        task.setName("Finished Messi");
        //task.shift(new TimeDurationImpl(GPTimeUnitStack.DAY, -1));
        TaskGraphNode dummy = new TaskGraphNode(task);

        Iterator<TaskGraphNode> it = myTaskGraph.iterator();
        while(it.hasNext()){
            TaskGraphNode node = it.next();
            if(node.getSuccessors().isEmpty())
                node.addSuccessor(dummy);
        }

        dummy.calculateLateDates();
        return dummy;
    }


  /**
   * PERT graph node abstraction
   *
   * @author bbaranne
   */
   static class TaskGraphNode {

    private List<TaskGraphNode> successors;

    private int type;

    private Task myTask;

    private TimeDuration slack;

    private GregorianCalendar LFT;

    private GregorianCalendar LST;

    TaskGraphNode(Task task) {
      successors = new ArrayList<TaskGraphNode>();
      myTask = task;
    }

    void setType(int type) {
      this.type = type;
    }

    int getType() {
      return this.type;
    }

    void addSuccessor(TaskGraphNode successor) {
      this.successors.add(successor);
    }

    List<TaskGraphNode> getSuccessors() {
      return this.successors;
    }

    String getName() {
      return myTask.getName();
    }

    TimeDuration getDuration() { return myTask.getDuration(); }

    int getID() {
      return myTask.getTaskID();
    }

    boolean isCritical() {
        return slack.getLength() == 0;
        //return myTask.isCritical();
    }

    @Override
    public String toString() {
      return "{" + getName() + ", " + getDuration() + /* ", " + successors + */"}";
    }

    GregorianCalendar getEndDate() {
      return myTask.getDisplayEnd();
    }

    GregorianCalendar getStartDate() {
      return myTask.getStart();
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

    /*
    private void calculateSlack(){ // Isto nao tem em conta os feriados/fins de semana
       float slackie =  getLFT().getTime().getTime() - getEndDate().getTime().getTime();

       long slackInDays = (long) (slackie/(1000 * 60 * 60 * 24));

       this.slack = new TimeDurationImpl(GPTimeUnitStack.DAY, slackInDays);
    }
     */

    TimeDuration getSlack(){
        return slack;
    }

    private void calculateLateFinish(){
      if (successors.size() == 0){
        LFT = getEndDate();
        return;
      }

      Iterator<TaskGraphNode> it = successors.iterator();
      TaskGraphNode chosen = it.next();
      if (chosen.getLST() == null)
          return;

      while(it.hasNext()) {
          TaskGraphNode aux = it.next();
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

    GregorianCalendar getLFT(){
        return LFT;
    }

    private void calculateLateStart(){
      Task auxTask = myTask.unpluggedClone();
      auxTask.shift(getSlack());

      LST = auxTask.getStart();
    }

    GregorianCalendar getLST(){
        return LST;
    }

    void calculateLateDates(){
        calculateLateFinish();
        if (LFT != null){
            calculateSlack();
            calculateLateStart();
        }
    }

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
