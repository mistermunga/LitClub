package com.litclub.ui;

import com.litclub.SceneManager;
import com.litclub.client.api.ApiClient;
import com.litclub.session.AppSession;
import com.litclub.theme.ThemeManager;
import com.litclub.theme.ThemeToggleBar;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.*;
import java.util.Objects;
import java.util.Scanner;

public class LandingPage extends VBox {

    private final Label statusLabel;

    public LandingPage() {

        ThemeManager.getInstance().registerComponent(this);

        this.getStyleClass().add("page-root");
        this.setAlignment(Pos.CENTER);
        this.setSpacing(40);
        this.setPadding(new Insets(60, 40, 60, 40));

        // Components
        ImageView logo = createLogoHeader();
        VBox instanceSelector = createInstanceSelector();
        statusLabel = new Label();
        // Main layout
        this.getChildren().addAll(logo, instanceSelector, statusLabel);

    }

    private ImageView createLogoHeader() {
        String imagePath = ThemeManager.getInstance().isBrightMode()
                ? "/com/litclub/ui/icons/LitClub_logo_blackText.png"
                : "/com/litclub/ui/icons/LitClub_logo_whiteText.png";

        Image image = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream(imagePath),
                "Logo image not found at: " + imagePath
        ));

        ImageView logo = new ImageView(image);
        logo.setPreserveRatio(true);
        logo.setFitWidth(350);
        logo.getStyleClass().add("page-logo");

        // Add listener to update logo when theme changes
        ThemeManager.getInstance().brightModeProperty().addListener(
                (observable, oldValue, newValue) -> {
                    String newImagePath = newValue
                            ? "/com/litclub/ui/icons/LitClub_logo_blackText.png"
                            : "/com/litclub/ui/icons/LitClub_logo_whiteText.png";

                    Image newImage = new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream(newImagePath),
                            "Logo image not found at: " + newImagePath
                    ));

                    logo.setImage(newImage);
                }
        );

        return logo;
    }

    private VBox createInstanceSelector() {
        VBox container = new VBox();
        container.setAlignment(Pos.CENTER);
        container.setSpacing(20);
        container.getStyleClass().add("container");

        // Label
        Label urlLabel = new Label("Enter Instance URL");
        urlLabel.getStyleClass().add("label");

        // Input + Go Button Row
        HBox inputRow = new HBox();
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setSpacing(10);

        TextField instanceURL = new TextField("http://localhost:8080");
        instanceURL.setPromptText("https://example.litclub.com");
        instanceURL.getStyleClass().add("text-input");
        HBox.setHgrow(instanceURL, Priority.ALWAYS);
        instanceURL.setOnAction(e -> resolvePing(instanceURL.getText()));

        Button goButton = new Button("Go");
        goButton.getStyleClass().add("button-primary");
        goButton.setDefaultButton(true);
        goButton.setOnAction(e -> resolvePing(instanceURL.getText()));

        inputRow.getChildren().addAll(instanceURL, goButton);

        VBox toggleContainer = new ThemeToggleBar();

        container.getChildren().addAll(urlLabel, inputRow, toggleContainer);

        return container;
    }

    private URL getInstanceURL(String link) throws MalformedURLException {
        try {
            String normalized = link.trim().replaceAll("/+$", "");

            URI uri = new URI(normalized);

            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new MalformedURLException("Invalid or missing scheme (must be http or https): " + link);
            }

            return uri.toURL();
        } catch (URISyntaxException e) {
            throw new MalformedURLException("Invalid URL syntax: " + link);
        }
    }

    private boolean pingServer(URL baseUrl) {
        try {

            URI baseUri = baseUrl.toURI();
            URI pingUri = baseUri.resolve("/api/ping");
            URL pingUrl = pingUri.toURL();

            HttpURLConnection connection = (HttpURLConnection) pingUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                return false;
            }

            try (InputStream inputStream = connection.getInputStream();
                 Scanner scanner = new Scanner(inputStream).useDelimiter("\\A")) {
                String response = scanner.hasNext() ? scanner.next() : "";
                if (response.contains("\"serverType\":\"LitClub\"")) {
                    AppSession.getInstance().setINSTANCE_URL(pingUrl);
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Ping failed: " + e.getMessage());
            return false;
        }
    }

    private void showAccessDeniedError() {
        statusLabel.setText("Failed ping. Confirm URL is correct.");
        statusLabel.setVisible(true);
        statusLabel.getStyleClass().add("error-label");
    }

    private void resolvePing(String inputText) {
        try {
            if (!pingServer(getInstanceURL(inputText))) throw new Exception("Failed Ping");
            ApiClient.initialize(inputText);
            SceneManager.getInstance().showLogin();
        } catch (Exception e) {
            showAccessDeniedError();
        }
    }
}
