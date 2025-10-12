package com.litclub.ui.component.content.subcomponents;

import com.litclub.theme.ThemeManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.Objects;

public class MiniLogo extends ImageView {

    public MiniLogo() {
        String imagePath = ThemeManager.getInstance().isBrightMode()
                ? "/com/litclub/ui/icons/LitClub_logo_blackText_mini.png"
                : "/com/litclub/ui/icons/LitClub_logo_whiteText_mini.png";

        Image image = new Image(
                Objects.requireNonNull(getClass().getResourceAsStream(imagePath)
                , "Mini logo not found at" + imagePath)
        );

        this.setImage(image);
        this.setPreserveRatio(true);
        this.setFitWidth(35);
        this.getStyleClass().add("page-logo");

        ThemeManager.getInstance().brightModeProperty().addListener(
                (_, _, newValue) -> {
                    String newPath = newValue
                            ? "/com/litclub/ui/icons/LitClub_logo_blackText_mini.png"
                            : "/com/litclub/ui/icons/LitClub_logo_whiteText_mini.png";

                    Image newImage = new Image(
                            Objects.requireNonNull(getClass().getResourceAsStream(newPath)
                                    , "Mini logo not found at" + newPath)
                    );

                    this.setImage(newImage);
                }
        );
    }
}
