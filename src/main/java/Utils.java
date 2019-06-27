import java.util.ArrayList;
import java.util.Collections;

public enum Utils {
    ;

    public static <T> ArrayList<T> toList(final T[] input) {
        final ArrayList<T> out = new ArrayList<>();
        Collections.addAll(out, input);
        return out;
    }
}
