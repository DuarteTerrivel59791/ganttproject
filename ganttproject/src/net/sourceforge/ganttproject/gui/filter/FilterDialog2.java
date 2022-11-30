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

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.task.*;
import org.jdesktop.swingx.JXDatePicker;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;


public class FilterDialog2 {

    UIFacade uiFacade;
    JXDatePicker datePicker1;
    JXDatePicker datePicker2;
    JXDatePicker datePicker3;
    JXDatePicker datePicker4;

    JComboBox<String> prioritySelection;

    private TaskManager taskManager;

    public FilterDialog2(UIFacade uiFacade, TaskManager taskManager) {

        this.uiFacade = uiFacade;
        this.taskManager = taskManager;
    }


    public void filterPage() {

        JFrame frame = new JFrame();
        JPanel result = new JPanel();
        frame.setSize(1000, 600);
        frame.add(result);
        result.setLayout(null);

        datePicker1 = UIUtil.createDatePicker();
        datePicker1.setBounds(300, 20, 200, 20);
        result.add(datePicker1);

        datePicker2 = UIUtil.createDatePicker();
        datePicker2.setBounds(700, 20, 200, 20);
        result.add(datePicker2);

        datePicker3 = UIUtil.createDatePicker();
        datePicker3.setBounds(300, 50, 200, 20);
        result.add(datePicker3);

        datePicker4 = UIUtil.createDatePicker();
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

        JLabel titlePriority = new JLabel("Choose Priority: ");
        titlePriority.setBounds(10,100,230,25);
        result.add(titlePriority);

        titlePriority.setFont(fn);

        JLabel titleDuration = new JLabel("Choose Duration: ");
        titleDuration.setBounds(10,170,230,25);
        result.add(titleDuration);

        titleDuration.setFont(fn);

        String[] priorities= {"Menor", "Baixo", "Normal", "Alto", "Maior"};
        prioritySelection = new JComboBox<>(priorities);
        prioritySelection.setBounds(190,100,230,25);
        result.add(prioritySelection);

        JButton okbutton = new JButton("OK");
        okbutton.setBounds(880, 500, 60, 20);
        result.add(okbutton);

        JButton resetbutton = new JButton("Reset");
        resetbutton.setBounds(800, 500, 60, 20);
        result.add(resetbutton);

        JTextField mindurationbox = new JTextField();
        mindurationbox.setBounds(300,170, 230, 25);
        result.add(mindurationbox);

        JTextField maxdurationbox = new JTextField();
        maxdurationbox.setBounds(700,170, 230, 25);
        result.add(maxdurationbox);

        JLabel minduration = new JLabel("Min Duration: ");
        minduration.setBounds(190,170,230,25);
        result.add(minduration);

        JLabel maxduration = new JLabel("Max Duration: ");
        maxduration.setBounds(600,170,230,25);
        result.add(maxduration);

        JLabel progressTitle = new JLabel("Choose Progress: ");
        progressTitle.setBounds(10,240,230,25);
        result.add(progressTitle);

        progressTitle.setFont(fn);

        /*MultiThumbModel<Integer> model = new DefaultMultiThumbModel<>();
        JXMultiThumbSlider<Integer> progress = new JXMultiThumbSlider<>();
        progress.setModel(model);
        progress.setBounds(200, 240, 230, 25);*/

        SpinnerModel model = new SpinnerNumberModel(0, 0, 100, 1);
        JSpinner progress = new JSpinner(model);
        progress.setBounds(200, 240, 230,25);
        result.add(progress);


        frame.setTitle("Filter");
        frame.setVisible(true);
        frame.setLocation(0,0);
        frame.setLocation(600, 250);


        result.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        okbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //updateFilter();
            }
        });

    }

    private static String i18n(String key) {
        return GanttLanguage.getInstance().getText(key);
    }

    public Date getDatePicker1(){
        return datePicker1.getDate();
    }
    public Date getDatePicker2(){
        return datePicker1.getDate();
    }
    public Date getDatePicker3(){
        return datePicker1.getDate();
    }
    public Date getDatePicker4(){
        return datePicker1.getDate();
    }

    public Task.Priority getPriority(){
        if (prioritySelection.getPrototypeDisplayValue() == "Menor"){
            return Task.Priority.getPriority(0);
        }
        if (prioritySelection.getPrototypeDisplayValue() == "Baixo"){
            return Task.Priority.getPriority(1);
        }
        if (prioritySelection.getPrototypeDisplayValue() == "Normal"){
            return Task.Priority.getPriority(2);
        }
        if (prioritySelection.getPrototypeDisplayValue() == "Alto"){
            return Task.Priority.getPriority(3);
        }
        if (prioritySelection.getPrototypeDisplayValue() == "Maior"){
            return Task.Priority.getPriority(4);
        }
        return Task.Priority.getPriority(2);
    }

    public void updateFilter() {
        //usamos ao clicar no OK depois de inserir os dados do filtro
        Filter f = new FilterClass();

        if (getDatePicker1() != null) {}
        //atualizar o f.set...
        if (getDatePicker2() != null) {}

        //...

        taskManager.setCurrentFilter(f);
    }
}
