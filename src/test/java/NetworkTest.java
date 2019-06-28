import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkTest {

    @Test
    void copy() {

        final Network network = new Architect().LSTM(new int[]{2, 5, 6, 1});
        network.mutate(Mutation.ADD_NODE);
        network.mutate(Mutation.ADD_CONN);
        network.mutate(Mutation.ADD_GATE);
        network.mutate(Mutation.MOD_ACTIVATION);

        final Network copy = network.copy();

        assertEquals(network.input, copy.input);
        assertEquals(network.output, copy.output);
        assertEquals(network.score, copy.score);
        assertTrue(this.listEquals(network.nodes, copy.nodes));
        assertTrue(this.listEquals(network.connections, copy.connections));
        assertTrue(this.listEquals(network.gates, copy.gates));
        assertTrue(this.listEquals(network.selfConnections, copy.selfConnections));
    }

    private <T> boolean listEquals(final ArrayList<T> list, final ArrayList<T> list1) {
        if (list.size() != list1.size()) {
            System.out.println("Wrong length!: " + list.size() + " - " + list1.size());
            return false;
        }
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).equals(list1.get(i))) {
                System.out.println(list.get(i).toString() + " - " + list1.get(i).toString());
                return false;
            }
        }
        return true;
    }
}