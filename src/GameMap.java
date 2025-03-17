import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class GameMap {
    private PApplet p;
    private PImage[] tiles;
    private int[][] backgroundLayer;
    private int[][] foregroundLayer;
    private int tileWidth, tileHeight, scaleFactor;

    // Tile indices that block player movement
    private int[] solidTiles = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38};
    private int[] solidForegroundTiles = solidTiles;

    public GameMap(PApplet p, PImage tileset, int cols, int rows, int scaleFactor) {
        this.p = p;
        this.scaleFactor = scaleFactor;
        this.tileWidth = tileset.width / cols;
        this.tileHeight = tileset.height / rows;
        extractTiles(tileset, cols, rows);
    }

    // Extract individual tiles from tileset image
    private void extractTiles(PImage tileset, int cols, int rows) {
        tiles = new PImage[cols * rows];
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                tiles[i + j * cols] = tileset.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    // Load map data from JSON file
    public void loadMapFromJSON(String filename) {
        JSONObject mapData = p.loadJSONObject(filename);
        int mapWidth = mapData.getInt("width");
        int mapHeight = mapData.getInt("height");

        backgroundLayer = parseLayer(mapData.getJSONArray("tiles"), mapWidth, mapHeight);
        foregroundLayer = parseLayer(mapData.getJSONArray("foreground"), mapWidth, mapHeight);
    }

    private int[][] parseLayer(JSONArray layerArray, int width, int height) {
        int[][] layer = new int[height][width];
        for (int i = 0; i < height; i++) {
            JSONArray row = layerArray.getJSONArray(i);
            for (int j = 0; j < width; j++) {
                layer[i][j] = row.getInt(j);
            }
        }
        return layer;
    }

    // Save map data to JSON file
    public void saveMapToJSON(String filename) {
        JSONObject mapData = new JSONObject();
        mapData.setInt("width", getMapWidth());
        mapData.setInt("height", getMapHeight());
        mapData.setJSONArray("tiles", convertLayerToJSONArray(backgroundLayer));
        mapData.setJSONArray("foreground", convertLayerToJSONArray(foregroundLayer));

        p.saveJSONObject(mapData, "data/" + filename);
        p.println("Map saved to " + filename);
    }

    private JSONArray convertLayerToJSONArray(int[][] layer) {
        JSONArray layerArray = new JSONArray();
        for (int[] row : layer) {
            JSONArray rowArray = new JSONArray();
            for (int tile : row) {
                rowArray.append(tile);
            }
            layerArray.append(rowArray);
        }
        return layerArray;
    }

    public JSONArray getEnemiesFromJSON(String filename) {
        return p.loadJSONObject(filename).getJSONArray("enemies");
    }

    // Render a layer of the map
    public void drawLayer(int[][] layer) {
        for (int i = 0; i < layer.length; i++) {
            for (int j = 0; j < layer[i].length; j++) {
                int tileIndex = layer[i][j];
                if (tileIndex != 0) {  // Skip empty tiles
                    p.image(tiles[tileIndex], j * tileWidth * scaleFactor, i * tileHeight * scaleFactor,
                            tileWidth * scaleFactor, tileHeight * scaleFactor);
                }
            }
        }
    }

    public void drawBackgroundLayer() { drawLayer(backgroundLayer); }
    public void drawForegroundLayer() { drawLayer(foregroundLayer); }

    // Check if an object collides with solid tiles
    public boolean checkCollision(float x, float y, float width, float height) {
        int startX = (int)(x / (tileWidth * scaleFactor));
        int startY = (int)(y / (tileHeight * scaleFactor));
        int endX = (int)((x + width) / (tileWidth * scaleFactor));
        int endY = (int)((y + height) / (tileHeight * scaleFactor));

        return checkTileCollision(backgroundLayer, startX, startY, endX, endY, solidTiles) ||
                checkTileCollision(foregroundLayer, startX, startY, endX, endY, solidForegroundTiles);
    }

    private boolean checkTileCollision(int[][] layer, int startX, int startY, int endX, int endY, int[] solidList) {
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                if (y >= 0 && y < layer.length && x >= 0 && x < layer[0].length) {
                    if (contains(solidList, layer[y][x])) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean contains(int[] array, int value) {
        for (int num : array) {
            if (num == value) return true;
        }
        return false;
    }

    // Methods to modify tiles
    public void setBackgroundTile(int row, int col, int tileIndex) {
        modifyTile(backgroundLayer, row, col, tileIndex);
    }

    public void setForegroundTile(int row, int col, int tileIndex) {
        modifyTile(foregroundLayer, row, col, tileIndex);
    }

    private void modifyTile(int[][] layer, int row, int col, int tileIndex) {
        if (row >= 0 && row < layer.length && col >= 0 && col < layer[0].length) {
            layer[row][col] = tileIndex;
        }
    }

    // Getters for map properties
    public int getMapWidth() { return backgroundLayer[0].length; }
    public int getMapHeight() { return backgroundLayer.length; }
    public int getWidthInPixels() { return getMapWidth() * tileWidth * scaleFactor; }
    public int getHeightInPixels() { return getMapHeight() * tileHeight * scaleFactor; }
    public int getTileWidth() { return tileWidth; }
    public int getTileHeight() { return tileHeight; }
    public int getBackgroundTile(int row, int col) { return backgroundLayer[row][col]; }
    public int getForegroundTile(int row, int col) { return foregroundLayer[row][col]; }
    public int[] getSolidTiles() { return solidTiles; }
    public int[] getSolidForegroundTiles() { return solidForegroundTiles; }
    public PImage[] getTiles() { return tiles; }
}