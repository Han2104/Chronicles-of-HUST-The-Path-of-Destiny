package com.hust.game.maps.d9;

import com.hust.game.util.AssetLoader;
import org.w3c.dom.*;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * D9MapLoader - Load TMX map và TSX tilesets
 */
public class D9MapLoader {
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;

    public D9MapLoader() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public D9Map loadMap(String mapPath) {
        String normalizedMapPath = AssetLoader.normalize(mapPath);
        System.out.println("[D9] Loading map: " + normalizedMapPath);

        if (builder == null) {
            System.err.println("[D9] Warning: XML parser is not available.");
            return null;
        }

        try {
            Document doc = parseXml(normalizedMapPath);

            // Parse map attributes
            Element mapElement = doc.getDocumentElement();
            int width = Integer.parseInt(mapElement.getAttribute("width"));
            int height = Integer.parseInt(mapElement.getAttribute("height"));
            int tileWidth = Integer.parseInt(mapElement.getAttribute("tilewidth"));
            int tileHeight = Integer.parseInt(mapElement.getAttribute("tileheight"));

            D9Map map = new D9Map(width, height, tileWidth, tileHeight);
            System.out.println("[D9] Map size: " + width + "x" + height
                    + " tiles (" + map.getPixelWidth() + "x" + map.getPixelHeight() + " px)");
            System.out.println("[D9] Tile size: " + tileWidth + "x" + tileHeight);

            // TASK 1: TMX Validation Logs
            System.out.println("[D9] TASK 1 - TMX Validation:");
            System.out.println("[D9]   Map width: " + width + ", height: " + height);

            parseTilesets(map, AssetLoader.parentPath(normalizedMapPath), doc);

            // Parse layers
            NodeList layerNodes = doc.getElementsByTagName("layer");
            boolean platformsLayerFound = false;
            boolean floorDividerLayerFound = false;
            int platformTileCount = 0;
            int floorDividerTileCount = 0;
            List<D9Map.Rectangle> collisionBoxes = new ArrayList<>();
            for (int i = 0; i < layerNodes.getLength(); i++) {
                Element layerElement = (Element) layerNodes.item(i);
                String layerName = layerElement.getAttribute("name");
                int[][] layerData = parseLayerData(layerElement, width, height);

                switch (layerName) {
                    case "Background":
                        map.setBackgroundLayer(layerData);
                        break;
                    case "Platforms":
                        platformsLayerFound = true;
                        map.setPlatformsLayer(layerData);
                        List<D9Map.Rectangle> platformBoxes = generateCollisionBoxes(layerData, tileWidth, tileHeight);
                        platformTileCount = platformBoxes.size();
                        collisionBoxes.addAll(platformBoxes);
                        break;
                    case "Decor":
                        map.setDecorLayer(layerData);
                        break;
                    case "FloorDivider":
                        floorDividerLayerFound = true;
                        map.setFloorDividerLayer(layerData);
                        List<D9Map.Rectangle> floorDividerBoxes = generateCollisionBoxes(layerData, tileWidth, tileHeight);
                        floorDividerTileCount = floorDividerBoxes.size();
                        collisionBoxes.addAll(floorDividerBoxes);
                        break;
                    case "Checkpoints":
                        map.setCheckpointsLayer(layerData);
                        break;
                }
            }
            map.setPlatformCollisionBoxes(collisionBoxes);
            System.out.println("[D9] Platforms layer found: " + platformsLayerFound);
            System.out.println("[D9] FloorDivider layer found: " + floorDividerLayerFound);
            System.out.println("[D9] Platform tiles found: " + platformTileCount);
            System.out.println("[D9] FloorDivider tiles found: " + floorDividerTileCount);
            System.out.println("[D9] Platform collision rectangles created: " + map.getPlatformCollisionBoxes().size());

            // TASK 1: Layer validation logs
            System.out.println("[D9]   Layers found: Background, Platforms, Decor, FloorDivider, Checkpoints");
            System.out.println("[D9]   All expected layers exist: " + (platformsLayerFound && map.getBackgroundLayer() != null && map.getDecorLayer() != null && map.getFloorDividerLayer() != null && map.getCheckpointsLayer() != null));
            System.out.println("[D9]   Non-empty tile count: " + ((platformTileCount + floorDividerTileCount) > 0 ? "Yes (platforms: " + platformTileCount + ", floor divider: " + floorDividerTileCount + ")" : "No"));

            // Parse objects
            NodeList objectGroupNodes = doc.getElementsByTagName("objectgroup");
            for (int i = 0; i < objectGroupNodes.getLength(); i++) {
                Element objectGroupElement = (Element) objectGroupNodes.item(i);
                if ("Objects".equals(objectGroupElement.getAttribute("name"))) {
                    List<D9Object> objects = parseObjects(objectGroupElement);
                    map.setObjects(objects);
                    break;
                }
            }

            if (map.getObjects() == null) {
                map.setObjects(Collections.emptyList());
            }

            // TASK 1: Tileset and object validation logs
            System.out.println("[D9]   Tileset references: " + map.getTilesets().size() + " tilesets loaded");
            for (D9Map.TilesetInfo ts : map.getTilesets()) {
                System.out.println("[D9]     - " + ts.getImageSource() + " (firstgid=" + ts.getFirstGid() + ")");
            }
            System.out.println("[D9]   Objects loaded: " + map.getObjects().size());
            System.out.println("[D9]   Object names/classes:");
            for (D9Object obj : map.getObjects()) {
                System.out.println("[D9]     - " + obj.getName() + " (" + obj.getClassType() + ")");
            }
            System.out.println("[D9]   PlayerSpawn candidates:");
            List<D9Object> spawns = map.getPlayerSpawns();
            for (D9Object spawn : spawns) {
                System.out.println("[D9]     - " + spawn.getName() + " at (" + spawn.getX() + "," + spawn.getY() + ")");
            }
            System.out.println("[D9]   Objects named 'spawn_floor_1': " + map.getObjects().stream().filter(o -> "spawn_floor_1".equals(o.getName())).count());
            System.out.println("[D9]   MapBoundary objects:");
            List<D9Object> boundaries = map.getMapBoundaries();
            for (D9Object boundary : boundaries) {
                System.out.println("[D9]     - " + boundary.getName() + " (" + boundary.getProperty("side") + ")");
            }

            configureMapBoundaries(map);
            validateExpectedObjects(map);

            return map;

        } catch (Exception e) {
            System.err.println("[D9] Warning: failed to load map '" + normalizedMapPath + "': " + e.getMessage());
            return null;
        }
    }

    private Document parseXml(String path) throws Exception {
        try (InputStream stream = AssetLoader.openStream(path)) {
            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();
            return doc;
        }
    }

    private void parseTilesets(D9Map map, String mapDirectory, Document doc) {
        NodeList tilesetNodes = doc.getElementsByTagName("tileset");
        System.out.println("[D9Tileset] TMX tileset count: " + tilesetNodes.getLength());

        for (int i = 0; i < tilesetNodes.getLength(); i++) {
            Element tilesetElement = (Element) tilesetNodes.item(i);
            String source = tilesetElement.getAttribute("source");
            int firstGid = parseInt(tilesetElement.getAttribute("firstgid"), 0);
            System.out.println("[D9Tileset] firstgid=" + firstGid + ", source=" + source);

            if (tilesetElement.hasAttribute("source")) {
                String tsxPath = resolveTilesetSource(mapDirectory, source);
                if (tsxPath == null) {
                    System.err.println("[D9Tileset] Warning: unable to resolve tileset source '" + source + "'.");
                    continue;
                }
                parseTilesetFromTsx(tsxPath, firstGid, map);
            } else {
                parseInternalTileset(tilesetElement, firstGid, map, mapDirectory);
            }
        }
    }

    private void parseInternalTileset(Element tilesetElement, int firstGid, D9Map map, String mapDirectory) {
        try {
            String name = tilesetElement.getAttribute("name");
            int tileWidth = parseInt(tilesetElement.getAttribute("tilewidth"), 0);
            int tileHeight = parseInt(tilesetElement.getAttribute("tileheight"), 0);
            int tileCount = parseInt(tilesetElement.getAttribute("tilecount"), 0);
            int columns = parseInt(tilesetElement.getAttribute("columns"), 0);

            Element imageElement = (Element) tilesetElement.getElementsByTagName("image").item(0);
            if (imageElement == null) {
                System.err.println("[D9Tileset] Warning: inline tileset missing <image> element.");
                return;
            }
            String imageSource = imageElement.getAttribute("source");
            int imageWidth = parseInt(imageElement.getAttribute("width"), 0);
            int imageHeight = parseInt(imageElement.getAttribute("height"), 0);
            String imagePath = resolveImageSource(mapDirectory, imageSource);
            boolean imageExists = assetExists(imagePath);
            boolean imageLoaded = false;
            if (imageExists) {
                try (InputStream imageStream = AssetLoader.openStream(imagePath)) {
                    imageLoaded = ImageIO.read(imageStream) != null;
                } catch (Exception ignored) {
                }
            }

            System.out.println("[D9Tileset] Inline TSX loaded: " + name
                    + " tilecount=" + tileCount + " columns=" + columns
                    + " image source=" + imageSource + " imagePath=" + imagePath
                    + " imageExists=" + imageExists + " imageLoaded=" + imageLoaded
                    + " imageWidth=" + imageWidth + " imageHeight=" + imageHeight);

            if (tileWidth <= 0 || tileHeight <= 0 || tileCount <= 0 || columns <= 0) {
                System.err.println("[D9Tileset] Warning: invalid inline tileset metadata for '" + name + "'.");
            }
            map.addTileset(new D9Map.TilesetInfo(firstGid, tileCount, columns, tileWidth, tileHeight, imagePath));
        } catch (Exception e) {
            System.err.println("[D9Tileset] Warning: failed to parse inline tileset: " + e.getMessage());
        }
    }

    private String resolveTilesetSource(String mapDirectory, String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String normalizedSource = AssetLoader.normalize(source);
        String candidate = AssetLoader.joinPath(mapDirectory, normalizedSource);
        if (assetExists(candidate)) {
            return candidate;
        }

        if (normalizedSource.matches("^[A-Za-z]:/.*")) {
            String fileName = Paths.get(normalizedSource).getFileName().toString();
            String localCandidate = AssetLoader.joinPath(mapDirectory, fileName);
            if (assetExists(localCandidate)) {
                System.out.println("[D9Tileset] Resolving tileset source '" + source + "' to local file '" + localCandidate + "'.");
                return localCandidate;
            }
        }

        Path path = Paths.get(normalizedSource);
        if (path.getFileName() != null) {
            String baseName = path.getFileName().toString();
            String localCandidate = AssetLoader.joinPath(mapDirectory, baseName);
            if (assetExists(localCandidate)) {
                System.out.println("[D9Tileset] Resolving tileset source '" + source + "' to local file '" + localCandidate + "'.");
                return localCandidate;
            }
        }

        return candidate;
    }

    private boolean assetExists(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        String normalizedPath = AssetLoader.normalize(path);
        Path filePath = Paths.get(normalizedPath);
        if (Files.isRegularFile(filePath)) {
            return true;
        }

        // Allow classpath resources to be resolved later by AssetLoader.openStream
        try {
            AssetLoader.openStream(normalizedPath).close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void parseTilesetFromTsx(String tsxPath, int firstGid, D9Map map) {
        try {
            Document tsxDoc = parseXml(tsxPath);
            Element tilesetElement = tsxDoc.getDocumentElement();
            String name = tilesetElement.getAttribute("name");
            int tileWidth = parseInt(tilesetElement.getAttribute("tilewidth"), 0);
            int tileHeight = parseInt(tilesetElement.getAttribute("tileheight"), 0);
            int tileCount = parseInt(tilesetElement.getAttribute("tilecount"), 0);
            int columns = parseInt(tilesetElement.getAttribute("columns"), 0);

            Element imageElement = (Element) tilesetElement.getElementsByTagName("image").item(0);
            if (imageElement == null) {
                System.err.println("[D9Tileset] Warning: TSX '" + tsxPath + "' missing <image> element.");
                return;
            }

            String imageSource = imageElement.getAttribute("source");
            int imageWidth = parseInt(imageElement.getAttribute("width"), 0);
            int imageHeight = parseInt(imageElement.getAttribute("height"), 0);
            String imagePath = resolveImageSource(AssetLoader.parentPath(tsxPath), imageSource);
            boolean imageExists = assetExists(imagePath);
            boolean imageLoaded = false;
            if (imageExists) {
                try (InputStream imageStream = AssetLoader.openStream(imagePath)) {
                    imageLoaded = ImageIO.read(imageStream) != null;
                } catch (Exception ignored) {
                }
            }

            System.out.println("[D9Tileset] TSX loaded: " + tsxPath + " name=" + name
                    + " tilecount=" + tileCount + " columns=" + columns
                    + " image source=" + imageSource + " imagePath=" + imagePath
                    + " imageExists=" + imageExists + " imageLoaded=" + imageLoaded
                    + " imageWidth=" + imageWidth + " imageHeight=" + imageHeight);

            if (tileWidth <= 0 || tileHeight <= 0 || tileCount <= 0 || columns <= 0) {
                System.err.println("[D9Tileset] Warning: invalid tileset metadata in '" + tsxPath + "'.");
            }

            map.addTileset(new D9Map.TilesetInfo(firstGid, tileCount, columns, tileWidth, tileHeight, imagePath));
        } catch (Exception e) {
            System.err.println("[D9Tileset] Warning: failed to load tileset '" + tsxPath + "': " + e.getMessage());
        }
    }

    private String resolveImageSource(String tsxDirectory, String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String normalizedSource = AssetLoader.normalize(source);
        String candidate = AssetLoader.joinPath(tsxDirectory, normalizedSource);
        if (assetExists(candidate)) {
            return candidate;
        }
        if (normalizedSource.matches("^[A-Za-z]:/.*")) {
            String fileName = Paths.get(normalizedSource).getFileName().toString();
            String localCandidate = AssetLoader.joinPath(tsxDirectory, fileName);
            if (assetExists(localCandidate)) {
                System.out.println("[D9Tileset] Resolving image source '" + source + "' to local file '" + localCandidate + "'.");
                return localCandidate;
            }
        }
        Path path = Paths.get(normalizedSource);
        if (path.getFileName() != null) {
            String baseName = path.getFileName().toString();
            String localCandidate = AssetLoader.joinPath(tsxDirectory, baseName);
            if (assetExists(localCandidate)) {
                System.out.println("[D9Tileset] Resolving image source '" + source + "' to local file '" + localCandidate + "'.");
                return localCandidate;
            }
        }
        return candidate;
    }

    private int[][] parseLayerData(Element layerElement, int mapWidth, int mapHeight) {
        Element dataElement = (Element) layerElement.getElementsByTagName("data").item(0);
        if (dataElement == null) {
            return new int[mapHeight][mapWidth];
        }

        String encoding = dataElement.getAttribute("encoding");
        String compression = dataElement.getAttribute("compression");
        int width = parseInt(layerElement.getAttribute("width"), mapWidth);
        int height = parseInt(layerElement.getAttribute("height"), mapHeight);
        int[][] layer = new int[height][width];

        if ("csv".equals(encoding)) {
            String csvData = dataElement.getTextContent().replace("\r", "").trim();
            String[] tokens = csvData.split(",");
            int expectedTiles = width * height;
            int count = Math.min(tokens.length, expectedTiles);
            for (int i = 0; i < count; i++) {
                String token = tokens[i].trim();
                if (!token.isEmpty()) {
                    layer[i / width][i % width] = parseInt(token, 0);
                }
            }
            return layer;
        }

        if (encoding == null || encoding.isBlank()) {
            NodeList tileNodes = dataElement.getElementsByTagName("tile");
            int expectedTiles = width * height;
            int count = Math.min(tileNodes.getLength(), expectedTiles);
            for (int i = 0; i < count; i++) {
                Element tileElement = (Element) tileNodes.item(i);
                layer[i / width][i % width] = parseInt(tileElement.getAttribute("gid"), 0);
            }
            return layer;
        }

        if ("base64".equals(encoding)) {
            String rawData = dataElement.getTextContent().trim();
            if (rawData.isBlank()) {
                return layer;
            }
            byte[] decoded = decodeBase64Data(rawData, compression, layerElement.getAttribute("name"));
            if (decoded == null) {
                return layer;
            }
            NodeList chunkElements = dataElement.getElementsByTagName("chunk");
            if (chunkElements.getLength() > 0) {
                for (int i = 0; i < chunkElements.getLength(); i++) {
                    Element chunk = (Element) chunkElements.item(i);
                    fillChunk(layer, chunk, decoded);
                }
                return layer;
            }
            fillLayerFromBytes(layer, decoded, width, height);
            return layer;
        }

        System.err.println("[D9Tileset] Warning: unsupported layer encoding: " + encoding
                + ". Layer '" + layerElement.getAttribute("name") + "' will be empty.");
        return layer;
    }

    private byte[] decodeBase64Data(String rawData, String compression, String layerName) {
        try {
            byte[] bytes = Base64.getDecoder().decode(rawData);
            if (compression == null || compression.isBlank()) {
                return bytes;
            }
            try (InputStream compressedStream = new ByteArrayInputStream(bytes)) {
                if ("gzip".equalsIgnoreCase(compression)) {
                    try (GZIPInputStream gzip = new GZIPInputStream(compressedStream)) {
                        return gzip.readAllBytes();
                    }
                }
                if ("zlib".equalsIgnoreCase(compression) || "zlib".equalsIgnoreCase(compression.trim())) {
                    try (InflaterInputStream inflater = new InflaterInputStream(compressedStream)) {
                        return inflater.readAllBytes();
                    }
                }
                System.err.println("[D9Tileset] Warning: unsupported compression '" + compression + "' for layer '" + layerName + "'.");
                return null;
            }
        } catch (Exception e) {
            System.err.println("[D9Tileset] Warning: failed to decode base64 layer '" + layerName + "': " + e.getMessage());
            return null;
        }
    }

    private void fillChunk(int[][] layer, Element chunk, byte[] decoded) {
        int chunkX = parseInt(chunk.getAttribute("x"), 0);
        int chunkY = parseInt(chunk.getAttribute("y"), 0);
        int chunkWidth = parseInt(chunk.getAttribute("width"), 0);
        int chunkHeight = parseInt(chunk.getAttribute("height"), 0);
        if (chunkWidth <= 0 || chunkHeight <= 0) {
            return;
        }
        int expectedTiles = chunkWidth * chunkHeight;
        int[] gids = decodeTileGids(decoded, expectedTiles);
        for (int i = 0; i < gids.length; i++) {
            int x = chunkX + (i % chunkWidth);
            int y = chunkY + (i / chunkWidth);
            if (y >= 0 && y < layer.length && x >= 0 && x < layer[y].length) {
                layer[y][x] = gids[i];
            }
        }
    }

    private void fillLayerFromBytes(int[][] layer, byte[] decoded, int width, int height) {
        int expectedTiles = width * height;
        int[] gids = decodeTileGids(decoded, expectedTiles);
        for (int i = 0; i < gids.length; i++) {
            int x = i % width;
            int y = i / width;
            if (y < layer.length && x < layer[y].length) {
                layer[y][x] = gids[i];
            }
        }
    }

    private int[] decodeTileGids(byte[] decoded, int expectedTiles) {
        int tileCount = Math.min(expectedTiles, decoded.length / 4);
        int[] gids = new int[tileCount];
        for (int i = 0; i < tileCount; i++) {
            int index = i * 4;
            gids[i] = (decoded[index] & 0xFF)
                    | ((decoded[index + 1] & 0xFF) << 8)
                    | ((decoded[index + 2] & 0xFF) << 16)
                    | ((decoded[index + 3] & 0xFF) << 24);
        }
        return gids;
    }

    private List<D9Object> parseObjects(Element objectGroupElement) {
        List<D9Object> objects = new ArrayList<>();
        NodeList objectNodes = objectGroupElement.getElementsByTagName("object");

        for (int i = 0; i < objectNodes.getLength(); i++) {
            Element objectElement = (Element) objectNodes.item(i);

            String name = objectElement.getAttribute("name");
            String classType = readObjectClassType(objectElement);
            double x = parseDouble(objectElement.getAttribute("x"), 0);
            double y = parseDouble(objectElement.getAttribute("y"), 0);
            int width = (int) Math.round(parseDouble(objectElement.getAttribute("width"), 32));
            int height = (int) Math.round(parseDouble(objectElement.getAttribute("height"), 32));

            Map<String, String> properties = new LinkedHashMap<>();
            NodeList propertyNodes = objectElement.getElementsByTagName("property");
            for (int j = 0; j < propertyNodes.getLength(); j++) {
                Element propertyElement = (Element) propertyNodes.item(j);
                String propName = propertyElement.getAttribute("name");
                String propValue = propertyElement.hasAttribute("value")
                        ? propertyElement.getAttribute("value")
                        : propertyElement.getTextContent();
                if (propName != null && !propName.isBlank()) {
                    properties.put(propName, propValue != null ? propValue.trim() : "");
                }
            }

            if (classType.isBlank()) {
                System.err.println("[D9] Warning: object '" + name + "' has no class/type.");
            }

            objects.add(new D9Object(name, classType, (int) Math.round(x), (int) Math.round(y), width, height, properties));
        }

        return objects;
    }

    private String readObjectClassType(Element objectElement) {
        String classType = objectElement.getAttribute("class");
        if (classType == null || classType.isBlank()) {
            classType = objectElement.getAttribute("type");
        }
        if (classType == null || classType.isBlank()) {
            classType = objectElement.getAttribute("classType");
        }
        return classType != null ? classType.trim() : "";
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private List<D9Map.Rectangle> generateCollisionBoxes(int[][] layerData, int tileWidth, int tileHeight) {
        List<D9Map.Rectangle> boxes = new ArrayList<>();
        if (layerData == null || layerData.length == 0) {
            return boxes;
        }
        int height = layerData.length;
        int width = layerData[0].length;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((layerData[y][x] & 0x1FFFFFFF) != 0) {
                    boxes.add(new D9Map.Rectangle(x * tileWidth, y * tileHeight, tileWidth, tileHeight));
                }
            }
        }

        return boxes;
    }

    private void configureMapBoundaries(D9Map map) {
        List<D9Object> boundaryObjects = map.getMapBoundaries();
        Map<String, D9Map.Rectangle> explicitBoundaryBySide = new LinkedHashMap<>();
        List<String> explicitSides = new ArrayList<>();

        for (D9Object object : boundaryObjects) {
            if (!object.getPropertyAsBoolean("solid", true)) {
                continue;
            }
            String side = object.getProperty("side");
            if (side == null || side.isBlank()) {
                side = inferBoundarySide(object.getName());
                System.err.println("[D9] Warning: MapBoundary '" + object.getName()
                        + "' is missing property side; inferred side='" + side + "'.");
            }
            if (side == null || side.isBlank()) {
                System.err.println("[D9] Warning: MapBoundary '" + object.getName()
                        + "' skipped because side is missing.");
                continue;
            }
            String normalizedSide = side.toLowerCase();
            explicitBoundaryBySide.put(normalizedSide, object.toRectangle());
            if (!explicitSides.contains(normalizedSide)) {
                explicitSides.add(normalizedSide);
            }
        }

        List<String> missingSides = new ArrayList<>();
        List<String> fallbackSides = new ArrayList<>();
        List<D9Map.Rectangle> fallbackBoundaryBoxes = new ArrayList<>();
        Map<String, D9Map.Rectangle> fallback = fallbackBoundaries(map.getPixelWidth(), map.getPixelHeight());
        for (Map.Entry<String, D9Map.Rectangle> entry : fallback.entrySet()) {
            if (!explicitBoundaryBySide.containsKey(entry.getKey())) {
                missingSides.add(entry.getKey());
                fallbackSides.add(entry.getKey());
                fallbackBoundaryBoxes.add(entry.getValue());
            }
        }

        map.setBoundaryCollisionBoxes(new ArrayList<>(explicitBoundaryBySide.values()), fallbackBoundaryBoxes);

        System.out.println("[D9] Explicit MapBoundary objects found: " + explicitBoundaryBySide.size());
        System.out.println("[D9] Explicit boundary sides: " + formatSides(explicitSides));
        System.out.println("[D9] Missing boundary sides: " + formatSides(missingSides));
        System.out.println("[D9] Fallback boundaries created for sides: " + formatSides(fallbackSides));
        System.out.println("[D9] Boundary collision rectangles created: " + map.getBoundaryCollisionBoxes().size());
        System.out.println("[D9] Total solid collisions: " + map.getCollisionBoxes().size());
    }

    private String formatSides(List<String> sides) {
        if (sides == null || sides.isEmpty()) {
            return "none";
        }
        return String.join(",", sides);
    }

    private Map<String, D9Map.Rectangle> fallbackBoundaries(int mapWidthPx, int mapHeightPx) {
        Map<String, D9Map.Rectangle> fallback = new LinkedHashMap<>();
        fallback.put("left", new D9Map.Rectangle(-32, 0, 32, mapHeightPx));
        fallback.put("right", new D9Map.Rectangle(mapWidthPx, 0, 32, mapHeightPx));
        fallback.put("top", new D9Map.Rectangle(0, -32, mapWidthPx, 32));
        fallback.put("bottom", new D9Map.Rectangle(0, mapHeightPx, mapWidthPx, 64));
        return fallback;
    }

    private String inferBoundarySide(String objectName) {
        if (objectName == null) {
            return "";
        }
        String normalized = objectName.toLowerCase();
        if (normalized.contains("left")) return "left";
        if (normalized.contains("right")) return "right";
        if (normalized.contains("top")) return "top";
        if (normalized.contains("bottom")) return "bottom";
        return "";
    }

    private void validateExpectedObjects(D9Map map) {
        warnMissingObject(map, "spawn_floor_1");
        D9Object spawn = map.getPlayerSpawn();
        if (spawn != null && spawn.getProperty("spawnId") == null && spawn.getProperty("spawId") != null) {
            System.err.println("[D9] Warning: spawn_floor_1 uses property 'spawId'; accepting it as spawnId fallback.");
        }

        for (int floor = 2; floor <= 7; floor++) {
            String checkpointId = "cp_" + floor;
            boolean found = map.getCheckpoints().stream()
                    .anyMatch(obj -> checkpointId.equals(obj.getProperty("checkpointId")));
            if (!found) {
                System.err.println("[D9] Warning: checkpoint missing for checkpointId=" + checkpointId);
            }
        }

        for (int floor = 1; floor <= 6; floor++) {
            warnMissingObject(map, "iron_door_floor_" + floor);
        }
        boolean finalDoorFound = map.getQuizDoors().stream()
                .anyMatch(obj -> "FinalQuizDoor".equals(obj.getClassType()));
        if (!finalDoorFound) {
            System.err.println("[D9] Warning: FinalQuizDoor object is missing.");
        } else if (map.getObjectByName("iron_door_floor_7") == null) {
            System.err.println("[D9] Warning: expected final door name iron_door_floor_7 is missing; using FinalQuizDoor class object instead.");
        }
    }

    private void warnMissingObject(D9Map map, String objectName) {
        if (map.getObjectByName(objectName) == null) {
            System.err.println("[D9] Warning: expected object missing: " + objectName);
        }
    }
}
