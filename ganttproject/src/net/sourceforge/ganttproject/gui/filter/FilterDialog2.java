/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 Dmitry Barashev

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
package net.sourceforge.ganttproject.gui.filter;
import javax.swing.*;

import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.language.GanttLanguage;
import org.jdesktop.swingx.JXDatePicker;

import java.awt.*;

public class FilterDialog2 {

    UIFacade uiFacade;

    public FilterDialog2(UIFacade uiFacade) {
        this.uiFacade = uiFacade;
    }


    public void filterPage() {

        JFrame frame = new JFrame();
        JPanel result = new JPanel();
        frame.setSize(1000, 600);
        frame.add(result);
        result.setLayout(null);

        JXDatePicker datePicker1 = UIUtil.createDatePicker();
        datePicker1.setBounds(300, 20, 200, 20);
        result.add(datePicker1);

        JXDatePicker datePicker2 = UIUtil.createDatePicker();
        datePicker2.setBounds(700, 20, 200, 20);
        result.add(datePicker2);

        JXDatePicker datePicker3 = UIUtil.createDatePicker();
        datePicker3.setBounds(300, 50, 200, 20);
        result.add(datePicker3);

        JXDatePicker datePicker4 = UIUtil.createDatePicker();
        datePicker4.setBounds(700, 50, 200, 20);
        result.add(datePicker4);

        JLabel titleDate = new JLabel("Choose Dates: ");
        titleDate.setBounds(10,30,230,25);
        result.add(titleDate);

        JLabel initDate1 = new JLabel("Choose Initial Date: ");
        initDate1.setBounds(190,17,230,25);
        result.add(initDate1);

        JLabel endDate1 = new JLabel("Choose End Date: ");
        endDate1.setBounds(600,17,230,25);
        result.add(endDate1);

        JLabel initDate2 = new JLabel("Choose Initial Date: ");
        initDate2.setBounds(190,47,230,25);
        result.add(initDate2);

        JLabel endDate2 = new JLabel("Choose End Date: ");
        endDate2.setBounds(600,47,230,25);
        result.add(endDate2);

        Font fn = new Font("Arial", Font.PLAIN, 20);
        titleDate.setFont(fn);

        frame.setTitle("Filter");
        frame.setVisible(true);
        frame.setLocation(0,0);
        frame.setLocation(600, 250);


        result.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

    }

    private static String i18n(String key) {
        return GanttLanguage.getInstance().getText(key);
    }

}
