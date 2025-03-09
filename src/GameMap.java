import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;


public class GameMap {
    private PApplet p;
    private PImage[] tiles;

    // Map data
    private int[][] backgroundLayer;
    private int[][] foregroundLayer;

    // Tile information
    private int tileWidth;
    private int tileHeight;
    private int scaleFactor;

    // Collision information
    private int[] solidTiles = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38};
    private int[] solidForegroundTiles = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38};

    public GameMap(PApplet p, PImage tileset, int cols, int rows, int scaleFactor) {
        this.p = p;
        this.scaleFactor = scaleFactor;

        // Calculate tile dimensions
        this.tileWidth = tileset.width / cols;
        this.tileHeight = tileset.height / rows;

        // Extract tiles from tileset
        this.tiles = new PImage[cols * rows];
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                tiles[i + j * cols] = tileset.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    public void loadMapFromJSON(String filename) {
        JSONObject mapData = p.loadJSONObject(filename);
        JSONArray tilesArray = mapData.getJSONArray("tiles");
        JSONArray foregroundArray = mapData.getJSONArray("foreground");

        int mapHeight = mapData.getInt("height");
        int mapWidth = mapData.getInt("width");

        backgroundLayer = new int[mapHeight][mapWidth];
        foregroundLayer = new int[mapHeight][mapWidth];

        // Load background layer
        for (int i = 0; i < mapHeight; i++) {
            JSONArray row = tilesArray.getJSONArray(i);
            for (int j = 0; j < mapWidth; j++) {
                backgroundLayer[i][j] = row.getInt(j);
            }
        }

        // Load foreground layer
        for (int i = 0; i < mapHeight; i++) {
            JSONArray row = foregroundArray.getJSONArray(i);
            for (int j = 0; j < mapWidth; j++) {
                foregroundLayer[i][j] = row.getInt(j);
            }
        }
    }

    public void saveMapToJSON(String filename) {
        JSONObject mapData = new JSONObject();
        mapData.setInt("width", backgroundLayer[0].length);
        mapData.setInt("height", backgroundLayer.length);

        // Create tiles array
        JSONArray tilesArray = new JSONArray();
        for (int i = 0; i < backgroundLayer.length; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < backgroundLayer[i].length; j++) {
                row.setInt(j, backgroundLayer[i][j]);
            }
            tilesArray.setJSONArray(i, row);
        }
        mapData.setJSONArray("tiles", tilesArray);

        // Create foreground array
        JSONArray foregroundArray = new JSONArray();
        for (int i = 0; i < foregroundLayer.length; i++) {
            JSONArray row = new JSONArray();
            for (int j = 0; j < foregroundLayer[i].length; j++) {
                row.setInt(j, foregroundLayer[i][j]);
            }
            foregroundArray.setJSONArray(i, row);
        }
        mapData.setJSONArray("foreground", foregroundArray);

        // Save to file
        p.saveJSONObject(mapData, "data/" + filename);
        p.println("Map saved to " + filename);
    }

    public JSONArray getEnemiesFromJSON(String filename) {
        JSONObject json = p.loadJSONObject(filename);
        return json.getJSONArray("enemies");
    }


    public void drawBackgroundLayer() {
        for (int i = 0; i < backgroundLayer.length; i++) {
            for (int j = 0; j < backgroundLayer[i].length; j++) {
                p.image(tiles[backgroundLayer[i][j]],
                        j * tileWidth * scaleFactor,
                        i * tileHeight * scaleFactor,
                        tileWidth * scaleFactor,
                        tileHeight * scaleFactor);
            }
        }
    }

    public void drawForegroundLayer() {
        for (int i = 0; i < foregroundLayer.length; i++) {
            for (int j = 0; j < foregroundLayer[i].length; j++) {
                int tileIndex = foregroundLayer[i][j];
                if (tileIndex != 0) { // Only draw non-zero foreground tiles
                    p.image(tiles[tileIndex],
                            j * tileWidth * scaleFactor,
                            i * tileHeight * scaleFactor,
                            tileWidth * scaleFactor,
                            tileHeight * scaleFactor);
                }
            }
        }
    }

    public boolean checkCollision(float x, float y, float width, float height) {
        // Get the tiles that the object's collision box intersects with
        int startTileX = (int)(x / (tileWidth * scaleFactor));
        int startTileY = (int)(y / (tileHeight * scaleFactor));
        int endTileX = (int)((x + width) / (tileWidth * scaleFactor));
        int endTileY = (int)((y + height) / (tileHeight * scaleFactor));

        // Check if any of those tiles are solid
        for (int tileY = startTileY; tileY <= endTileY; tileY++) {
            for (int tileX = startTileX; tileX <= endTileX; tileX++) {
                // Check map bounds
                if (tileY >= 0 && tileY < backgroundLayer.length &&
                        tileX >= 0 && tileX < backgroundLayer[0].length) {

                    // Check collision with background tiles
                    if (contains(solidTiles, backgroundLayer[tileY][tileX])) {
                        return true; // Collision detected
                    }

                    // Check collision with foreground tiles
                    if (contains(solidForegroundTiles, foregroundLayer[tileY][tileX])) {
                        return true; // Collision detected
                    }
                }
            }
        }

        return false; // No collision
    }

    // Helper function for checking if a value is in an array
    private boolean contains(int[] array, int value) {
        for (int element : array) {
            if (element == value) {
                return true;
            }
        }
        return false;
    }

    // Methods for editor to modify the map
    public void setBackgroundTile(int row, int col, int tileIndex) {
        if (row >= 0 && row < backgroundLayer.length && col >= 0 && col < backgroundLayer[0].length) {
            backgroundLayer[row][col] = tileIndex;
        }
    }

    public void setForegroundTile(int row, int col, int tileIndex) {
        if (row >= 0 && row < foregroundLayer.length && col >= 0 && col < foregroundLayer[0].length) {
            foregroundLayer[row][col] = tileIndex;
        }
    }

    // Getters
    public int getMapWidth() {
        return backgroundLayer[0].length;
    }

    public int getMapHeight() {
        return backgroundLayer.length;
    }

    public int getWidthInPixels() {
        return backgroundLayer[0].length * tileWidth * scaleFactor;
    }

    public int getHeightInPixels() {
        return backgroundLayer.length * tileHeight * scaleFactor;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public PImage[] getTiles() {
        return tiles;
    }

    public int getTileCount() {
        return tiles.length;
    }

    public int getBackgroundTile(int row, int col) {
        return backgroundLayer[row][col];
    }

    public int getForegroundTile(int row, int col) {
        return foregroundLayer[row][col];
    }

    public int[] getSolidTiles() {
        return solidTiles;
    }
    public int[] getSolidForegroundTiles() {
        return solidForegroundTiles;
    }
}