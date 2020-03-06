package bean;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class Percent implements Serializable {
    private String name;
    private int value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }


    public Percent(String name, int  value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" + "\""+name+"\":" + value + "}";
    }
}
