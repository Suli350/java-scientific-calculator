package io.github.suli350.calculator;

public enum AngleMode {
    DEGREES, RADIANS;

    double toRadians(double angle) {
        return this == DEGREES ? Math.toRadians(angle) : angle;
    }

    double fromRadians(double angle) {
        return this == DEGREES ? Math.toDegrees(angle) : angle;
    }
}
