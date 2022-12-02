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
import net.sourceforge.ganttproject.GanttExportSettings;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.ChartSelection;
import net.sourceforge.ganttproject.chart.ChartSelectionListener;
import net.sourceforge.ganttproject.chart.export.ChartImageVisitor;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.TaskManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
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

  PertChart() {
  }

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


  /************************************************************************************************/

  /** Color of the border of normal tasks. */
  final static Color NORMAL_COLOR = Color.BLUE.brighter();

  /** Color of the border of supertasks. */
  final static Color SUPER_COLOR = Color.RED;

  /** Color of the border of milestones. */
  final static Color MILESTONE_COLOR = Color.BLACK;

  /** List of abstract nodes. */
  protected java.util.List<TaskGraphNode> myTaskGraphNodes;

  /** List of graphical arrows. */
  protected java.util.List<GraphicalArrow> myGraphicalArrows;

  /** List of graphical nodes (in relation with abstract nodes) */
  protected List<GraphicalNode> myGraphicalNodes;

  // private Map myMapPositionListOfNodes;

  /** Number of columns */
  protected int nbCols;

  /** PERT chart abstraction used to build graph. */
  protected ChartAbstraction myPertAbstraction;

  protected int myMaxX = 1;
  protected int myMaxY = 1;

  /** The currently mouse pressed graphical node. */
  protected GraphicalNode myPressedGraphicalNode;

  /**
   * Offset between the mouse pointer when clicked on a graphical node and the
   * top left corner of this same node.
   */
  protected int myXClickedOffset, myYClickedOffset;

  final static GanttLanguage language = GanttLanguage.getInstance();

  abstract int getTextPaddingX();

  abstract int getTextPaddingY();

  abstract int getNodeWidth();

  abstract int getNodeHeight();

  abstract int getxGap();

  abstract int getyGap();

  abstract int getxOffset();

  abstract int getYOffset();

  /** Recalculate preferred size so that graphics fit with nodes positions. */
  protected void recalculatPreferredSize() {
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
   * @return <code>true</code> if the point of coordinates <code>x</code>,
   *         <code>y</code> is in the rectangle described by is top left corner
   *         (<code>rectX</code>, <code>rectY</code>) and dimension (
   *         <code>rectWidth</code>, <code>rectHeight</code>),
   *         <code>false</code> otherwise.
   */
  protected static boolean isInRectancle(int x, int y, int rectX, int rectY, int rectWidth, int rectHeight) {
    return (x > rectX && x < rectX + rectWidth && y > rectY && y < rectY + rectHeight);
  }

  /**
   * @return The GraphicalNode at the <code>x</code>, <code>y</code> position,
   *         or <code>null</code> if there is no node.
   */
  protected GraphicalNode getGraphicalNode(int x, int y) {
    for (GraphicalNode gn : myGraphicalNodes) {
      if (isInRectancle(x, y, gn.x, gn.y, getNodeWidth(), getNodeHeight())) {
        return gn;
      }
    }
    return null;
  }

  /** Updates the data for each nodes. */
  protected void updateGraphNodesInfo() {
    if (myTaskGraphNodes != null) {
      for (TaskGraphNode tgn : myTaskGraphNodes) {
        GraphicalNode graphicalNode = getGraphicalNodeByID(tgn.getID());
        if (graphicalNode != null) {
          graphicalNode.updateData(tgn);
        }
      }
    }
  }

  protected boolean isZeroPosition(TaskGraphNode taskGraphNode) {
    for (TaskGraphNode t : myTaskGraphNodes) {
      if (t.getSuccessors().contains(taskGraphNode)) {
        return false;
      }
    }
    return true;
  }

  protected int getGridX(int x) {
    int res = getxOffset();
    int tmp = 0;
    while (res < x) {
      tmp = res;
      res += getNodeWidth() + getxGap();
    }
    return tmp;
  }

  protected int getGridY(int y) {
    int res = getYOffset();
    int tmp = 0;
    while (res < y) {
      tmp = res;
      res += getNodeHeight() + getyGap();
    }
    return tmp;
  }

  protected void moveDown(GraphicalNode graphicalNode) {
    int row = graphicalNode.getRow();
    while (this.isOccupied(++row, graphicalNode.getCol())) {
      // yup, the body is empty. But there is ++row above.
    }
    graphicalNode.setRow(row);
  }

  protected GraphicalNode getNode(int row, int col) {
    for (GraphicalNode node : getNodeInColumn(col)) {
      if (node.getRow() == row) {
        return node;
      }
    }
    return null;
  }

  protected void moveRight(GraphicalNode graphicalNode) {
    for (TaskGraphNode successor : graphicalNode.node.getSuccessors()) {
      moveRight(getGraphicalNodeByID(successor.getID()));
    }
    int newCol = graphicalNode.getCol() + 1;
    if (isOccupied(graphicalNode.getRow(), newCol)) {
      moveRight(getNode(graphicalNode.getRow(), newCol));
    }
    graphicalNode.setCol(newCol);
    if (newCol == nbCols) {
      this.nbCols++;
    }
  }

  protected void remove(GraphicalNode graphicalNode) {
    myGraphicalNodes.remove(graphicalNode);
    if (graphicalNode.getCol() == -1) {
      return;
    }

    for (GraphicalNode gnode : getNodeInColumn(graphicalNode.getCol())) {
      if (gnode.getRow() > graphicalNode.getRow()) {
        gnode.setRow(gnode.getRow() - 1);
      }
    }

    // int iNbRow = ((Integer)this.rowsList.get(new
    // Integer(graphicalNode.col))).intValue();
    // rowsList.put(new Integer(graphicalNode.col), new Integer(iNbRow-1));

    if (graphicalNode.getCol() == nbCols - 1) {
      List<GraphicalNode> list = getNodeInColumn(this.nbCols - 1);
      while (list.size() == 0) {
        nbCols--;
        list = getNodeInColumn(nbCols - 1);
      }
    }

    graphicalNode.setRow(-1);
    graphicalNode.setCol(-1);
  }

  protected GraphicalNode getGraphicalNodeByID(int id) {
    for (GraphicalNode gn : myGraphicalNodes) {
      if (gn.node.getID() == id) {
        return gn;
      }
    }
    return null;
  }

  // TODO Translate:
  /** ajoute la graphical node dans la map position/liste des successeurs Bounjour*/
  protected void add(int col, GraphicalNode graphicalNode) {
    myGraphicalNodes.remove(graphicalNode);

    if (nbCols - 1 < col) {
      nbCols = col + 1;
    }

    int row = 0;
    while (isOccupied(row, col)) {
      row++;
    }

    graphicalNode.setRow(row);
    graphicalNode.setCol(col);

    myGraphicalNodes.add(graphicalNode);

    // rowsList.put(new Integer(col), new Integer(iNbRow+1));
  }

  protected List<TaskGraphNode> getNodesThatAreInASpecificSuccessorPosition(int col) {
    List<GraphicalNode> graphicaleNodes = getNodeInColumn(col);
    if (graphicaleNodes.size() == 0) {
      return null;
    }

    List<TaskGraphNode> res = new ArrayList<>();
    for (GraphicalNode gn : graphicaleNodes) {
      res.addAll(gn.node.getSuccessors());
    }

    return res;
  }

  /**
   * Get the list of GraphicalNode that are in a column.
   *
   * @param col
   *          the column number to look in
   * @return the list of GraphicalNode in the col
   */
  protected List<GraphicalNode> getNodeInColumn(int col) {
    List<GraphicalNode> list = new ArrayList<>();
    for (GraphicalNode gnode : myGraphicalNodes) {
      if (gnode.getCol() == col) {
        list.add(gnode);
      }
    }
    return list;
  }

  protected boolean isOccupied(int row, int col) {
    for (GraphicalNode gnode : getNodeInColumn(col)) {
      if (gnode.getRow() == row) {
        return true;
      }
    }
    return false;
  }

  /*
  private List<TaskGraphNode> getAncestor(TaskGraphNode tgn) {
    List<TaskGraphNode> ancestors = new ArrayList<>();
    for (TaskGraphNode tnode : myTaskGraphNodes) {
      List<TaskGraphNode> successor = tnode.getSuccessors();
      if (successor.contains(tgn)) {
        ancestors.add(tnode);
      }
    }
    return ancestors;
  }
  */

  protected boolean isCrossingNode(GraphicalNode gnode) {
    for (TaskGraphNode ancestor : myPertAbstraction.getAncestor(gnode.node)) {
      GraphicalNode gancestor = getGraphicalNodeByID(ancestor.getID());
      if (gancestor.getCol() < gnode.getCol() - 1) {
        for (int col = gnode.getCol() - 1; col > gancestor.getCol(); col--) {
          if (this.isOccupied(gnode.getRow(), col)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected void avoidCrossingNode() {
    if (nbCols == 0) {
      return;
    }

    int col = nbCols - 1;
    while (col > 0) {
      boolean hasmoved = false;
      for (GraphicalNode gnode : getNodeInColumn(col)) {
        while (isCrossingNode(gnode)) {
          moveDown(gnode);
          hasmoved = true;
        }
      }
      if (hasmoved && col < nbCols - 1) {
        col++;
      } else {
        col--;
      }
    }
  }

  protected boolean isCrossingArrow(GraphicalNode gnode) {
    // search for the successors with the highest and lowest position
    int maxUp = Integer.MAX_VALUE, maxDown = -1;
    for (TaskGraphNode successorNode : gnode.node.getSuccessors()) {
      GraphicalNode successor = getGraphicalNodeByID(successorNode.getID());
      if (successor.getRow() < maxUp) {
        maxUp = successor.getRow();
      }
      if (successor.getRow() > maxDown) {
        maxDown = successor.getRow();
      }
    }

    // Find all other nodes on the same column
    List<GraphicalNode> otherNodes = this.getNodeInColumn(gnode.getCol());
    otherNodes.remove(gnode);

    // TODO Translate
    // parcours des nodes sur la même colonne
    List<TaskGraphNode> gnodeSuccessors = gnode.node.getSuccessors();
    for (GraphicalNode otherNode : otherNodes) {
      for (TaskGraphNode otherSuccessor : otherNode.node.getSuccessors()) {
        GraphicalNode otherSuccessorNode = getGraphicalNodeByID(otherSuccessor.getID());
        if (maxUp < gnode.getRow()) {
          // some arrows are going up
          if (otherSuccessorNode.getRow() <= gnode.getRow() && !gnodeSuccessors.contains(otherSuccessor)) {
            return true;
          }
        }
        if (maxDown > gnode.getRow()) {
          // some arrow are going down
          if (otherSuccessorNode.getRow() >= gnode.getRow() && !gnodeSuccessors.contains(otherSuccessor)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  protected void avoidCrossingLine() {
    boolean restart;
    do {
      restart = false;
      for (int col = 0; col < nbCols && restart == false; col++) {
        List<GraphicalNode> list = getNodeInColumn(col);
        if (list.size() > 1) {
          for (GraphicalNode gnode : list) {
            if (isCrossingArrow(gnode)) {
              moveRight(gnode);
              avoidCrossingNode();
              // Stop processing and start over
              restart = true;
              break;
            }
          }
        }
      }
    } while (restart);
  }

  protected void removeEmptyColumn() {
    for (int col = nbCols - 1; col >= 0; col--) {
      if (this.getNodeInColumn(col).size() == 0) {
        if (col != nbCols - 1) {
          for (int c = col + 1; c < nbCols; c++) {
            for (GraphicalNode gnode : getNodeInColumn(c)) {
              gnode.setCol(gnode.getCol()-1);
            }
          }
        }
        nbCols--;
      }
    }
  }

  @Override
  public void buildImage(GanttExportSettings settings, ChartImageVisitor imageVisitor) {
    // TODO Auto-generated method stub

  }

  @Override
  public RenderedImage getRenderedImage(GanttExportSettings settings) {
    BufferedImage image = new BufferedImage(getMaxX(), getMaxY(), BufferedImage.TYPE_INT_RGB);
    Graphics g = image.getGraphics();
    g.fillRect(0, 0, getMaxX(), getMaxY());
    paint(g);
    return image;
  }

  @Override
  public void reset() {
    myPertAbstraction = null;
  }

  @Override
  public void paint(Graphics g) {
    this.buildPertChart();
    super.paint(g);
    for (GraphicalNode myGraphicalNode : myGraphicalNodes) {
      myGraphicalNode.paint(g);
    }
    for (GraphicalArrow myGraphicalArrow : myGraphicalArrows) {
      myGraphicalArrow.paintMe(g);
    }
  }

  protected void calculateGraphicalNodesCoordinates() {
    setMaxX(0);
    setMaxY(0);
    for (GraphicalNode gnode : myGraphicalNodes) {
      gnode.x += (getNodeWidth() + getxGap()) * gnode.getCol();
      gnode.y += (getNodeHeight() + getyGap()) * gnode.getRow();

      setMaxX(gnode.x > getMaxX() ? gnode.x : getMaxX());
      setMaxY(gnode.y > getMaxY() ? gnode.y : getMaxY());
    }

    setMaxX(getMaxX() + getNodeWidth() + getxGap());
    setMaxY(getMaxY() + getNodeHeight() + getyGap());
  }

  protected void calculateArrowsCoordinates() {
    for (GraphicalNode gn : myGraphicalNodes) {
      for (TaskGraphNode tgn : gn.node.getSuccessors()) {
        int id = tgn.getID();
        GraphicalArrow arrow = new GraphicalArrow(gn, getGraphicalNodeByID(id));
        myGraphicalArrows.add(arrow);
      }
    }
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

  final static Color defaultBackgroundColor = new Color(0.9f, 0.9f, 0.9f);

  final static Color defaultCriticalColor = new Color(250, 250, 115).brighter();

  protected static int textPaddingX = 10;

  protected static int textPaddingY = 0;

  @Override
  public IGanttProject getProject() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setDimensions(int height, int width) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setStartDate(Date startDate) {
    // TODO Auto-generated method stub
  }

  private final static int ARROW_HEIGHT = 10;

  private final static int ARROW_WIDTH = 15;

  private final static int ARROW_CORNER_WIDTH = 6;

  /** Color of the arrows. */
  final static Color ARROW_COLOR = Color.GRAY;

  private int getArrowHeight() {
    return (int) (ARROW_HEIGHT * getDpi());
  }

  private int getArrowWidth() {
    return (int) (ARROW_WIDTH * getDpi());
  }

  private int getArrowCornerWidth() {
    return (int) (ARROW_CORNER_WIDTH * getDpi());
  }

  /**
   * Graphical arrow that is rendered on graphics.
   *
   * @author bbaranne
   */
  private class GraphicalArrow {
    GraphicalNode from;

    GraphicalNode to;

    GraphicalArrow(GraphicalNode from, GraphicalNode to) {
      this.from = from;
      this.to = to;
    }

    private void paintMe(Graphics g) {
      g.setColor(ARROW_COLOR);

      int arrowFromX, arrowFromY;
      int arrowToX, arrowToY;

      arrowFromX = from.x + getNodeWidth();
      arrowFromY = from.y + getNodeHeight() / 2;
      arrowToX = to.x;
      arrowToY = to.y + getNodeHeight() / 2;

      int[] xS = { arrowToX, arrowToX - getArrowWidth(), arrowToX - getArrowWidth()};
      int[] yS = { arrowToY, arrowToY - getArrowHeight() / 2, arrowToY + getArrowHeight() / 2 };
      int nb = xS.length;

      g.fillPolygon(xS, yS, nb); // flèche

      if (arrowFromY != arrowToY) {
        int[] middleLineX = { arrowFromX + getxGap() / 2 - getArrowCornerWidth(), arrowFromX + getxGap() / 2,
                arrowFromX + getxGap() / 2, arrowFromX + getxGap() / 2 + getArrowCornerWidth()};
        int[] middleLineY = { arrowFromY,
                (arrowFromY < arrowToY ? arrowFromY + getArrowCornerWidth() : arrowFromY - getArrowCornerWidth()),
                (arrowFromY < arrowToY ? arrowToY - getArrowCornerWidth() : arrowToY + getArrowCornerWidth()), arrowToY };
        int middleLineNb = middleLineX.length;
        g.drawPolyline(middleLineX, middleLineY, middleLineNb);

        g.drawLine(arrowFromX, arrowFromY, middleLineX[0], middleLineY[0]);
        g.drawLine(arrowFromX + getxGap() / 2 + getArrowCornerWidth(), arrowToY, arrowToX - getArrowWidth(), arrowToY);
      } else {
        g.drawLine(arrowFromX, arrowFromY, arrowToX, arrowToY);
      }

      // g.drawString(from.node.getName(),arrowFromX+5,arrowFromY+15);
      // g.drawString(to.node.getName(),arrowFromX+50,arrowFromY+15);
    }
  }
}
