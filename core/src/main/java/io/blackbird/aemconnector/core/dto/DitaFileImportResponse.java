package io.blackbird.aemconnector.core.dto;

import java.io.Serializable;

public class DitaFileImportResponse implements Serializable {
    String message;
    String path;

    public DitaFileImportResponse(String message, String path) {
        this.message = message;
        this.path = path;
    }

    @Override
    public String toString() {
        return "<response>"
                + "<message>" + message + "</message>"
                + "<path>" + path + "</path>"
                + "</response>";
    }
}
