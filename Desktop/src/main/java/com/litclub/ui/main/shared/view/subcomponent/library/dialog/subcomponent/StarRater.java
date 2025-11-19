package com.litclub.ui.main.shared.view.subcomponent.library.dialog.subcomponent;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.util.Objects;

/**
 * StarRater: A simple JavaFX control for rating out of 10 using 5 stars.
 * Each star represents 0.5 increments.
 */
public class StarRater extends HBox {

    private final IntegerProperty rating = new SimpleIntegerProperty(0); // 0–10 inclusive

    private final Image fullStar;
    private final Image halfStar;
    private final Image emptyStar;

    private static final String STARPATH = "/com/litclub/ui/icons/stars/";

    private final ImageView[] stars = new ImageView[5];

    public StarRater() {
        setSpacing(4);
        setAlignment(Pos.CENTER_LEFT);

        fullStar = loadImage("full-black-star.png");
        halfStar = loadImage("half-black-star.png");
        emptyStar = loadImage("no-star.png");

        initializeStars();
        updateStars();
    }

    /* --------------------------
     *  Initialization Helpers
     * -------------------------- */

    private void initializeStars() {
        for (int i = 0; i < stars.length; i++) {
            ImageView starView = createStarView(i);
            stars[i] = starView;
            getChildren().add(starView);
        }
    }

    private ImageView createStarView(int index) {
        ImageView star = new ImageView(emptyStar);
        star.setFitWidth(24);
        star.setFitHeight(24);

        star.addEventHandler(MouseEvent.MOUSE_MOVED, e -> previewRating(index, e));
        star.addEventHandler(MouseEvent.MOUSE_EXITED, e -> updateStars());
        star.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> applyRating(index, e));

        return star;
    }

    private Image loadImage(String name) {
        return new Image(Objects.requireNonNull(getClass().getResource(STARPATH + name)).toExternalForm());
    }

    /* --------------------------
     *  Rating Logic
     * -------------------------- */

    private void previewRating(int index, MouseEvent e) {
        drawStars(computeStarValue(index, e));
    }

    private void applyRating(int index, MouseEvent e) {
        rating.set(computeStarValue(index, e));
        updateStars();
    }

    private int computeStarValue(int index, MouseEvent e) {
        double halfWidth = ((ImageView) e.getSource()).getFitWidth() / 2.0;
        boolean isHalf = e.getX() < halfWidth;
        return index * 2 + (isHalf ? 1 : 2);
    }

    private void updateStars() {
        drawStars(rating.get());
    }

    private void drawStars(int value) {
        for (int i = 0; i < stars.length; i++) {
            int remaining = value - (i * 2);

            if (remaining >= 2) {
                stars[i].setImage(fullStar);
            } else if (remaining == 1) {
                stars[i].setImage(halfStar);
            } else {
                stars[i].setImage(emptyStar);
            }
        }
    }

    /* --------------------------
     *  Properties
     * -------------------------- */

    public int getRating() {
        return rating.get();
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public void setRating(int value) {
        value = Math.max(0, Math.min(10, value));
        rating.set(value);
        updateStars();
    }
}

