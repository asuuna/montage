package app.ai.system;

import java.util.Locale;
import java.util.Map;

public final class GpuCapabilityDetector {
    private GpuCapabilityDetector() {
    }

    public static Map<String, Boolean> detect() {
        boolean cuda = hasLibrary("cuda") || hasLibrary("nvcuda");
        boolean directml = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows") && hasLibrary("directml");
        boolean metal = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")
                && System.getProperty("os.arch").toLowerCase(Locale.ROOT).contains("aarch64");
        return Map.of(
                "cuda", cuda,
                "directml", directml,
                "metal", metal
        );
    }

    private static boolean hasLibrary(String library) {
        try {
            System.loadLibrary(library);
            return true;
        } catch (UnsatisfiedLinkError ignored) {
            return false;
        }
    }
}
