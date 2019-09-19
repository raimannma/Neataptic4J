package architecture;

class Utils {
    static <T> int indexOf(final T[] arr, final T needle) {
        for (int i = 0; i < arr.length; i++) {
            final T elem = arr[i];
            if (elem.equals(needle)) {
                return i;
            }
        }
        return -1;
    }
}
