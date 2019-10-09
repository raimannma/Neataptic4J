package architecture;

import java.util.ArrayList;
import java.util.List;

public class ConnectionListGroup {
    public final List<Connection> in;
    public final List<Connection> out;
    public final List<Connection> self;

    ConnectionListGroup() {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.self = new ArrayList<>();
    }
}
