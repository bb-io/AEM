package io.blackbird.aemconnector.core.dto;

import lombok.Setter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Setter
@XmlRootElement(name = "error")
public class ErrorMessageXml implements Serializable {

    private String message;

    public ErrorMessageXml() {}

    public ErrorMessageXml(String message) {
        this.message = message;
    }

    @XmlElement
    public String getMessage() {
        return message;
    }

}
