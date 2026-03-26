package org.intentcloudsim.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

/**
 * Main JavaFX Application for Intent-Driven Cloud Simulation
 * 
 * This UI provides:
 * - Real-time simulation execution
 * - Natural language intent input
 * - Live visualization of cloud infrastructure
 * - Metrics and performance tracking
 * - SLA negotiation display
 * - Cost-performance trade-off analysis
 */
public class SimulationUI extends Application {

    private Stage primaryStage;
    private Scene mainScene;
    private TabPane tabPane;

    private SimulationControlPanel controlPanel;
    private InfrastructureVisualizationPanel infraPanel;
    private MetricsPanel metricsPanel;
    private IntentParsingPanel intentPanel;
    private TradeoffAnalysisPanel tradeoffPanel;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Intent-Driven Cloud Computing Simulation");
        
        // Get screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Create main GUI
        mainScene = createMainScene();
        primaryStage.setScene(mainScene);
        
        // Set window size to 90% of screen
        primaryStage.setWidth(screenWidth * 0.95);
        primaryStage.setHeight(screenHeight * 0.95);
        primaryStage.centerOnScreen();

        primaryStage.show();
    }

    /**
     * Create the main scene with all panels.
     */
    private Scene createMainScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-font-size: 12; -fx-font-family: 'Segoe UI';");

        // Top: Header
        root.setTop(createHeader());

        // Center: Tab pane with different views
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-font-size: 11;");

        // Create panels
        controlPanel = new SimulationControlPanel();
        intentPanel = new IntentParsingPanel();
        infraPanel = new InfrastructureVisualizationPanel();
        metricsPanel = new MetricsPanel();
        tradeoffPanel = new TradeoffAnalysisPanel();

        // Add tabs
        Tab controlTab = new Tab("Simulation Control", controlPanel);
        Tab intentTab = new Tab("Intent Parser", intentPanel);
        Tab infraTab = new Tab("Infrastructure", infraPanel);
        Tab metricsTab = new Tab("Metrics & Results", metricsPanel);
        Tab tradeoffTab = new Tab("Trade-off Analysis", tradeoffPanel);

        tabPane.getTabs().addAll(controlTab, intentTab, infraTab, metricsTab, tradeoffTab);

        root.setCenter(tabPane);

        // Bottom: Status bar
        root.setBottom(createStatusBar());

        return new Scene(root);
    }

    /**
     * Create header with title and version info.
     */
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 1 0; -fx-padding: 10;");

        Label title = new Label("Intent-Driven Autonomous Cloud Virtualization");
        title.setStyle("-fx-font-size: 18; -fx-font-weight: bold;");

        HBox infoBox = new HBox(20);
        infoBox.setPadding(new Insets(5, 0, 0, 0));

        Label version = new Label("Version: 1.0 | CloudSim Plus + JavaFX");
        version.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");

        Label patents = new Label("5 Patents: NLP Intent Parser | SLA Negotiation | Trade-off Engine | Intent-Aware Placement | History Learning");
        patents.setStyle("-fx-text-fill: #0066cc; -fx-font-size: 10;");

        infoBox.getChildren().addAll(version, patents);
        header.getChildren().addAll(title, infoBox);

        return header;
    }

    /**
     * Create status bar at bottom.
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(8));
        statusBar.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0; -fx-background-color: #f0f0f0;");

        Label simulationStatus = new Label("Status: Ready");
        simulationStatus.setStyle("-fx-font-size: 10;");

        Separator sep1 = new Separator();
        sep1.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label simulationTime = new Label("Simulation Time: 0s");
        simulationTime.setStyle("-fx-font-size: 10;");

        Separator sep2 = new Separator();
        sep2.setOrientation(javafx.geometry.Orientation.VERTICAL);

        Label resourceUsage = new Label("Resources: 0% CPU, 0% Memory");
        resourceUsage.setStyle("-fx-font-size: 10;");

        HBox.setHgrow(simulationStatus, Priority.ALWAYS);
        statusBar.getChildren().addAll(
            simulationStatus, sep1, simulationTime, sep2, resourceUsage
        );

        return statusBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
