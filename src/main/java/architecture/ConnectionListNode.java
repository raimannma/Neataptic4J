package architecture;

import java.util.ArrayList;

class ConnectionListNode {
    final Connection self;
    final ArrayList<Connection> in;
    final ArrayList<Connection> out;
    final ArrayList<Connection> gated;

    ConnectionListNode(final Node node) {
        this.in = new ArrayList<>();
        this.out = new ArrayList<>();
        this.gated = new ArrayList<>();
        this.self = new Connection(node, node, 0);
    }
}
