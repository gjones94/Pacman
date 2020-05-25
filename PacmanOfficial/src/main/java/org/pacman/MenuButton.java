package org.pacman;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;

public class MenuButton extends Button {

    private final String identifier;

    private final javafx.event.EventHandler<MouseEvent> onMouseHover = event -> MenuButton.super.setFocused(true);

    private final javafx.event.EventHandler<MouseEvent> offMouseHover = event -> MenuButton.super.setFocused(false);

    private final ChangeListener<Boolean> onFocus = (observableValue, aBoolean, t1) -> {
        if (t1) {
            MenuButton.super.setId("BTN_HOVERED");
        } else {
            MenuButton.super.setId("BTN_NORMAL");
        }
    };

    public MenuButton(String name, Font font, double width, double height){ //add EventHandler click
        super(name);
        super.setPrefWidth(width);
        super.setPrefHeight(height);
        super.setId("BTN_NORMAL");
        super.setFont(font);
        this.identifier = name;
        initButtonEffects();
    }

    private void initButtonEffects() {
        this.setOnMouseEntered(onMouseHover);
        this.setOnMouseExited(offMouseHover);
        this.focusedProperty().addListener(onFocus);
    }

    public String getIdentifier() {
        return this.identifier;
    }
}
