import java.util.ArrayList;

public class ConnectionHistory {
    ArrayList<Connection> in;
    Connection self;
    ArrayList<Connection> selfArr;
    ArrayList<Connection> gated;
    ArrayList<Connection> out;
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
