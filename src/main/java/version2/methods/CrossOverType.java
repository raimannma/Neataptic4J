package version2.methods;

import org.jetbrains.annotations.Nullable;

public enum CrossOverType {
    SINGLE_POINT("SINGLE_POINT", new double[]{0.4}),
    TWO_POINT("TWO_POINT", new double[]{0.4, 0.9}),
    UNIFORM("UNIFORM"),
    AVERAGE("AVERAGE");

    private final String name;
    @Nullable
    private final double[] config;

    CrossOverType(final String name, @Nullable final double[] config) {
        this.name = name;
        this.config = config;
    }

    CrossOverType(final String name) {
        this.name = name;
        this.config = null;
    }
}
