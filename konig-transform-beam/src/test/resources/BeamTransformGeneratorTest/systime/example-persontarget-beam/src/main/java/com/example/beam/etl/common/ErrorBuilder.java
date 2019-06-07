package com.example.beam.etl.common;

public class ErrorBuilder {
    private StringBuilder buffer;

    public boolean isEmpty() {
        return (buffer.length() == 0);
    }

    public void addError(String text) {
        if (!isEmpty()) {
            buffer.append("; ");
        }
        buffer.append(text);
    }

    public String toString() {
        return buffer.toString();
    }
}
