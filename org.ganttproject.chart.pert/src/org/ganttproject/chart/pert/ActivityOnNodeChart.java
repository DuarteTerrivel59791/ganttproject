/*
GanttProject is an opensource project management tool.
Copyright (C) 2005-2011 Bernoit Baranne, Julien Seiler, GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.ganttproject.chart.pert;

import net.sourceforge.ganttproject.GanttExportSettings;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.export.ChartImageVisitor;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.util.StringUtils;
import org.ganttproject.chart.pert.PertChartAbstraction.TaskGraphNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ActivityOnNodeChart extends ActivityOnNodePertChart {

  private final static int NODE_WIDTH = 200;

  private final static int NODE_HEIGHT = 130;

  public ActivityOnNodeChart() {
    super();
  }

  public String getName() {
    return language.getText("aonDiagram");
  }

  int getNodeWidth() {
    return (int) (NODE_WIDTH * getDpi());
  }

  int getNodeHeight() {
    return (int) (NODE_HEIGHT * getDpi());
  }

  @Override
  protected void buildPertChart() {
    if (myPertAbstraction == null) {
      myPertAbstraction = new PertChartAbstraction(myTaskManager);
      myTaskGraphNodes = myPertAbstraction.getTaskGraphNodes();
      myGraphicalNodes = new ArrayList<>();
      myGraphicalArrows = new ArrayList<>();
      // myMapPositionListOfNodes = new HashMap();
      // rowsList = new HashMap();
      nbCols = 0;
      setBackground(Color.WHITE);
      this.process();
      // System.out.println("Position correction");
      // correctPositionBecauseOfSuperTasks();
      avoidCrossingNode();
      avoidCrossingLine();
      removeEmptyColumn();
      calculateGraphicalNodesCoordinates();
      calculateArrowsCoordinates();
      setPreferredSize(new Dimension(getMaxX(), getMaxY()));
    } else {
      myPertAbstraction = new PertChartAbstraction(myTaskManager);
      myTaskGraphNodes = myPertAbstraction.getTaskGraphNodes();
      updateGraphNodesInfo();
    }
  }

  private void process() {
    for (TaskGraphNode tgn : myTaskGraphNodes) {
      if (isZeroPosition(tgn)) {
        add(0, new AONGraphicalNode(tgn, this));
      }
    }

    // TODO Translate:
    // ici tous les 0 position sont faits.
    int col = 0;
    List<TaskGraphNode> l = getNodesThatAreInASpecificSuccessorPosition(col);
    while (l != null) {
      for (TaskGraphNode tnode : l) {
        GraphicalNode gnode = getGraphicalNodeByID(tnode.getID());
        if (gnode == null) {
          gnode = createGraphicalNode(tnode);
        } else {
          remove(gnode);
        }
        add(col + 1, gnode);
      }
      col++;
      l = getNodesThatAreInASpecificSuccessorPosition(col);
    }
  }

  private GraphicalNode createGraphicalNode(TaskGraphNode taskGraphNode) {
    GraphicalNode res = getGraphicalNodeByID(taskGraphNode.getID());
    if (res != null) {
      return res;
    }
    return new AONGraphicalNode(taskGraphNode, this);
  }

}