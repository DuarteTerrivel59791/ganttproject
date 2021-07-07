/*
Copyright 2002-2019 Alexandre Thomas, BarD Software s.r.o

This file is part of GanttProject, an open-source project management tool.

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
package net.sourceforge.ganttproject;

import biz.ganttproject.LoggerApi;
import biz.ganttproject.app.FXSearchUi;
import biz.ganttproject.app.FXToolbar;
import biz.ganttproject.app.FXToolbarBuilder;
import biz.ganttproject.core.option.ChangeValueEvent;
import biz.ganttproject.core.option.ChangeValueListener;
import biz.ganttproject.platform.UpdateKt;
import biz.ganttproject.platform.UpdateOptions;
import biz.ganttproject.storage.cloud.GPCloudOptions;
import biz.ganttproject.storage.cloud.GPCloudStatusBar;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.scene.Scene;
import kotlin.Unit;
import net.sourceforge.ganttproject.action.ActiveActionProvider;
import net.sourceforge.ganttproject.action.ArtefactAction;
import net.sourceforge.ganttproject.action.ArtefactDeleteAction;
import net.sourceforge.ganttproject.action.ArtefactNewAction;
import net.sourceforge.ganttproject.action.ArtefactPropertiesAction;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.action.edit.EditMenu;
import net.sourceforge.ganttproject.action.help.HelpMenu;
import net.sourceforge.ganttproject.action.project.ProjectMenu;
import net.sourceforge.ganttproject.action.resource.ResourceActionSet;
import net.sourceforge.ganttproject.action.view.ViewCycleAction;
import net.sourceforge.ganttproject.action.view.ViewMenu;
import net.sourceforge.ganttproject.action.zoom.ZoomActionSet;
import net.sourceforge.ganttproject.chart.Chart;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.Document.DocumentException;
import net.sourceforge.ganttproject.export.CommandLineExportApplication;
import net.sourceforge.ganttproject.gui.CommandLineProjectOpenStrategy;
import net.sourceforge.ganttproject.gui.ResourceTreeUIFacade;
import net.sourceforge.ganttproject.gui.TaskTreeUIFacade;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.gui.UIUtil;
import net.sourceforge.ganttproject.gui.scrolling.ScrollingManager;
import net.sourceforge.ganttproject.io.GPSaver;
import net.sourceforge.ganttproject.io.GanttXMLOpen;
import net.sourceforge.ganttproject.io.GanttXMLSaver;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.language.GanttLanguage.Event;
import net.sourceforge.ganttproject.parser.GPParser;
import net.sourceforge.ganttproject.parser.ParserFactory;
import net.sourceforge.ganttproject.plugins.PluginManager;
import net.sourceforge.ganttproject.print.PrintManager;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.resource.ResourceEvent;
import net.sourceforge.ganttproject.resource.ResourceView;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.CustomColumnsStorage;
import net.sourceforge.ganttproject.task.TaskManagerImpl;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main frame of the project
 */
public class GanttProject extends GanttProjectBase implements ResourceView, GanttLanguage.Listener {

  private final LoggerApi boundsLogger = GPLogger.create("Window.Bounds");
  private final LoggerApi startupLogger = GPLogger.create("Window.Startup");
  /**
   * The JTree part.
   */
  private GanttTree2 tree;

  /**
   * GanttGraphicArea for the calendar with Gantt
   */
  private GanttGraphicArea area;

  /**
   * GanttPeoplePanel to edit person that work on the project
   */
  private GanttResourcePanel resp;

  private final EditMenu myEditMenu;

  private final ProjectMenu myProjectMenu;

  /**
   * Informations for the current project.
   */
  public PrjInfos prjInfos = new PrjInfos();

  /**
   * Boolean to know if the file has been modify
   */
  public boolean askForSave = false;

  /**
   * Is the application only for viewer.
   */
  public boolean isOnlyViewer;

  private final ResourceActionSet myResourceActions;

  private final ZoomActionSet myZoomActions;

  //private final FacadeInvalidator myFacadeInvalidator;

  private UIConfiguration myUIConfiguration;

  private final GanttOptions options;

  private TaskContainmentHierarchyFacadeImpl myCachedFacade;

  private ArrayList<GanttPreviousState> myPreviousStates = new ArrayList<GanttPreviousState>();

  private MouseListener myStopEditingMouseListener = null;

  private GanttChartTabContentPanel myGanttChartTabContent;

  private ResourceChartTabContentPanel myResourceChartTabContent;

  private List<RowHeightAligner> myRowHeightAligners = Lists.newArrayList();

  private ParserFactory myParserFactory;

  private HumanResourceManager myHumanResourceManager;

  private RoleManager myRoleManager;

  private static Consumer<Boolean> ourQuitCallback;

  private FXSearchUi mySearchUi;

  public GanttProject(boolean isOnlyViewer) {
    startupLogger.debug("Creating main frame...");
    ToolTipManager.sharedInstance().setInitialDelay(200);
    ToolTipManager.sharedInstance().setDismissDelay(60000);

    myCalendar.addListener(() -> GanttProject.this.setModified());
    Mediator.registerTaskSelectionManager(getTaskSelectionManager());

    this.isOnlyViewer = isOnlyViewer;
    if (!isOnlyViewer) {
      setTitle(language.getText("appliTitle"));
    } else {
      setTitle("GanttViewer");
    }
    setFocusable(true);
    startupLogger.debug("1. loading look'n'feels");
    options = new GanttOptions(getRoleManager(), getDocumentManager(), isOnlyViewer);
    myUIConfiguration = options.getUIConfiguration();
    myUIConfiguration.setChartFontOption(getUiFacadeImpl().getChartFontOption());
    myUIConfiguration.setDpiOption(getUiFacadeImpl().getDpiOption());

    addProjectEventListener(getTaskManager().getProjectListener());
    getActiveCalendar().addListener(getTaskManager().getCalendarListener());
    ImageIcon icon = new ImageIcon(getClass().getResource("/icons/ganttproject-logo-512.png"));
    setIconImage(icon.getImage());


    //myFacadeInvalidator = new FacadeInvalidator(getTree().getModel(), myRowHeightAligners);
    //getProject().addProjectEventListener(myFacadeInvalidator);
    area = new GanttGraphicArea(this, getTree(), getTaskManager(), getZoomManager(), getUndoManager(),
        myTaskTableChartConnector,
        Suppliers.memoize(() -> myTaskTableSupplier.get().getActionConnector()));
    getTree().init();
    options.addOptionGroups(getUIFacade().getOptions());
    options.addOptionGroups(getUIFacade().getGanttChart().getOptionGroups());
    options.addOptionGroups(getUIFacade().getResourceChart().getOptionGroups());
    options.addOptionGroups(getProjectUIFacade().getOptionGroups());
    options.addOptionGroups(getDocumentManager().getNetworkOptionGroups());
    options.addOptions(GPCloudOptions.INSTANCE.getOptionGroup());
    options.addOptions(getRssFeedChecker().getOptions());
    options.addOptions(UpdateOptions.INSTANCE.getOptionGroup());
    options.addOptions(myTaskManagerConfig.getTaskOptions());
    startupLogger.debug("2. loading options");
    initOptions();

    getTree().setGraphicArea(area);
    getUIFacade().setLookAndFeel(getUIFacade().getLookAndFeel());
    myRowHeightAligners.add(getTree().getRowHeightAligner());
    getUiFacadeImpl().getAppFontOption().addChangeValueListener(new ChangeValueListener() {
      @Override
      public void changeValue(ChangeValueEvent event) {
        for (RowHeightAligner aligner : myRowHeightAligners) {
          aligner.optionsChanged();
        }
      }
    });

    getZoomManager().addZoomListener(area.getZoomListener());

    ScrollingManager scrollingManager = getScrollingManager();
    scrollingManager.addScrollingListener(area.getViewState());
    scrollingManager.addScrollingListener(getResourcePanel().area.getViewState());

    startupLogger.debug("3. creating menus...");
    myResourceActions = getResourcePanel().getResourceActionSet();
    myZoomActions = new ZoomActionSet(getZoomManager());
    JMenuBar bar = new JMenuBar();
    setJMenuBar(bar);
    // Allocation of the menus

    // Project menu related sub menus and items
//    ProjectMRUMenu mruMenu = new ProjectMRUMenu(this, getUIFacade(), getProjectUIFacade(), "lastOpen");
//    mruMenu.setIcon(new ImageIcon(getClass().getResource("/icons/recent_16.gif")));
//    getDocumentManager().addListener(mruMenu);

    myProjectMenu = new ProjectMenu(this, "project");
    bar.add(myProjectMenu);

    myEditMenu = new EditMenu(getProject(), getUIFacade(), getViewManager(), () -> mySearchUi.requestFocus(), "edit");
    bar.add(myEditMenu);
    getTree().getTreeTable().setupActionMaps(myEditMenu.getSearchAction());
    getResourcePanel().getTreeTable().setupActionMaps(myEditMenu.getSearchAction());

    ViewMenu viewMenu = new ViewMenu(getProject(), getViewManager(), getUiFacadeImpl().getDpiOption(), getUiFacadeImpl().getChartFontOption(), "view");
    bar.add(viewMenu);

    {
      JMenu mTask = UIUtil.createTooltiplessJMenu(GPAction.createVoidAction("task"));
      mTask.add(myTaskActions.getCreateAction());
      mTask.add(myTaskActions.getPropertiesAction());
      mTask.add(myTaskActions.getDeleteAction());
      getResourcePanel().setTaskPropertiesAction(myTaskActions.getPropertiesAction());
      bar.add(mTask);
    }
    JMenu mHuman = UIUtil.createTooltiplessJMenu(GPAction.createVoidAction("human"));
    for (AbstractAction a : myResourceActions.getActions()) {
      mHuman.add(a);
    }
    mHuman.add(myResourceActions.getResourceSendMailAction());
    mHuman.add(myResourceActions.getCloudResourceList());
    bar.add(mHuman);

    HelpMenu helpMenu = new HelpMenu(getProject(), getUIFacade(), getProjectUIFacade());
    bar.add(helpMenu.createMenu());

    startupLogger.debug("4. creating views...");

    myGanttChartTabContent = new GanttChartTabContentPanel(
        getProject(), getUIFacade(), area.getJComponent(),
        getUIConfiguration(), myTaskTableSupplier, myTaskActions);

    getViewManager().createView(myGanttChartTabContent, new ImageIcon(getClass().getResource("/icons/tasks_16.gif")));
    getViewManager().toggleVisible(myGanttChartTabContent);

    myResourceChartTabContent = new ResourceChartTabContentPanel(getProject(), getUIFacade(), getResourcePanel(),
        getResourcePanel().area);
    getViewManager().createView(myResourceChartTabContent, new ImageIcon(getClass().getResource("/icons/res_16.gif")));
    getViewManager().toggleVisible(myResourceChartTabContent);

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            getGanttChart().reset();
            getResourceChart().reset();
            // This will clear any modifications which might be caused by
            // adjusting widths of table columns during initial layout process.
            getProject().setModified(false);
          }
        });
      }
    });
    startupLogger.debug("5. calculating size and packing...");

    FXToolbar fxToolbar = createToolbar();
    Platform.runLater(() -> {
      GPCloudStatusBar cloudStatusBar = new GPCloudStatusBar(
          myObservableDocument, getUIFacade(), getProjectUIFacade(), getProject()
      );
      Scene statusBarScene = new Scene(cloudStatusBar.getLockPanel(), javafx.scene.paint.Color.TRANSPARENT);
      statusBarScene.getStylesheets().add("biz/ganttproject/app/StatusBar.css");
      getStatusBar().setLeftScene(statusBarScene);
    });

    createContentPane(fxToolbar.getComponent());
    //final FXToolbar toolbar = fxToolbar;
    //final List<? extends JComponent> buttons = addButtons(getToolBar());
    // Chart tabs
    getTabs().setSelectedIndex(0);

    startupLogger.debug("6. changing language ...");
    languageChanged(null);
    // Add Listener after language update (to be sure that it is not updated
    // twice)
    language.addListener(this);

    startupLogger.debug("7. first attempt to restore bounds");
    restoreBounds();
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowEvent) {
        quitApplication(true);
      }

      @Override
      public void windowOpened(WindowEvent e) {
        boundsLogger.debug("Resizing window...");
        boundsLogger.debug("Bounds after opening: {}", new Object[]{GanttProject.this.getBounds()}, ImmutableMap.of());
        restoreBounds();
        // It is important to run aligners after look and feel is set and font sizes
        // in the UI manager updated.
        SwingUtilities.invokeLater(() -> {
          for (RowHeightAligner aligner : myRowHeightAligners) {
            aligner.optionsChanged();
          }
        });
        getUiFacadeImpl().getDpiOption()
            .addChangeValueListener(event -> SwingUtilities.invokeLater(() -> getContentPane().doLayout()));
        getGanttChart().reset();
        getResourceChart().reset();
        // This will clear any modifications which might be caused by
        // adjusting widths of table columns during initial layout process.
        getProject().setModified(false);
      }
    });

    startupLogger.debug("8. finalizing...");
    // applyComponentOrientation(GanttLanguage.getInstance()
    // .getComponentOrientation());
    getTaskManager().addTaskListener(new TaskModelModificationListener(this, getUIFacade()));
    addMouseListenerToAllContainer(this.getComponents());

    // Add globally available actions/key strokes
    GPAction viewCycleForwardAction = new ViewCycleAction(getViewManager(), true);
    UIUtil.pushAction(getTabs(), true, viewCycleForwardAction.getKeyStroke(), viewCycleForwardAction);

    GPAction viewCycleBackwardAction = new ViewCycleAction(getViewManager(), false);
    UIUtil.pushAction(getTabs(), true, viewCycleBackwardAction.getKeyStroke(), viewCycleBackwardAction);

    try {
      myObservableDocument.set(getDocumentManager().newUntitledDocument());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private void restoreBounds() {
    if (options.isLoaded()) {
      if (options.isMaximized()) {
        setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
      }
      Rectangle bounds = new Rectangle(options.getX(), options.getY(), options.getWidth(), options.getHeight());
      boundsLogger.debug("Bounds stored in the  options: {}", new Object[]{bounds}, ImmutableMap.of());

      UIUtil.MultiscreenFitResult fit = UIUtil.multiscreenFit(bounds);
      // If more than 1/4 of the rectangle is visible on screen devices then leave it where it is
      if (fit.totalVisibleArea < 0.25 || Math.max(bounds.width, bounds.height) < 100) {
        // Otherwise if it is visible on at least one device, try to fit it there
        if (fit.argmaxVisibleArea != null) {
          bounds = fitBounds(fit.argmaxVisibleArea, bounds);
        } else {
          UIUtil.MultiscreenFitResult currentFit = UIUtil.multiscreenFit(this.getBounds());
          if (currentFit.argmaxVisibleArea != null) {
            // If there are no devices where rectangle is visible, fit it on the current device
            bounds = fitBounds(currentFit.argmaxVisibleArea, bounds);
          } else {
            boundsLogger.debug(
                "We have not found the display corresponding to bounds {}. Leaving the window where it is",
                new Object[]{bounds}, ImmutableMap.of()
            );
            return;
          }
        }
      }
      setBounds(bounds);
    }
  }

  static private Rectangle fitBounds(GraphicsConfiguration display, Rectangle bounds) {
    Rectangle displayBounds = display.getBounds();
    Rectangle visibleBounds = bounds.intersection(displayBounds);
    int fitX = visibleBounds.x;
    if (fitX + bounds.width > displayBounds.x + displayBounds.width) {
      fitX = Math.max(displayBounds.x, displayBounds.x + displayBounds.width - bounds.width);
    }
    int fitY = visibleBounds.y;
    if (fitY + bounds.height > displayBounds.y + displayBounds.height) {
      fitY = Math.max(displayBounds.y, displayBounds.y + displayBounds.height - bounds.height);
    }
    return new Rectangle(fitX, fitY, bounds.width, bounds.height);

  }


//  @Override
//  public TaskContainmentHierarchyFacade getTaskContainment() {
//    if (myFacadeInvalidator == null) {
//      return TaskContainmentHierarchyFacade.STUB;
//    }
//    if (!myFacadeInvalidator.isValid() || myCachedFacade == null) {
//      myCachedFacade = new TaskContainmentHierarchyFacadeImpl(tree);
//      myFacadeInvalidator.reset();
//    }
//    return myCachedFacade;
//  }

  private void initOptions() {
    // Color color = GanttGraphicArea.taskDefaultColor;
    // myApplicationConfig.register(options);
    options.setUIConfiguration(myUIConfiguration);
    options.load();
    myUIConfiguration = options.getUIConfiguration();
  }

  private void addMouseListenerToAllContainer(Component[] containers) {
    for (Component container : containers) {
      container.addMouseListener(getStopEditingMouseListener());
      if (container instanceof Container) {
        addMouseListenerToAllContainer(((Container) container).getComponents());
      }
    }
  }

  /**
   * @return A mouseListener that stop the edition in the ganttTreeTable.
   */
  private MouseListener getStopEditingMouseListener() {
    if (myStopEditingMouseListener == null)
      myStopEditingMouseListener = new MouseAdapter() {
        // @Override
        // public void mouseClicked(MouseEvent e) {
        // if (e.getSource() != bNew && e.getClickCount() == 1) {
        // tree.stopEditing();
        // }
        // if (e.getButton() == MouseEvent.BUTTON1
        // && !(e.getSource() instanceof JTable)
        // && !(e.getSource() instanceof AbstractButton)) {
        // Task taskUnderPointer =
        // area.getChartImplementation().findTaskUnderPointer(e.getX(),
        // e.getY());
        // if (taskUnderPointer == null) {
        // getTaskSelectionManager().clear();
        // }
        // }
        // }
      };
    return myStopEditingMouseListener;
  }

  /**
   * @return the options of ganttproject.
   */
  public GanttOptions getGanttOptions() {
    return options;
  }

  /**
   * Function to change language of the project
   */
  @Override
  public void languageChanged(Event event) {
    applyComponentOrientation(language.getComponentOrientation());
    area.repaint();
    getResourcePanel().area.repaint();

    CustomColumnsStorage.changeLanguage(language);

    applyComponentOrientation(language.getComponentOrientation());
  }

  /**
   * @return the ToolTip in HTML (with gray bgcolor)
   */
  public static String getToolTip(String msg) {
    return "<html><body bgcolor=#EAEAEA>" + msg + "</body></html>";
  }

  /**
   * Create the button on toolbar
   */
  private FXToolbar createToolbar() {
    FXToolbarBuilder builder = new FXToolbarBuilder();
    builder.addButton(myProjectMenu.getOpenProjectAction().asToolbarAction())
        .addButton(myProjectMenu.getSaveProjectAction().asToolbarAction())
        .addWhitespace();

    final ArtefactAction newAction;
    {
      final GPAction taskNewAction = myTaskActions.getCreateAction().asToolbarAction();
      final GPAction resourceNewAction = getResourceTree().getNewAction().asToolbarAction();
      newAction = new ArtefactNewAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return getTabs().getSelectedIndex() == UIFacade.GANTT_INDEX ? taskNewAction : resourceNewAction;
        }
      }, new Action[]{taskNewAction, resourceNewAction});
      builder.addButton(taskNewAction).addButton(resourceNewAction);
    }

    final ArtefactAction deleteAction;
    {
      final GPAction taskDeleteAction = myTaskActions.getDeleteAction();
      final GPAction resourceDeleteAction = getResourceTree().getDeleteAction().asToolbarAction();
      deleteAction = new ArtefactDeleteAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return getTabs().getSelectedIndex() == UIFacade.GANTT_INDEX ? taskDeleteAction : resourceDeleteAction;
        }
      }, new Action[]{taskDeleteAction, resourceDeleteAction});
    }
    builder.setArtefactActions(newAction, deleteAction);

    final ArtefactAction propertiesAction;
    {
      final GPAction taskPropertiesAction = myTaskActions.getPropertiesAction().asToolbarAction();
      final GPAction resourcePropertiesAction = getResourceTree().getPropertiesAction().asToolbarAction();
      propertiesAction = new ArtefactPropertiesAction(new ActiveActionProvider() {
        @Override
        public AbstractAction getActiveAction() {
          return getTabs().getSelectedIndex() == UIFacade.GANTT_INDEX ? taskPropertiesAction : resourcePropertiesAction;
        }
      }, new Action[]{taskPropertiesAction, resourcePropertiesAction});
    }

    UIUtil.registerActions(getRootPane(), false, newAction, propertiesAction, deleteAction);
    UIUtil.registerActions(myGanttChartTabContent.getComponent(), true, newAction, propertiesAction, deleteAction);
    UIUtil.registerActions(myResourceChartTabContent.getComponent(), true, newAction, propertiesAction, deleteAction);
    getTabs().addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        // Tell artefact actions that the active provider changed, so they
        // are able to update their state according to the current delegate
        newAction.actionStateChanged();
        propertiesAction.actionStateChanged();
        deleteAction.actionStateChanged();
        getTabs().getSelectedComponent().requestFocus();
      }
    });

    builder.addButton(deleteAction)
        .addWhitespace()
        .addButton(propertiesAction)
        .addButton(getCutAction().asToolbarAction())
        .addButton(getCopyAction().asToolbarAction())
        .addButton(getPasteAction().asToolbarAction())
        .addWhitespace()
        .addButton(myEditMenu.getUndoAction().asToolbarAction())
        .addButton(myEditMenu.getRedoAction().asToolbarAction());
    mySearchUi = new FXSearchUi(getProject(), getUIFacade(), myEditMenu.getSearchAction());
    builder.addSearchBox(mySearchUi);

    //return result;
    return builder.build();
  }

  void doShow() {
    setVisible(true);
    boundsLogger.debug("Bounds after setVisible: {}", new Object[]{getBounds()}, ImmutableMap.of());
    DesktopIntegration.setup(GanttProject.this);
    getActiveChart().reset();
    getRssFeedChecker().setOptionsVersion(getGanttOptions().getVersion());
    getRssFeedChecker().run();

    UpdateKt.checkAvailableUpdates(getUpdater(), getUIFacade());
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  }

  @Override
  public List<GanttPreviousState> getBaselines() {
    return myPreviousStates;
  }

  /**
   * Refresh the information of the project on the status bar.
   */
  public void refreshProjectInformation() {
    if (getTaskManager().getTaskCount() == 0 && resp.nbPeople() == 0) {
      getStatusBar().setSecondText("");
    } else {
      getStatusBar().setSecondText(
          language.getCorrectedLabel("task") + " : " + getTaskManager().getTaskCount() + "  "
              + language.getCorrectedLabel("resources") + " : " + resp.nbPeople());
    }
  }

  /**
   * Print the project
   */
  public void printProject() {
    Chart chart = getUIFacade().getActiveChart();
    if (chart == null) {
      getUIFacade().showErrorDialog(
          "Failed to find active chart.\nPlease report this problem to GanttProject development team");
      return;
    }
    try {
      PrintManager.printChart(chart, options.getExportSettings());
    } catch (OutOfMemoryError e) {
      getUIFacade().showErrorDialog(GanttLanguage.getInstance().getText("printing.out_of_memory"));
    }
  }

  /**
   * Create a new project
   */
  public void newProject() {
    getProjectUIFacade().createProject(getProject());
    try {
      Document newDocument = getDocumentManager().newUntitledDocument();
      getProject().setDocument(newDocument);
      myObservableDocument.set(newDocument);
      fireProjectCreated();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void open(Document document) throws IOException, DocumentException {
    document.read();
    getDocumentManager().addToRecentDocuments(document);
    //myMRU.add(document.getPath(), true);
    myObservableDocument.set(document);
    setTitle(language.getText("appliTitle") + " [" + document.getFileName() + "]");
    for (Chart chart : PluginManager.getCharts()) {
      chart.reset();
    }

    // myDelayManager.fireDelayObservation(); // it is done in repaint2
    addMouseListenerToAllContainer(this.getComponents());

    fireProjectOpened();
  }

  public void openStartupDocument(String path) {
    if (path == null) {
      return;
    }
    var strategy = new CommandLineProjectOpenStrategy(getProject(), getDocumentManager(), (TaskManagerImpl) getTaskManager(), getUIFacade(), getProjectUIFacade(), getGanttOptions().getPluginPreferences());
    strategy.openStartupDocument(path, () -> {
      fireProjectCreated();
      return Unit.INSTANCE;
    });
  }

  /**
   * Save the project as (with a dialog file chooser)
   */
  public boolean saveAsProject() {
    getProjectUIFacade().saveProjectAs(getProject());
    return true;
  }

  /**
   * @return the UIConfiguration.
   */
  @Override
  public UIConfiguration getUIConfiguration() {
    return myUIConfiguration;
  }

  private boolean myQuitEntered = false;

  /**
   * Quit the application
   */
  @Override
  public boolean quitApplication(boolean withSystemExit) {
    if (myQuitEntered) {
      return false;
    }
    myQuitEntered = true;
    try {
      options.setWindowPosition(getX(), getY());
      options.setWindowSize(getWidth(), getHeight(), (getExtendedState() & Frame.MAXIMIZED_BOTH) != 0);
      options.setUIConfiguration(myUIConfiguration);
      options.save();
      if (getProjectUIFacade().ensureProjectSaved(getProject())) {
        getProject().close();
        setVisible(false);
        dispose();
        if (ourQuitCallback != null) {
          ourQuitCallback.accept(withSystemExit);
        }
        return true;
      } else {
        setVisible(true);
        return false;
      }
    } finally {
      myQuitEntered = false;
    }
  }

  public void setAskForSave(boolean afs) {
    if (isOnlyViewer) {
      return;
    }
    fireProjectModified(afs);
    String title = getTitle();
    askForSave = afs;
    try {
      if (System.getProperty("mrj.version") != null) {
        rootPane.putClientProperty("windowModified", Boolean.valueOf(afs));
        // see http://developer.apple.com/qa/qa2001/qa1146.html
      } else {
        if (askForSave) {
          if (!title.endsWith(" *")) {
            setTitle(title + " *");
          }
        }
      }
    } catch (AccessControlException e) {
      // This can happen when running in a sandbox (Java WebStart)
      System.err.println(e + ": " + e.getMessage());
    }
  }

  public GanttResourcePanel getResourcePanel() {
    if (this.resp == null) {
      this.resp = new GanttResourcePanel(this, getUIFacade());
      this.resp.init();
      myRowHeightAligners.add(this.resp.getRowHeightAligner());
      getHumanResourceManager().addView(this.resp);
    }
    return this.resp;
  }

  public GanttGraphicArea getArea() {
    return this.area;
  }

  public GanttTree2 getTree() {
    if (tree == null) {
      tree = new GanttTree2(this, getTaskManager(), getTaskSelectionManager(), getUIFacade(), myTaskActions);
    }
    return tree;
  }

  public GPAction getCopyAction() {
    return getViewManager().getCopyAction();
  }

  public GPAction getCutAction() {
    return getViewManager().getCutAction();
  }

  public GPAction getPasteAction() {
    return getViewManager().getPasteAction();
  }

  GPAction getSearchAction() { return myEditMenu.getSearchAction(); }
  @Override
  public ZoomActionSet getZoomActionSet() {
    return myZoomActions;
  }

  public static class Args {
    @Parameter(names = "-logback-config", description = "Path to logback configuration file", arity = 1)
    public String logbackConfig = "logback.xml";

    @Parameter(names = "-log", description = "Enable logging", arity = 1)
    public boolean log = true;

    @Parameter(names = "-log_file", description = "Log file name")
    public String logFile = "auto";

    @Parameter(names = {"-h", "-help"}, description = "Print usage")
    public boolean help = false;

    @Parameter(names = {"-version"}, description = "Print version number")
    public boolean version = false;

    @Parameter(description = "Input file name")
    public List<String> file = null;
  }

  /**
   * The main
   */
  public static boolean main(String[] arg) throws InvocationTargetException, InterruptedException {
    CommandLineExportApplication cmdlineApplication = new CommandLineExportApplication();
    final Args mainArgs = new Args();
    try {
      JCommander cmdLineParser = new JCommander(new Object[]{mainArgs, cmdlineApplication.getArguments()}, arg);
      GPLogger.init(mainArgs.logbackConfig);
      if (mainArgs.help) {
        cmdLineParser.usage();
        System.exit(0);
      }
      if (mainArgs.version) {
        System.out.println(GPVersion.getCurrentVersionNumber());
        System.exit(0);
      }
    } catch (Throwable e) {
      e.printStackTrace();
      return false;
    }
    if (mainArgs.log && "auto".equals(mainArgs.logFile)) {
      mainArgs.logFile = System.getProperty("user.home") + File.separator + "ganttproject.log";
    }
    if (mainArgs.log && !mainArgs.logFile.trim().isEmpty()) {
      try {
        GPLogger.setLogFile(mainArgs.logFile);
        File logFile = new File(mainArgs.logFile);
        System.setErr(new PrintStream(new FileOutputStream(logFile)));

      } catch (IOException e) {
        System.err.println("Failed to write log to file: " + e.getMessage());
        e.printStackTrace();
      }
    }

    GPLogger.logSystemInformation();
    // Check if an export was requested from the command line
    if (cmdlineApplication.export(mainArgs)) {
      // Export succeeded so exit application
      return false;
    }


    AppKt.startUiApp(mainArgs, ganttProject -> {
      ganttProject.setUpdater(org.eclipse.core.runtime.Platform.getUpdater());
      return null;
    });
    return true;
  }

  void doOpenStartupDocument(Args args) {
    fireProjectCreated();
    if (args.file != null && !args.file.isEmpty()) {
      openStartupDocument(args.file.get(0));
    }
  }

  // ///////////////////////////////////////////////////////
  // IGanttProject implementation
  @Override
  public String getProjectName() {
    return prjInfos.getName();
  }

  @Override
  public void setProjectName(String projectName) {
    prjInfos.setName(projectName);
    setAskForSave(true);
  }

  @Override
  public String getDescription() {
    return prjInfos.getDescription();
  }

  @Override
  public void setDescription(String description) {
    prjInfos.setDescription(description);
    setAskForSave(true);
  }

  @Override
  public String getOrganization() {
    return prjInfos.getOrganization();
  }

  @Override
  public void setOrganization(String organization) {
    prjInfos.setOrganization(organization);
    setAskForSave(true);
  }

  @Override
  public String getWebLink() {
    return prjInfos.getWebLink();
  }

  @Override
  public void setWebLink(String webLink) {
    prjInfos.setWebLink(webLink);
    setAskForSave(true);
  }

  @Override
  public HumanResourceManager getHumanResourceManager() {
    if (myHumanResourceManager == null) {
      myHumanResourceManager = new HumanResourceManager(getRoleManager().getDefaultRole(),
          getResourceCustomPropertyManager());
      myHumanResourceManager.addView(this);
    }
    return myHumanResourceManager;
  }

  @Override
  public RoleManager getRoleManager() {
    if (myRoleManager == null) {
      myRoleManager = RoleManager.Access.getInstance();
    }
    return myRoleManager;
  }

  @Override
  public Document getDocument() {
    return myObservableDocument.get();
  }

  @Override
  public void setDocument(Document document) {
    myObservableDocument.set(document);
  }

  @Override
  public void setModified() {
    setAskForSave(true);
  }

  @Override
  public void setModified(boolean modified) {
    setAskForSave(modified);

    String title = getTitle();
    if (modified == false && title.endsWith(" *")) {
      // Remove * from title
      setTitle(title.substring(0, title.length() - 2));
    }
  }

  @Override
  public boolean isModified() {
    return askForSave;
  }

  @Override
  public void close() {
    fireProjectClosed();
    prjInfos = new PrjInfos();
    RoleManager.Access.getInstance().clear();
    myObservableDocument.set(null);
    getTaskCustomColumnManager().reset();
    getResourceCustomPropertyManager().reset();

    for (int i = 0; i < myPreviousStates.size(); i++) {
      myPreviousStates.get(i).remove();
    }
    myPreviousStates = new ArrayList<GanttPreviousState>();
    myCalendar.reset();
    //myFacadeInvalidator.projectClosed();
  }

  @Override
  protected ParserFactory getParserFactory() {
    if (myParserFactory == null) {
      myParserFactory = new ParserFactoryImpl();
    }
    return myParserFactory;
  }

  // ///////////////////////////////////////////////////////////////
  // ResourceView implementation
  @Override
  public void resourceAdded(ResourceEvent event) {
    if (getStatusBar() != null) {
      // tabpane.setSelectedIndex(1);
      String description = language.getCorrectedLabel("resource.new.description");
      if (description == null) {
        description = language.getCorrectedLabel("resource.new");
      }
      getUIFacade().setStatusText(description);
      setAskForSave(true);
      refreshProjectInformation();
    }
  }

  @Override
  public void resourcesRemoved(ResourceEvent event) {
    refreshProjectInformation();
    setAskForSave(true);
  }

  @Override
  public void resourceChanged(ResourceEvent e) {
    setAskForSave(true);
  }

  @Override
  public void resourceAssignmentsChanged(ResourceEvent e) {
    setAskForSave(true);
  }

  // ///////////////////////////////////////////////////////////////
  // UIFacade

  @Override
  public GanttChart getGanttChart() {
    return getArea();
  }

  @Override
  public TimelineChart getResourceChart() {
    return getResourcePanel().area;
  }

  @Override
  public int getGanttDividerLocation() {
    return myGanttChartTabContent.getDividerLocation();
  }

  @Override
  public void setGanttDividerLocation(int location) {
    myGanttChartTabContent.setDividerLocation(location);
  }

  @Override
  public int getResourceDividerLocation() {
    return myResourceChartTabContent.getDividerLocation();
  }

  @Override
  public void setResourceDividerLocation(int location) {
    myResourceChartTabContent.setDividerLocation(location);
  }

  @Override
  public TaskTreeUIFacade getTaskTree() {
    return getTree();
  }

  @Override
  public ResourceTreeUIFacade getResourceTree() {
    return getResourcePanel();
  }

  private class ParserFactoryImpl implements ParserFactory {
    @Override
    public GPParser newParser() {
      return new GanttXMLOpen(prjInfos, getUIConfiguration(), getTaskManager(), getUIFacade());
    }

    @Override
    public GPSaver newSaver() {
      return new GanttXMLSaver(GanttProject.this, getArea(), getUIFacade(), () -> myTaskTableSupplier.get().getColumnList());
    }
  }

  @Override
  public int getViewIndex() {
    if (getTabs() == null) {
      return -1;
    }
    return getTabs().getSelectedIndex();
  }

  @Override
  public void setViewIndex(int viewIndex) {
    if (getTabs().getTabCount() > viewIndex) {
      getTabs().setSelectedIndex(viewIndex);
    }
  }

  public static void setApplicationQuitCallback(Consumer<Boolean> callback) {
    ourQuitCallback = callback;
  }

  @Override
  public void refresh() {
    getTaskManager().processCriticalPath(getTaskManager().getRootTask());
    getResourcePanel().getResourceTreeTableModel().updateResources();
    getResourcePanel().getResourceTreeTable().setRowHeight(getResourceChart().getModel().calculateRowHeight());
    for (Chart chart : PluginManager.getCharts()) {
      chart.reset();
    }
    super.repaint();
  }
}
