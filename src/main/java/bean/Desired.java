package bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.Serializable;

public class Desired implements Serializable {
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Desired(String name,String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" +
                "\""+name+"\":\"" + value + "\"" +
                '}';
    }
}
