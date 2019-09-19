package architecture;

import java.util.ArrayList;
import java.util.List;

public class ConnectionListGroup {
    public List<Connection> in;
    public List<Connection> out;
    public List<Connection> self;

    ConnectionListGroup() {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.self = new ArrayList<>();
    }
}
