import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class NodeTest {

    @org.junit.jupiter.api.Test
    void copy() {
        final Node node = new Node();
        node.bias = 5;
        node.squash = Activation.BENT_IDENTITY;
        node.mask = 1.45;
        node.type = NodeType.HIDDEN;
        final Node copy = node.copy();

        assertEquals(node.bias, copy.bias);
        assertEquals(node.squash, copy.squash);
        assertEquals(node.mask, copy.mask);
        assertEquals(node.type, copy.type);

        node.bias = 1;
        node.squash = Activation.BIPOLAR;
        node.mask = 1.6;
        node.type = NodeType.INPUT;

        assertNotEquals(node.bias, copy.bias);
        assertNotEquals(node.squash, copy.squash);
        assertNotEquals(node.mask, copy.mask);
        assertNotEquals(node.type, copy.type);
    }
}