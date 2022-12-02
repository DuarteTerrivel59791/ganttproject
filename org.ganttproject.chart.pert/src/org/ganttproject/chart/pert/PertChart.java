/*
Copyright 2003-2012 GanttProject Team

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

import biz.ganttproject.core.option.ChangeValueEvent;
import biz.ganttproject.core.option.ChangeValueListener;
import biz.ganttproject.core.option.FontOption;
import biz.ganttproject.core.option.FontSpec;
import biz.ganttproject.core.option.GPOptionGroup;
import biz.ganttproject.core.option.IntegerOption;
import com.google.common.base.Preconditions;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.ChartSelection;
import net.sourceforge.ganttproject.chart.ChartSelectionListener;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.TaskManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Date;
import java.util.List;

import static net.sourceforge.ganttproject.gui.UIFacade.DEFAULT_DPI;

public abstract class PertChart extends JPanel implements Chart {
  /** Task manager used to build PERT chart. It provides data. */
  TaskManager myTaskManager;
  private IntegerOption myDpi;
  private FontOption myChartFontOption;
  private Font myBaseFont;
  private Font myBoldFont;

  private static int textPaddingX = 10;

  private static int textPaddingY = 0;

  /** List of abstract nodes. */
  protected java.util.List<TaskGraphNode> myTaskGraphNodes;

  /** List of graphical arrows. */
  protected java.util.List<ActivityOnNodePertChart.GraphicalArrow> myGraphicalArrows;

  /** List of graphical nodes (in relation with abstract nodes) */
  private List<GraphicalNode> myGraphicalNodes;

  /** Number of columns */
  protected int nbCols;

  /** PERT chart abstraction used to build graph. */
  private PertChartAbstraction myPertAbstraction;

  private int myMaxX = 1;
  private int myMaxY = 1;

  /** The currently mouse pressed graphical node. */
  private GraphicalNode myPressedGraphicalNode;

  /**
   * Offset between the mouse pointer when clicked on a graphical node and the
   * top left corner of this same node.
   */
  private int myXClickedOffset, myYClickedOffset;

  final static GanttLanguage language = GanttLanguage.getInstance();

  private final JScrollPane myScrollPane;
  public int getTextPaddingX() {
    return (int) (textPaddingX * getDpi());
  }

  public int getTextPaddingY() {
    return (int) (textPaddingY * getDpi());
  }

  final static Color defaultBackgroundColor = new Color(0.9f, 0.9f, 0.9f);

  final static Color defaultCriticalColor = new Color(250, 250, 115).brighter();


  PertChart() {
    setBackground(Color.WHITE.brighter());
    this.addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(final MouseEvent e) {
        if (myPressedGraphicalNode != null) {
          myPressedGraphicalNode.x = e.getX() - myXClickedOffset;
          myPressedGraphicalNode.y = e.getY() - myYClickedOffset;
          if (e.getX() > getPreferredSize().getWidth()) {
            setPreferredSize(new Dimension(myPressedGraphicalNode.x + getNodeWidth() + getxGap(),
                    (int) getPreferredSize().getHeight()));
            revalidate();
          }
          if (e.getY() > getPreferredSize().getHeight()) {
            setPreferredSize(new Dimension((int) getPreferredSize().getWidth(),
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

  abstract int getNodeHeight();

  abstract int getNodeWidth();

  protected abstract int getxGap();

  protected abstract int getyGap();

  @Override
  public void init(IGanttProject project, IntegerOption dpiOption, FontOption chartFontOption) {
    myTaskManager = project.getTaskManager();
    myDpi = Preconditions.checkNotNull(dpiOption);
    myChartFontOption = chartFontOption;
    myChartFontOption.addChangeValueListener(new ChangeValueListener() {
      @Override
      public void changeValue(ChangeValueEvent event) {
        updateFonts();
      }
    });
    updateFonts();
  }

  private void updateFonts() {
    FontSpec fontSpec = myChartFontOption.getValue();
    float scaleFactor = fontSpec.getSize().getFactor() * getDpi();
    myBaseFont = new Font(fontSpec.getFamily(), Font.PLAIN, (int)(10*scaleFactor));
    myBoldFont = myBaseFont.deriveFont(Font.BOLD);
  }

  @Override
  public abstract String getName();

  /** Builds PERT chart. */
  protected abstract void buildPertChart();

  /** This method in not supported by this Chart. */
  @Override
  public Date getStartDate() {
    throw new UnsupportedOperationException();
  }

  /** This method in not supported by this Chart. */
  @Override
  public Date getEndDate() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GPOptionGroup[] getOptionGroups() {
    return null;
  }

  @Override
  public Chart createCopy() {
    return null;
  }

  @Override
  public ChartSelection getSelection() {
    return ChartSelection.EMPTY;
  }

  @Override
  public IStatus canPaste(ChartSelection selection) {
    return Status.CANCEL_STATUS;
  }

  @Override
  public void paste(ChartSelection selection) {
  }

  @Override
  public void addSelectionListener(ChartSelectionListener listener) {
    // No listeners are implemented
  }

  @Override
  public void removeSelectionListener(ChartSelectionListener listener) {
    // No listeners are implemented
  }

  float getDpi() {
    return myDpi.getValue().floatValue() / DEFAULT_DPI;
  }

  Font getBaseFont() {
    return myBaseFont;
  }

  Font getBoldFont() {
    return myBoldFont;
  }

  /** Recalculate preferred size so that graphics fit with nodes positions. */
  private void recalculatPreferredSize() {
    int maxX = 0;
    int maxY = 0;

    for (GraphicalNode gn : myGraphicalNodes) {
      int x = gn.x + getNodeWidth();
      int y = gn.y + getNodeHeight();
      maxX = Math.max(maxX, x);
      maxY = Math.max(maxY, y);
    }
    setPreferredSize(new Dimension(maxX, maxY));
    setMaxX(maxX);
    setMaxY(maxY);
  }

  /**
   * @return The GraphicalNode at the <code>x</code>, <code>y</code> position,
   *         or <code>null</code> if there is no node.
   */
  private GraphicalNode getGraphicalNode(int x, int y) {
    for (GraphicalNode gn : myGraphicalNodes) {
      if (isInRectancle(x, y, gn.x, gn.y, getNodeWidth(), getNodeHeight())) {
        return gn;
      }
    }
    return null;
  }

  /**
   * @return <code>true</code> if the point of coordinates <code>x</code>,
   *         <code>y</code> is in the rectangle described by is top left corner
   *         (<code>rectX</code>, <code>rectY</code>) and dimension (
   *         <code>rectWidth</code>, <code>rectHeight</code>),
   *         <code>false</code> otherwise.
   */
  private static boolean isInRectancle(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight) {
    return (x > rectX && x < rectX + rectWidth && y > rectY && y < rectY + rectHeight);
  }

  /**
   * Max and min coordinates in the graphics that paints the graphical nodes and
   * arrows.
   */
  protected int getMaxX() {
    return myMaxX;
  }

  protected void setMaxX(int myMaxX) {
    this.myMaxX = myMaxX;
  }

  protected int getMaxY() {
    return myMaxY;
  }

  protected void setMaxY(int myMaxY) {
    this.myMaxY = myMaxY;
  }
  private int getGridX(int x) {
    int res = getxOffset();
    int tmp = 0;
    while (res < x) {
      tmp = res;
      res += getNodeWidth() + getxGap();
    }
    return tmp;
  }

  private int getGridY(int y) {
    int res = getYOffset();
    int tmp = 0;
    while (res < y) {
      tmp = res;
      res += getNodeHeight() + getyGap();
    }
    return tmp;
  }


}
