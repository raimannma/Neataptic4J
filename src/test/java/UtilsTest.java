import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

    @Test
    void toList() {
        final double[] testArr = new double[10];
        Arrays.setAll(testArr, i -> Math.random() * 10);

        final List<Double> list = Utils.toList(testArr);
        IntStream.range(0, list.size())
                .forEach(i -> assertEquals(testArr[i], list.get(i)));

        final String[] arr = new String[4];
        arr[0] = "Dies";
        arr[1] = "ist";
        arr[2] = "ein";
        arr[3] = "Test";

        final List<String> stringList = Utils.toList(arr);
        IntStream.range(0, stringList.size())
                .forEach(i -> assertEquals(arr[i], stringList.get(i)));
    }

}