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

import net.sourceforge.ganttproject.chart.Chart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class ActivityOnNodeChart extends PertChart {

  private final static int NODE_WIDTH = 200;

  private final static int NODE_HEIGHT = 130;

  private final static int X_GAP = 30;// 60;

  private final static int Y_GAP = 15;// 30;

  private final static int X_OFFSET = 5;

  private final static int Y_OFFSET = 5;

  /** Color of the border of normal tasks. */
  final static Color NORMAL_COLOR = Color.BLUE.brighter();

  /** Color of the border of supertasks. */
  final static Color SUPER_COLOR = Color.RED;

  /** Color of the border of milestones. */
  final static Color MILESTONE_COLOR = Color.BLACK;

  /** Color of the arrows. */
  final static Color ARROW_COLOR = Color.GRAY;

  private final JScrollPane myScrollPane;

  public ActivityOnNodeChart() {
    setBackground(Color.WHITE.brighter());

    this.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        if (myPressedGraphicalNode != null) {
          myPressedGraphicalNode.x = e.getX() - myXClickedOffset;
          myPressedGraphicalNode.y = e.getY() - myYClickedOffset;
          if (e.getX() > getPreferredSize().getWidth()) {
            ActivityOnNodeChart.this.setPreferredSize(new Dimension(myPressedGraphicalNode.x + getNodeWidth() + getxGap(),
                    (int) getPreferredSize().getHeight()));
            revalidate();
          }
          if (e.getY() > getPreferredSize().getHeight()) {
            ActivityOnNodeChart.this.setPreferredSize(new Dimension((int) getPreferredSize().getWidth(),
                    myPressedGraphicalNode.y + getNodeHeight() + getyGap()));
            revalidate();
          }
          repaint();
        }
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        // nothing to do...
      }
    });

    this.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent arg0) {
        // nothing to do...
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
        // nothing to do...
      }

      @Override
      public void mouseExited(MouseEvent arg0) {
        // nothing to do...
      }

      @Override
      public void mousePressed(MouseEvent e) {
        myPressedGraphicalNode = getGraphicalNode(e.getX(), e.getY());
        if (myPressedGraphicalNode != null) {
          myXClickedOffset = e.getX() - myPressedGraphicalNode.x;
          myYClickedOffset = e.getY() - myPressedGraphicalNode.y;

          myPressedGraphicalNode.backgroundColor = myPressedGraphicalNode.backgroundColor.darker();
        }
        repaint();
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (myPressedGraphicalNode != null) {
          if (myPressedGraphicalNode.node.isCritical()) {
            myPressedGraphicalNode.backgroundColor = defaultCriticalColor;
          } else {
            myPressedGraphicalNode.backgroundColor = defaultBackgroundColor;
          }
          myPressedGraphicalNode.x = getGridX(e.getX() - myXClickedOffset + getNodeWidth() / 2);
          myPressedGraphicalNode.y = getGridY(e.getY());
          myPressedGraphicalNode = null;
          repaint();
        }
        recalculatPreferredSize();
        revalidate();
        repaint();
      }
    });
    myScrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  public String getName() {
    return language.getText("aonDiagram");
  }

  int getTextPaddingX() {
    return (int) (textPaddingX * getDpi());
  }

  int getTextPaddingY() {
    return (int) (textPaddingY * getDpi());
  }

  int getNodeWidth() {
    return (int) (NODE_WIDTH * getDpi());
  }

  int getNodeHeight() {
    return (int) (NODE_HEIGHT * getDpi());
  }

  /** Gap between two TaskGraphNodes with the same X coordinate. */
  int getxGap() {
    return (int) (X_GAP * getDpi());
  }

  /** Gap between two TaskGraphNodes with the same Y coordinate. */
  int getyGap() {
    return (int) (Y_GAP * getDpi());
  }

  /** X offset for the top left task graph node. */
  int getxOffset() {
    return (int) (X_OFFSET * getDpi());
  }

  /** Y offset for the top left task graph node. */
  int getYOffset() {
    return (int) (Y_OFFSET * getDpi());
  }

  @Override
  protected void buildPertChart() {
    if (myPertAbstraction == null) {
      myPertAbstraction = new AONChartAbstraction(myTaskManager);
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
      myPertAbstraction = new AONChartAbstraction(myTaskManager);
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

  @Override
  public Object getAdapter(Class adapter) {
    if (adapter.equals(Chart.class)) {
      return this;
    }
    if (adapter.equals(Container.class)) {
      return myScrollPane;
    }
    return null;
  }

}