package com.project.resumeTracker.helper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate> {

    public CustomLocalDateDeserializer() {
        this(null);
    }

    public CustomLocalDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();
        if (date.equalsIgnoreCase("Present")) {
            return null; // For current jobs, endDate is null
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            // Log error or handle as needed, returning null for now
            return null;
        }
    }
}
