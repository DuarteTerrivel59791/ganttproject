// Copyright (C) 2016 BarD Software
package biz.ganttproject.storage.cloud;

import biz.ganttproject.storage.StorageDialogBuilder;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import net.sourceforge.ganttproject.document.DocumentStorageUi;
import net.sourceforge.ganttproject.document.webdav.WebDavServerDescriptor;
import org.controlsfx.control.HyperlinkLabel;

import java.util.Optional;

/**
 * @author dbarashev@bardsoftware.com
 */
public class GPCloudStorage implements StorageDialogBuilder.Ui {
  private final GPCloudStorageOptions myOptions;
  private final BorderPane myPane;
  private DocumentStorageUi.DocumentReceiver myDocumentReceiver;
  private StorageDialogBuilder.DialogUi myDialogUi;

  public GPCloudStorage(GPCloudStorageOptions options) {
    myOptions = options;
    myPane = new BorderPane();
  }

  static Label newLabel(String key, String... classes) {
    Label label = new Label(key);
    label.getStyleClass().addAll(classes);
    //label.setPrefWidth(400);
    return label;
  }

  static HyperlinkLabel newHyperlink(EventHandler<ActionEvent> eventHandler, String text, String... classes) {
    HyperlinkLabel result = new HyperlinkLabel(text);
    result.addEventHandler(ActionEvent.ACTION, eventHandler);
    result.getStyleClass().addAll(classes);
    return result;
  }

  static Pane centered(Node... nodes) {
    VBox centered = new VBox();
    centered.setMaxWidth(Double.MAX_VALUE);
    centered.getStyleClass().add("center");
    centered.getChildren().addAll(nodes);
    return centered;
  }

  private Pane createSetupCloudPane() {
    VBox cloudSetupPane = new VBox();
    cloudSetupPane.setPrefWidth(400);
    cloudSetupPane.getStyleClass().add("pane-service-contents");


    Label signupSubtitle = newLabel("Not yet signed up?", "subtitle");
    Label signupHelp = newLabel(
        "Creating an account on GanttProject Cloud is free and easy. No credit card required. Get up and running instantly.",
        "help");
    cloudSetupPane.getChildren().addAll(signupSubtitle, signupHelp);


    Button signupButton = new Button("Sign Up");
    signupButton.getStyleClass().addAll("btn-signup");
    cloudSetupPane.getChildren().add(centered(signupButton));
    return cloudSetupPane;
  }

  private Pane createConnectCloudPane() {
    VBox cloudConnectPane = new VBox();
    cloudConnectPane.setPrefWidth(400);
    cloudConnectPane.getStyleClass().add("pane-service-contents");
    Label title = newLabel("Sign in to GanttProject Cloud", "title");
    Label titleHelp = newLabel(
        "You seem to be registered on GanttProject Cloud but you don't store your password on disk. You need to request a new PIN and type it into the field below",
        "title-help");
    cloudConnectPane.getChildren().addAll(title, titleHelp);
    return cloudConnectPane;
  }


  @Override
  public String getId() {
    return "cloud";
  }

  @Override
  public Pane createUi(DocumentStorageUi.DocumentReceiver documentReceiver, StorageDialogBuilder.DialogUi dialogUi) {
    myDocumentReceiver = documentReceiver;
    myDialogUi = dialogUi;
    return doCreateUi();
  }

  private Pane doCreateUi() {
    GPCloudLoginPane loginPane = new GPCloudLoginPane(myOptions, myDialogUi);
    GPCloudSignupPane signupPane = new GPCloudSignupPane();
    GPCloudStartPane startPane = new GPCloudStartPane(myDialogUi, this::replaceUi, loginPane, signupPane);
    Optional<WebDavServerDescriptor> cloudServer = myOptions.getCloudServer();
    if (cloudServer.isPresent()) {
      WebDavServerDescriptor wevdavServer = cloudServer.get();
      if (wevdavServer.getPassword() == null) {
        myPane.setCenter(loginPane.createPane());
      } else {
        WebdavStorage webdavStorage = new WebdavStorage(wevdavServer);
        myPane.setCenter(webdavStorage.createUi(myDocumentReceiver, myDialogUi));
      }
    } else {
      myPane.setCenter(startPane.createPane());
    }
    return myPane;
  }

  private void replaceUi(Pane newUi) {
    FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), myPane);
    fadeIn.setFromValue(0.0);
    fadeIn.setToValue(1.0);

    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), myPane);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.1);
    fadeOut.play();
    fadeOut.setOnFinished(e ->  {
      myPane.setCenter(newUi);
      myDialogUi.resize();
      fadeIn.play();
    });

  }
}