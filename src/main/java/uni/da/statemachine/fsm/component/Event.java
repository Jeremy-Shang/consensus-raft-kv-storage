package uni.da.statemachine.fsm.component;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class Event {
    public EventType type;

    public enum Type {
        FAIL, SUCCESS;
    }

    public Event(EventType type) {
        type = type;

    }
}
