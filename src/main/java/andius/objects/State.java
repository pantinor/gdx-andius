package andius.objects;

import andius.Constants;
import andius.Constants.Status;
import com.badlogic.gdx.graphics.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class State {

    private final java.util.Map<Constants.Status, AtomicInteger> states = new HashMap<>();

    public boolean has(Constants.Status s) {
        if (!this.states.containsKey(s)) {
            return false;
        }
        return this.states.get(s).get() > 0;
    }

    public void set(Constants.Status s, int v) {
        if (this.states.containsKey(s)) {
            this.states.get(s).set(v);
        } else {
            this.states.put(s, new AtomicInteger(v));
        }
    }

    public void reset() {
        for (Status s : Status.values()) {
            set(s, 0);
        }
    }

    public void decrement(Constants.Status s) {
        if (this.states.containsKey(s)) {
            if (this.states.get(s).decrementAndGet() < 0) {
                this.states.get(s).set(0);
            }
        }
    }

    public void increment(Constants.Status s) {
        if (this.states.containsKey(s)) {
            this.states.get(s).incrementAndGet();
        }
    }

    public boolean isDisabled() {
        boolean disabled = false;

        if (has(Status.ASHES)) {
            disabled = true;
        }

        if (has(Status.AFRAID)) {
            disabled = true;
        }

        if (has(Status.STONED)) {
            disabled = true;
        }

        if (has(Status.PARALYZED)) {
            disabled = true;
        }

        if (has(Status.ASLEEP)) {
            disabled = true;
        }

        return disabled;
    }

    public Color color() {
        for (Status s : Status.values()) {
            if (has(s)) {
                return s.getColor();
            }
        }
        return Color.WHITE;
    }

    private transient final java.util.List<String> displays = new ArrayList<>();

    @Override
    public String toString() {
        displays.clear();
        for (Status s : Status.values()) {
            if (has(s)) {
                displays.add(s.getDisplay());
            }
        }
        if (displays.isEmpty()) {
            return "OK";
        } else {
            return displays.toString().replace("[", "").replace("]", "");
        }
    }

    public String toLongString() {
        displays.clear();
        for (Status s : Status.values()) {
            if (has(s)) {
                displays.add(s.toString());
            }
        }
        if (displays.isEmpty()) {
            return "OK";
        } else {
            return displays.toString().replace("[", "").replace("]", "").replace(",", " and ");
        }
    }

}
