package bean;

import java.io.Serializable;

public class State implements Serializable {
    private Desired desired;
    private Percent percent;

    public State(Desired desired) {
        this.desired = desired;
    }

    public State(Percent percent) {
        this.percent = percent;
    }

    public Percent getPercent() {
        return percent;
    }

    public void setPercent(Percent percent) {
        this.percent = percent;
    }

    public String toDesiredString() {
        if (desired != null) {
            return "{\"state\":{\"desired\":" + desired.toString() + "}}";
        } else {
            return "";
        }
    }

    public String toPercentString() {
        if (percent != null) {
            return "{\"state\":{\"desired\":" + percent.toString() + "}}";
        } else {
            return "";
        }
    }

    public Desired getDesired() {
        return desired;
    }

    public void setDesired(Desired desired) {
        this.desired = desired;
    }
}
