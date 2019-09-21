package architecture;

import java.util.Arrays;

public class DataEntry {
    double[] input;
    double[] output;

    public DataEntry(final double[] input, final double[] output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.input);
        result = 31 * result + Arrays.hashCode(this.output);
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final DataEntry dataEntry = (DataEntry) o;
        return Arrays.equals(this.input, dataEntry.input) &&
                Arrays.equals(this.output, dataEntry.output);
    }
}
