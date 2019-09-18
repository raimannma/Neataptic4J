package version2.methods;

public enum ConnectionType {
    ALL_TO_ALL("OUTPUT"), ALL_TO_ELSE("INPUT"), ONE_TO_ONE("SELF");

    private final String name;

    ConnectionType(final String name) {
        this.name = name;
    }
}
