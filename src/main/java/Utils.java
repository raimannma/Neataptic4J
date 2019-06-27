import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Utils {
    public static ArrayList<Double> toList(final double[] input) {
        return Arrays.stream(input).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<Integer> getKeys(final ArrayList<Connection> list) {
        return IntStream.range(0, list.size())
                .filter(i -> list.get(i) != null)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
