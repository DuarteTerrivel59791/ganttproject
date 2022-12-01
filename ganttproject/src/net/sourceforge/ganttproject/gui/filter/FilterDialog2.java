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

import biz.ganttproject.core.time.CalendarFactory;
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

    Filter f;
    JXDatePicker datePicker1;
    JXDatePicker datePicker2;
    JXDatePicker datePicker3;
    JXDatePicker datePicker4;

    JComboBox<String> minPrioritySelection;
    JComboBox<String> maxPrioritySelection;

    JTextField minDurationBox;
    JTextField maxDurationBox;

    JSpinner minProgress;
    JSpinner maxProgress;

    private TaskManager taskManager;

    public FilterDialog2(UIFacade uiFacade, TaskManager taskManager) {
        this.uiFacade = uiFacade;
        this.taskManager = taskManager;
        f = taskManager.getCurrentFilter();
    }


    public void filterPage() {

        final JFrame frame = new JFrame();
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

        String[] priorities= {"", "Menor", "Baixo", "Normal", "Alto", "Maior"};
        minPrioritySelection = new JComboBox<>(priorities);
        minPrioritySelection.setBounds(190,100,230,25);
        result.add(minPrioritySelection);

        maxPrioritySelection = new JComboBox<>(priorities);
        maxPrioritySelection.setBounds(500,100,230,25);
        result.add(maxPrioritySelection);

        JButton okbutton = new JButton("OK");
        okbutton.setBounds(880, 500, 60, 20);
        result.add(okbutton);

        JButton resetbutton = new JButton("Reset");
        resetbutton.setBounds(800, 500, 60, 20);
        result.add(resetbutton);

        minDurationBox = new JTextField();
        minDurationBox.setBounds(300,170, 230, 25);
        result.add(minDurationBox);

        maxDurationBox = new JTextField();
        maxDurationBox.setBounds(700,170, 230, 25);
        result.add(maxDurationBox);

        JLabel minDuration = new JLabel("Min Duration: ");
        minDuration.setBounds(190,170,230,25);
        result.add(minDuration);

        JLabel maxDuration = new JLabel("Max Duration: ");
        maxDuration.setBounds(600,170,230,25);
        result.add(maxDuration);

        JLabel progressTitle = new JLabel("Choose Progress: ");
        progressTitle.setBounds(10,240,230,25);
        result.add(progressTitle);

        progressTitle.setFont(fn);

        SpinnerModel minModel = new SpinnerNumberModel(0, 0, 100, 1);
        minProgress = new JSpinner(minModel);
        minProgress.setBounds(200, 240, 230,25);
        result.add(minProgress);

        SpinnerModel maxModel = new SpinnerNumberModel(100, 0, 100, 1);
        maxProgress = new JSpinner(maxModel);
        maxProgress.setBounds(500, 240, 230,25);
        result.add(maxProgress);

        refreshValues();

        frame.setTitle("Filter");
        frame.setVisible(true);
        frame.setLocation(0,0);
        frame.setLocation(600, 250);


        result.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        resetbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                taskManager.resetFilter();
                resetValues();
            }
        });
        okbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilter();
                frame.dispose();
            }
        });
    }

    private void resetValues(){
        datePicker1.setDate(null);
        datePicker2.setDate(null);
        datePicker3.setDate(null);
        datePicker4.setDate(null);
        minPrioritySelection.setSelectedIndex(0);
        maxPrioritySelection.setSelectedIndex(0);
        minDurationBox.setText("");
        maxDurationBox.setText("");
        minProgress.setValue(0);
        maxProgress.setValue(100);
    }

    private void refreshValues(){
        if (f.hasMinStartDate())
            datePicker1.setDate(f.getMinStartDate().getTime());
        if (f.hasMaxStartDate())
            datePicker2.setDate(f.getMaxStartDate().getTime());
        if (f.hasMinEndDate())
            datePicker3.setDate(f.getMinEndDate().getTime());
        if(f.hasMaxEndDate())
            datePicker4.setDate(f.getMaxEndDate().getTime());
        if(f.hasMinPriority())
            minPrioritySelection.setSelectedIndex(f.getMinPriority().ordinal() + 1);
        if (f.hasMaxPriority())
            maxPrioritySelection.setSelectedIndex(f.getMaxPriority().ordinal() + 1);
        if(f.hasMinLength())
            minDurationBox.setText(Integer.toString(f.getMinLength()));
        if (f.hasMaxLength())
            maxDurationBox.setText(Integer.toString(f.getMaxLength()));
        if (f.hasMinCompletion())
            minProgress.getModel().setValue(f.getMinCompletion());
        if (f.hasMaxCompletion())
            maxProgress.getModel().setValue(f.getMaxCompletion());
    }

    private static String i18n(String key) {
        return GanttLanguage.getInstance().getText(key);
    }

    public Task.Priority getPriority(String priority){
        if (priority.equals("Menor")){
            return Task.Priority.getPriority(0);
        }
        else if (priority.equals("Baixo")){
            return Task.Priority.getPriority(1);
        }
        else if (priority.equals( "Normal")){
            return Task.Priority.getPriority(2);
        }
        else if (priority.equals("Alto")){
            return Task.Priority.getPriority(3);
        }
        else if (priority.equals("Maior")){
            return Task.Priority.getPriority(4);
        }
        return null;
    }

    public int getDuration(String duration){
        int d = -1;
        try{
            d = Integer.parseInt(duration);
        } catch (NumberFormatException e){
        }
        return d;
    }

    public void updateFilter() {
        //usamos ao clicar no OK depois de inserir os dados do filtro

        Date minStartDate = datePicker1.getDate();
        Date maxStartDate = datePicker2.getDate();
        Date minEndDate = datePicker3.getDate();
        Date maxEndDate = datePicker4.getDate();
        Task.Priority minPriority = getPriority((String) minPrioritySelection.getSelectedItem());
        Task.Priority maxPriority = getPriority((String) maxPrioritySelection.getSelectedItem());

        int minDuration = getDuration(minDurationBox.getText());
        int maxDuration = getDuration(maxDurationBox.getText());

        int minP = (int) minProgress.getValue();
        int maxP = (int) maxProgress.getValue();

        if (minStartDate != null) {
            f.setMinStartDate(CalendarFactory.createGanttCalendar(minStartDate));
        }
        if (maxStartDate != null) {
            f.setMaxStartDate(CalendarFactory.createGanttCalendar(maxStartDate));
        }
        if (minEndDate != null) {
            f.setMinEndDate(CalendarFactory.createGanttCalendar(minEndDate));
        }
        if (maxEndDate != null) {
            f.setMaxEndDate(CalendarFactory.createGanttCalendar(maxEndDate));
        }
        if (minPriority != null){
            f.setMinPriority(minPriority);
        }
        if (maxPriority != null){
            f.setMaxPriority(maxPriority);
        }
        if (minDuration != -1){
            f.setMinLength(minDuration);
        }
        if (maxDuration != -1){
            f.setMaxLength(maxDuration);
        }
        f.setMaxCompletion(maxP);
        f.setMinCompletion(minP);

        taskManager.setCurrentFilter(f);
    }
}
