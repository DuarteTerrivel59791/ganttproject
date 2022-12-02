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

import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.dependency.TaskDependency;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySlice;

public class AONChartAbstraction extends ChartAbstraction{


  public AONChartAbstraction(TaskManager taskManager) {
      super(taskManager);
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
    protected TaskGraphNode getTaskGraphNode(Task task) {
        TaskGraphNode res = getTaskGraphNodeByID(task.getTaskID());
        if (res == null) {
            res = new AONTaskGraphNode(task);
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

  public void calculateLateDates(){
        Iterator<TaskGraphNode> it = myTaskGraph.iterator();
        List<AONTaskGraphNode> queue = new ArrayList<>();
        do {
            queue.clear();
            while (it.hasNext()) {
                AONTaskGraphNode aux = (AONTaskGraphNode) it.next();
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
        task.setName(i18n("end"));
        AONTaskGraphNode dummy = new AONTaskGraphNode(task);

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
