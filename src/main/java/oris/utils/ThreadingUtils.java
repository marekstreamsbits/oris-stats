package oris.utils;

public class ThreadingUtils {

    private ThreadingUtils() {
    }

    public static int defaultThreadCount() {
        final int availableProcessors = Runtime.getRuntime().availableProcessors();
        if (availableProcessors <= 1) {
            return 4;
        } else {
            return (int) Math.round(availableProcessors * 1.5);
        }
    }
}