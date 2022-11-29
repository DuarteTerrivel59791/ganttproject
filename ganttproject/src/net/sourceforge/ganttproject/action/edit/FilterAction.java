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
package net.sourceforge.ganttproject.action.edit;

import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.gui.UIFacade;
<<<<<<< HEAD
import net.sourceforge.ganttproject.gui.about.FilterDialog;
=======
import net.sourceforge.ganttproject.gui.filter.FilterDialog2;
>>>>>>> origin/BRANCH_2_8_9
import net.sourceforge.ganttproject.gui.options.SettingsDialog2;

import java.awt.event.ActionEvent;

/**
 * Action to show the options dialog for the application. It will seach and show
 * all available OptionPageProvider classes
 */
public class FilterAction extends GPAction {
    private final UIFacade myUiFacade;
    private final IGanttProject myProject;

    public FilterAction(IGanttProject project, UIFacade uiFacade) {
        super("filter.app");
        myUiFacade = uiFacade;
        myProject = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (calledFromAppleScreenMenu(e)) {
            return;
        }
<<<<<<< HEAD
        FilterDialog dialog = new FilterDialog(myUiFacade);
        dialog.show();
    }
}
=======
        FilterDialog2 dialog = new FilterDialog2(myUiFacade);
        dialog.show();
    }
}
>>>>>>> origin/BRANCH_2_8_9
