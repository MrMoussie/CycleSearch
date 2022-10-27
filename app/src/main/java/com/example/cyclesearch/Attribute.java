package com.example.cyclesearch;


import androidx.annotation.NonNull;

/**
 * Enum representing the attributes in Weka classifier.
 */
public enum Attribute {
    RIGHT_POCKET_AX("pocketAx"),
    RIGHT_POCKET_AY("pocketAy"),
    RIGHT_POCKET_AZ("pocketAz"),
    RIGHT_POCKET_GX("pocketGx"),
    RIGHT_POCKET_GY("pocketGy"),
    RIGHT_POCKET_GZ("pocketGz"),
    WALKING("walking"),
    STANDING("standing"),
    SITTING("sitting"),
    BIKING("biking");

    private final String attribute;

    Attribute(String input) {
        this.attribute = input;
    }

    @NonNull
    @Override
    public String toString() {
        return attribute;
    }
}