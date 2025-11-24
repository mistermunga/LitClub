package com.litclub.ui.main.shared.view.subcomponent.clubactions.subcomponents;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Scheduler extends HBox {

    private final DatePicker datePicker;
    private final Spinner<Integer> hourSpinner;
    private final Spinner<Integer> minuteSpinner;
    private final ObjectProperty<LocalDateTime> dateTimeValue;

    public Scheduler() {
        this(LocalDateTime.now().plusMinutes(30));
    }

    public Scheduler(LocalDateTime initialValue) {
        // Ensure initial value is in the future
        LocalDateTime futureValue = (initialValue == null || initialValue.isBefore(LocalDateTime.now()))
                ? LocalDateTime.now().plusHours(1)
                : initialValue;

        dateTimeValue = new SimpleObjectProperty<>(futureValue);

        // Initialize date picker with restriction to future dates only
        datePicker = new DatePicker();
        datePicker.setValue(futureValue.toLocalDate());
        datePicker.setMaxWidth(150);

        // Disable past dates
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        // Initialize hour spinner (0-23)
        hourSpinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory hourFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23,
                        futureValue.getHour());
        hourFactory.setWrapAround(true);
        hourSpinner.setValueFactory(hourFactory);
        hourSpinner.setMaxWidth(80);
        hourSpinner.setEditable(true);

        // Validate time when hour changes
        hourSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateFutureTime());

        // Initialize minute spinner (0-59)
        minuteSpinner = new Spinner<>();
        SpinnerValueFactory.IntegerSpinnerValueFactory minuteFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59,
                        futureValue.getMinute());
        minuteFactory.setWrapAround(true);
        minuteSpinner.setValueFactory(minuteFactory);
        minuteSpinner.setMaxWidth(80);
        minuteSpinner.setEditable(true);

        // Validate time when minute changes
        minuteSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateFutureTime());

        // Add change listeners to update the dateTimeValue property
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateFutureTime();
            updateDateTime();
        });

        // Layout
        Label timeLabel = new Label("at");
        Label colonLabel = new Label(":");

        this.setSpacing(8);
        this.setAlignment(Pos.CENTER_LEFT);
        this.getChildren().addAll(
                datePicker,
                timeLabel,
                hourSpinner,
                colonLabel,
                minuteSpinner
        );

        // Set initial value
        updateDateTime();
    }

    private void validateFutureTime() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null && selectedDate.equals(LocalDate.now())) {
            // If today is selected, ensure time is in the future
            LocalTime now = LocalTime.now();
            Integer currentHour = hourSpinner.getValue();
            Integer currentMinute = minuteSpinner.getValue();

            if (currentHour != null && currentMinute != null) {
                LocalTime selectedTime = LocalTime.of(currentHour, currentMinute);
                if (selectedTime.isBefore(now) || selectedTime.equals(now)) {
                    // Adjust to next valid time (current time + 1 minute)
                    LocalTime futureTime = now.plusMinutes(1);
                    hourSpinner.getValueFactory().setValue(futureTime.getHour());
                    minuteSpinner.getValueFactory().setValue(futureTime.getMinute());
                }
            }
        }
    }

    private void updateDateTime() {
        LocalDate date = datePicker.getValue();
        Integer hour = hourSpinner.getValue();
        Integer minute = minuteSpinner.getValue();

        if (date != null && hour != null && minute != null) {
            dateTimeValue.set(LocalDateTime.of(date, LocalTime.of(hour, minute)));
        }
    }

    // Main getter for the LocalDateTime value
    public LocalDateTime getValue() {
        return dateTimeValue.get();
    }

    // Setter for programmatic updates
    public void setValue(LocalDateTime dateTime) {
        if (dateTime != null) {
            datePicker.setValue(dateTime.toLocalDate());
            hourSpinner.getValueFactory().setValue(dateTime.getHour());
            minuteSpinner.getValueFactory().setValue(dateTime.getMinute());
            dateTimeValue.set(dateTime);
        }
    }

    // Property for binding
    public ObjectProperty<LocalDateTime> valueProperty() {
        return dateTimeValue;
    }

    // Validation Methods

    /**
     * Checks if a valid future date and time have been selected
     */
    public boolean isValid() {
        return datePicker.getValue() != null
                && hourSpinner.getValue() != null
                && minuteSpinner.getValue() != null
                && isFuture();
    }

    /**
     * Checks if the selected date-time is in the future
     */
    public boolean isFuture() {
        LocalDateTime value = getValue();
        return value != null && value.isAfter(LocalDateTime.now());
    }

    /**
     * Checks if the selected date-time is after a given date-time
     */
    public boolean isAfter(LocalDateTime other) {
        LocalDateTime value = getValue();
        return value != null && other != null && value.isAfter(other);
    }

    /**
     * Checks if the selected date-time is before a given date-time
     */
    public boolean isBefore(LocalDateTime other) {
        LocalDateTime value = getValue();
        return value != null && other != null && value.isBefore(other);
    }

    /**
     * Checks if the selected date-time is between two date-times (inclusive)
     */
    public boolean isBetween(LocalDateTime start, LocalDateTime end) {
        LocalDateTime value = getValue();
        return value != null && start != null && end != null
                && !value.isBefore(start) && !value.isAfter(end);
    }

    /**
     * Validates that a future date is selected
     */
    public boolean hasValue() {
        return getValue() != null && isFuture();
    }

    /**
     * Sets the minimum selectable future date (must be today or later)
     */
    public void setMinDate(LocalDate minDate) {
        LocalDate effectiveMinDate = minDate.isBefore(LocalDate.now()) ? LocalDate.now() : minDate;
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(effectiveMinDate));
            }
        });
    }

    /**
     * Sets the maximum selectable date
     */
    public void setMaxDate(LocalDate maxDate) {
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(maxDate));
            }
        });
    }

    /**
     * Clears the selection and resets to 1 hour from now
     */
    public void clear() {
        LocalDateTime futureDefault = LocalDateTime.now().plusHours(1);
        datePicker.setValue(futureDefault.toLocalDate());
        hourSpinner.getValueFactory().setValue(futureDefault.getHour());
        minuteSpinner.getValueFactory().setValue(futureDefault.getMinute());
        updateDateTime();
    }

    // Component accessors for advanced customization
    public DatePicker getDatePicker() {
        return datePicker;
    }

    public Spinner<Integer> getHourSpinner() {
        return hourSpinner;
    }

    public Spinner<Integer> getMinuteSpinner() {
        return minuteSpinner;
    }
}