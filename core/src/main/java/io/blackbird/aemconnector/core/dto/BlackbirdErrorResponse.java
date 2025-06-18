package io.blackbird.aemconnector.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.Date;

@Value
@Builder
@JsonDeserialize(builder = BlackbirdErrorResponse.BlackbirdErrorResponseBuilder.class)
public class BlackbirdErrorResponse implements Serializable {
    int status;
    String error;
    String message;
    String path;

    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    Date timestamp = new Date();

    @JsonPOJOBuilder(withPrefix = "")
    public static class BlackbirdErrorResponseBuilder {
    }
}
