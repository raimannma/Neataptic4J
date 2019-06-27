import java.util.ArrayList;
import java.util.List;

public class ConnectionHistory {
    final List<Connection> in;
    final List<Connection> selfArr;
    final List<Connection> gated;
    final List<Connection> out;
    Connection self;
    Node node;

    public ConnectionHistory(final Node node) {
        this();
        this.node = node;
        this.self = new Connection(node, node, 0.0);
    }

    public ConnectionHistory() {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.gated = new ArrayList<>();
        this.selfArr = new ArrayList<>();
    }
}
