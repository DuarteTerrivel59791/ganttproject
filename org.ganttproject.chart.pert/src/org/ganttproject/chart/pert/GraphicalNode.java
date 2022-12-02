package org.ganttproject.chart.pert;

import net.sourceforge.ganttproject.util.StringUtils;

import javax.swing.*;
import java.awt.*;

public abstract class GraphicalNode extends JComponent {

        protected TaskGraphNode node;
        private int col = -1; // determines X
        private int row = -1;

        protected Color backgroundColor = null;

        ActivityOnNodePertChart chart;
        int x , y;

    GraphicalNode(TaskGraphNode node, ActivityOnNodePertChart chart) {
            this.row = -1;
            this.col = -1;
            this.node = node;
            this.backgroundColor = chart.defaultBackgroundColor;
            if (node.isCritical()) {
                this.backgroundColor = chart.defaultCriticalColor;
            }
            this.chart = chart;
            this.x = chart.getxOffset();
            this.y = chart.getYOffset();
        }

        /**
         * Updates the linked abstract node.
         *
         * @param node
         *          new linked abstract node.
         */
        void updateData(TaskGraphNode node) {
            this.node = node;
        }

        /**
         * Paints the graphical node.
         *
         * @param g
         *          Graphics where the graphical node is to be painted.
         */
        @Override
    public void paint(Graphics g) {
        if (node.isCritical()) {
            this.backgroundColor = chart.defaultCriticalColor;
        } else {
            this.backgroundColor = chart.defaultBackgroundColor;
        }
        paintMe(g);
    }

    abstract void paintMe(Graphics g);

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }


}
