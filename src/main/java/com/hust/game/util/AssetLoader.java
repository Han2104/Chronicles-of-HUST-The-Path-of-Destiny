package com.hust.game.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads game assets from the project root first, then from the runtime classpath.
 */
public final class AssetLoader {
    private static final String ASSETS_PREFIX = "assets/";

    private AssetLoader() {
    }

    public static InputStream openStream(String path) throws IOException {
        String normalizedPath = normalize(path);
        for (Path candidate : fileCandidates(normalizedPath)) {
            if (Files.isRegularFile(candidate)) {
                return Files.newInputStream(candidate);
            }
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = AssetLoader.class.getClassLoader();
        }

        for (String candidate : resourceCandidates(normalizedPath)) {
            InputStream stream = loader.getResourceAsStream(candidate);
            if (stream != null) {
                return stream;
            }
        }

        throw new FileNotFoundException("Asset not found: " + normalizedPath);
    }

    private static List<Path> fileCandidates(String path) {
        Set<Path> candidates = new LinkedHashSet<>();
        candidates.add(Paths.get(path));

        addParentCandidates(candidates, Paths.get("").toAbsolutePath().normalize(), path);

        try {
            Path codePath = Paths.get(AssetLoader.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).toAbsolutePath().normalize();
            addParentCandidates(candidates, Files.isDirectory(codePath) ? codePath : codePath.getParent(), path);
        } catch (Exception ignored) {
        }

        return new ArrayList<>(candidates);
    }

    private static void addParentCandidates(Set<Path> candidates, Path start, String path) {
        Path current = start;
        while (current != null) {
            candidates.add(current.resolve(path).normalize());
            current = current.getParent();
        }
    }

    public static BufferedImage loadImage(String path) {
        try (InputStream stream = openStream(path)) {
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                System.err.println("[Assets] Warning: unsupported image format: " + normalize(path));
            }
            return image;
        } catch (IOException e) {
            System.err.println("[Assets] Warning: missing asset: " + normalize(path));
            return null;
        }
    }

    public static String readString(String path) throws IOException {
        try (InputStream stream = openStream(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String parentPath(String path) {
        String normalizedPath = normalize(path);
        int slashIndex = normalizedPath.lastIndexOf('/');
        if (slashIndex < 0) {
            return "";
        }
        return normalizedPath.substring(0, slashIndex);
    }

    public static String joinPath(String directory, String child) {
        String normalizedDirectory = normalize(directory);
        String normalizedChild = normalize(child);
        if (normalizedDirectory.isEmpty()) {
            return normalizedChild;
        }
        return normalizedDirectory + "/" + normalizedChild;
    }

    public static String normalize(String path) {
        if (path == null) {
            return "";
        }
        String normalizedPath = path.replace('\\', '/').trim();
        while (normalizedPath.startsWith("./")) {
            normalizedPath = normalizedPath.substring(2);
        }
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        return normalizedPath;
    }

    private static List<String> resourceCandidates(String path) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(path);
        if (path.startsWith(ASSETS_PREFIX)) {
            candidates.add(path.substring(ASSETS_PREFIX.length()));
        } else {
            candidates.add(ASSETS_PREFIX + path);
        }
        return new ArrayList<>(candidates);
    }
}
