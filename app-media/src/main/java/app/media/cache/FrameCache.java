package app.media.cache;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

public class FrameCache {
    private final Map<String, BufferedImage> memoryCache;
    private final Path cacheDir;
    private final Object lock = new Object();

    public FrameCache(Path cacheDir, int maxEntries) throws IOException {
        Objects.requireNonNull(cacheDir, "cacheDir");
        if (maxEntries <= 0) {
            throw new IllegalArgumentException("maxEntries must be > 0");
        }
        this.cacheDir = cacheDir;
        Files.createDirectories(cacheDir);
        this.memoryCache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public BufferedImage get(String key, FrameLoader loader) throws IOException {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(loader, "loader");
        String hashedKey = hashKey(key);

        synchronized (lock) {
            BufferedImage cached = memoryCache.get(hashedKey);
            if (cached != null) {
                return cached;
            }
        }

        Path diskPath = cacheDir.resolve(hashedKey + ".png");
        if (Files.exists(diskPath)) {
            BufferedImage image = ImageIO.read(diskPath.toFile());
            synchronized (lock) {
                memoryCache.put(hashedKey, image);
            }
            return image;
        }

        BufferedImage loaded = loader.load();
        if (loaded == null) {
            return null;
        }

        ImageIO.write(loaded, "png", diskPath.toFile());
        synchronized (lock) {
            memoryCache.put(hashedKey, loaded);
        }
        return loaded;
    }

    public void clear() throws IOException {
        synchronized (lock) {
            memoryCache.clear();
        }
        try (var files = Files.list(cacheDir)) {
            files.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                }
            });
        }
    }

    private static String hashKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(key.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-1 algorithm not available", e);
        }
    }
}
