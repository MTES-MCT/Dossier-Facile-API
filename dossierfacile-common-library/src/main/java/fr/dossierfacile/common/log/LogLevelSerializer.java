package fr.dossierfacile.common.log;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class LogLevelSerializer  extends JsonSerializer<Level> {

    @Override
    public void serialize(Level value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.levelStr);
    }
}
