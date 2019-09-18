package version2.methods;

public enum GatingType {
    OUTPUT("OUTPUT"),
    INPUT("INPUT"),
    SELF("SELF");

    private final String name;

    GatingType(final String name) {
        this.name = name;
    }
}
