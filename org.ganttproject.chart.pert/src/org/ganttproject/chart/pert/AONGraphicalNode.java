package org.ganttproject.chart.pert;

import net.sourceforge.ganttproject.util.StringUtils;

import java.awt.*;

public class AONGraphicalNode extends GraphicalNode {

    AONGraphicalNode(PertChartAbstraction.TaskGraphNode node, PertChart chart) {
        super(node, chart);
    }

    public void paint(Graphics g) {
        if (node.isCritical()) {
            this.backgroundColor = chart.defaultCriticalColor;
        } else {
            this.backgroundColor = chart.defaultBackgroundColor;
        }
        paintMe(g);
    }

    private void paintMe(Graphics g) {
        Font f = g.getFont();
        g.setFont(chart.getBoldFont());
        FontMetrics fontMetrics = g.getFontMetrics(g.getFont());

        int type = this.node.getType();
        Color color;
        switch (type) {
            case PertChartAbstraction.Type.NORMAL:
                color = chart.NORMAL_COLOR;
                break;
            case PertChartAbstraction.Type.SUPER:
                color = chart.SUPER_COLOR;
                break;
            case PertChartAbstraction.Type.MILESTONE:
                color = chart.MILESTONE_COLOR;
                break;
            default:
                color = chart.NORMAL_COLOR;
        }
        g.setColor(this.backgroundColor);

        g.fillRoundRect(x, y, chart.getNodeWidth(), chart.getNodeHeight(), 16, 16);
        g.setColor(color);
        g.drawRoundRect(x, y, chart.getNodeWidth(), chart.getNodeHeight(), 16, 16);
        g.drawRoundRect(x + 1, y + 1, chart.getNodeWidth() - 2, chart.getNodeHeight() - 2, 14, 14);

        g.drawLine(x, y + chart.getTextPaddingY() + fontMetrics.getHeight() + chart.getYOffset(), x + chart.getNodeWidth(), y + chart.getTextPaddingY() + fontMetrics.getHeight()
                + chart.getYOffset());

        g.setColor(Color.BLACK);
        String name = node.getName();

        g.drawString(StringUtils.getTruncatedString(name, chart.getNodeWidth() - chart.getTextPaddingX(), fontMetrics), x + chart.getTextPaddingX(), y + chart.getTextPaddingY()
                + fontMetrics.getHeight());

        g.setFont(chart.getBaseFont());
        fontMetrics = g.getFontMetrics(g.getFont());

        g.setColor(Color.BLACK);
        g.drawString(chart.language.getText("start") + ": " + node.getStartDate().toString(), x + chart.getTextPaddingX(),
                (int) (y + chart.getTextPaddingY() + 2.3 * fontMetrics.getHeight()));
        g.drawString(chart.language.getText("end") + ": " + node.getEndDate().toString(), x + chart.getTextPaddingX(),
                (int) (y + chart.getTextPaddingY() + 3.3 * fontMetrics.getHeight()));

        if (node.getDuration() != null) {
            g.drawString(chart.language.getText("duration") + ": " + node.getDuration().getLength(), x + chart.getTextPaddingX(),
                    (int) (y + chart.getTextPaddingY() + 4.3 * fontMetrics.getHeight()));


            g.setColor(color);
            g.drawLine(x, (int) (y + chart.getTextPaddingY() + 4.6 * fontMetrics.getHeight() + chart.getYOffset()), x + chart.getNodeWidth(), (int) (y + chart.getTextPaddingY() + 4.6 * fontMetrics.getHeight()
                    + chart.getYOffset()));


            g.setColor(Color.BLACK);
            g.drawString(chart.language.getText("lateStart") + ": " + node.getLST().toString(), x + chart.getTextPaddingX(),
                    (int) (y + chart.getTextPaddingY() + 6.3 * fontMetrics.getHeight()));

            g.drawString(chart.language.getText("lateFinish") + ": " + node.getLFT().toString(), x + chart.getTextPaddingX(),
                    (int) (y + chart.getTextPaddingY() + 7.3 * fontMetrics.getHeight()));

            g.drawString(chart.language.getText("slack") + ": " + node.getSlack().toString(), x + chart.getTextPaddingX(),
                    (int) (y + chart.getTextPaddingY() + 8.3 * fontMetrics.getHeight()));
        }

        g.setFont(f);

    }
}
