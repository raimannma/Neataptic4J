import java.util.ArrayList;

public class ConnectionHistory {
    final ArrayList<Connection> in;
    final Connection self;
    final ArrayList<Connection> gated;
    final ArrayList<Connection> out;

    public ConnectionHistory(final Node node) {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.gated = new ArrayList<>();
        this.self = new Connection(node, node, 0);
    }
}
