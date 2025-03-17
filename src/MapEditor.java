import processing.core.PApplet;
import processing.core.PImage;

/**
 * Map editor for creating and modifying game maps.
 * Supports background and foreground layers, tile selection, and map saving.
 */
public class MapEditor {
    private PApplet p;
    private GameMap gameMap;
    private int screenWidth, screenHeight, scaleFactor;

    // Map and tile data
    private int[][] map;
    private int[][] foregroundMap;
    private PImage[] images;
    private int tileW, tileH;
    private int[] solidTiles;

    // Editor variables
    private int selectedTile = 0;
    private boolean editingForeground = false;
    private int editorPanelWidth = 200;
    private int editorCameraX = 0;
    private int editorCameraY = 0;
    private int editorScrollSpeed = 20;

    /**
     * Creates a new map editor instance.
     */
    public MapEditor(PApplet p, GameMap gameMap, int screenWidth, int screenHeight, int scaleFactor) {
        this.p = p;
        this.gameMap = gameMap;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scaleFactor = scaleFactor;

        // Get map data from GameMap
        this.images = gameMap.getTiles();
        this.map = new int[gameMap.getMapHeight()][gameMap.getMapWidth()];
        this.foregroundMap = new int[gameMap.getMapHeight()][gameMap.getMapWidth()];
        this.tileW = gameMap.getTileWidth();
        this.tileH = gameMap.getTileHeight();
        this.solidTiles = gameMap.getSolidTiles();

        // Copy existing map data
        for (int i = 0; i < gameMap.getMapHeight(); i++) {
            for (int j = 0; j < gameMap.getMapWidth(); j++) {
                this.map[i][j] = gameMap.getBackgroundTile(i, j);
                this.foregroundMap[i][j] = gameMap.getForegroundTile(i, j);
            }
        }
    }

    /**
     * Updates editor's internal data from the game map.
     */
    public void updateFromGameMap(GameMap gameMap) {
        // Update internal map copies
        for (int i = 0; i < gameMap.getMapHeight(); i++) {
            for (int j = 0; j < gameMap.getMapWidth(); j++) {
                this.map[i][j] = gameMap.getBackgroundTile(i, j);
                this.foregroundMap[i][j] = gameMap.getForegroundTile(i, j);
            }
        }

        // Reset the camera position
        this.editorCameraX = 0;
        this.editorCameraY = 0;
    }

    /**
     * Main draw method for the editor.
     */
    public void draw() {
        handleEditorScroll();
        handleMousePress();
        drawEditorMap();
        drawEditorPanel();
    }

    /**
     * Handles camera scrolling with WASD keys.
     */
    public void handleEditorScroll() {
        // Scroll with WASD keys
        if (p.keyPressed) {
            if (p.key == 'w' || p.key == 'W') editorCameraY -= editorScrollSpeed;
            if (p.key == 's' || p.key == 'S') editorCameraY += editorScrollSpeed;
            if (p.key == 'a' || p.key == 'A') editorCameraX -= editorScrollSpeed;
            if (p.key == 'd' || p.key == 'D') editorCameraX += editorScrollSpeed;
        }

        // Constrain camera to map bounds
        int mapPixelWidth = map[0].length * tileW * scaleFactor;
        int mapPixelHeight = map.length * tileH * scaleFactor;

        editorCameraX = p.constrain(editorCameraX, 0, Math.max(0, mapPixelWidth - (screenWidth - editorPanelWidth)));
        editorCameraY = p.constrain(editorCameraY, 0, Math.max(0, mapPixelHeight - screenHeight));
    }

    /**
     * Draws the map with both background and foreground layers.
     */
    public void drawEditorMap() {
        // Translate for camera movement
        p.pushMatrix();
        p.translate(-editorCameraX, -editorCameraY);

        // Draw background tiles
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                // Draw tile
                p.image(images[map[i][j]],
                        j * tileW * scaleFactor,
                        i * tileH * scaleFactor,
                        tileW * scaleFactor,
                        tileH * scaleFactor);

                // Always draw grid
                p.noFill();
                p.stroke(200, 200, 200, 128);
                p.rect(j * tileW * scaleFactor, i * tileH * scaleFactor,
                        tileW * scaleFactor, tileH * scaleFactor);
            }
        }

        // Draw foreground tiles
        for (int i = 0; i < foregroundMap.length; i++) {
            for (int j = 0; j < foregroundMap[i].length; j++) {
                int tileIndex = foregroundMap[i][j];
                if (tileIndex != 0) { // Only draw non-zero foreground tiles
                    p.image(images[tileIndex],
                            j * tileW * scaleFactor,
                            i * tileH * scaleFactor,
                            tileW * scaleFactor,
                            tileH * scaleFactor);
                }
            }
        }

        p.popMatrix();

        // Highlight tile under mouse
        if (p.mouseX < p.width - editorPanelWidth) {
            int tileX = (int)((p.mouseX + editorCameraX) / (tileW * scaleFactor));
            int tileY = (int)((p.mouseY + editorCameraY) / (tileH * scaleFactor));

            if (tileX >= 0 && tileX < map[0].length && tileY >= 0 && tileY < map.length) {
                p.noFill();
                p.stroke(255, 255, 0);
                p.strokeWeight(2);
                p.rect(tileX * tileW * scaleFactor - editorCameraX,
                        tileY * tileH * scaleFactor - editorCameraY,
                        tileW * scaleFactor,
                        tileH * scaleFactor);
                p.strokeWeight(1);
            }
        }
    }

    /**
     * Handles mouse press events for placing and erasing tiles.
     */
    public void handleMousePress() {
        // Place tiles on the map
        if (p.mouseX < p.width - editorPanelWidth) {
            int tileX = (int)((p.mouseX + editorCameraX) / (tileW * scaleFactor));
            int tileY = (int)((p.mouseY + editorCameraY) / (tileH * scaleFactor));

            if (tileX >= 0 && tileX < map[0].length && tileY >= 0 && tileY < map.length) {
                if (p.mouseButton == PApplet.LEFT) {
                    // Left click - place tile
                    if (editingForeground) {
                        foregroundMap[tileY][tileX] = selectedTile;
                    } else {
                        map[tileY][tileX] = selectedTile;
                    }
                } else if (p.mouseButton == PApplet.RIGHT) {
                    // Right click - erase tile
                    if (editingForeground) {
                        foregroundMap[tileY][tileX] = 0;
                    } else {
                        map[tileY][tileX] = 0;
                    }
                }
            }
        } else {
            // Handle clicks in the editor panel
            selectTileFromPanel();
        }
    }

    /**
     * Handles tile selection from the side panel.
     */
    private void selectTileFromPanel() {
        int tilesPerRow = 4;
        int tileDisplaySize = 40;
        int startX = p.width - editorPanelWidth + 10;
        int startY = 130;

        for (int i = 0; i < images.length; i++) {
            int tileX = i % tilesPerRow;
            int tileY = i / tilesPerRow;

            int x = startX + tileX * (tileDisplaySize + 10);
            int y = startY + tileY * (tileDisplaySize + 10);

            if (p.mouseX >= x && p.mouseX <= x + tileDisplaySize &&
                    p.mouseY >= y && p.mouseY <= y + tileDisplaySize) {
                selectedTile = i;
                break;
            }
        }
    }

    /**
     * Handles keyboard input for editor functions.
     */
    public void keyPressed() {
        // Toggle foreground/background layer
        if (p.key == 'f' || p.key == 'F') {
            editingForeground = !editingForeground;
        }
        // Save map
        else if (p.key == 'm' || p.key == 'M') {
            saveMapToJSON("data/map.json");
        }
    }

    /**
     * Handles mouse pressed events from external sources.
     */
    public void mousePressed(int mouseX, int mouseY, int mouseButton) {
        // Set processing mouse variables
        p.mouseX = mouseX;
        p.mouseY = mouseY;
        p.mouseButton = mouseButton;
        handleMousePress();
    }

    /**
     * Saves the current map to a JSON file.
     */
    public void saveMapToJSON(String filename) {
        // Update GameMap with current editor data
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                gameMap.setBackgroundTile(i, j, map[i][j]);
                gameMap.setForegroundTile(i, j, foregroundMap[i][j]);
            }
        }
        gameMap.saveMapToJSON(filename);
    }

    /**
     * Draws the editor panel UI with tile selection and controls.
     */
    public void drawEditorPanel() {
        // Draw panel background
        p.fill(50, 50, 60);
        p.noStroke();
        p.rect(p.width - editorPanelWidth, 0, editorPanelWidth, p.height);

        // Draw panel title
        p.fill(255);
        p.textAlign(PApplet.CENTER);
        p.textSize(18);
        p.text("TILE EDITOR", p.width - editorPanelWidth/2, 30);

        // Show current editing layer
        p.textSize(16);
        p.text("Layer: " + (editingForeground ? "Foreground" : "Background"), p.width - editorPanelWidth/2, 65);
        p.textSize(14);
        p.text("Press [F] to toggle layers", p.width - editorPanelWidth/2, 85);

        // Draw tileset
        p.textSize(16);
        p.text("Tileset", p.width - editorPanelWidth/2, 110);

        // Draw tile options
        int tilesPerRow = 4;
        int tileDisplaySize = 40;
        int startX = p.width - editorPanelWidth + 10;
        int startY = 130;

        for (int i = 0; i < images.length; i++) {
            int tileX = i % tilesPerRow;
            int tileY = i / tilesPerRow;

            int x = startX + tileX * (tileDisplaySize + 10);
            int y = startY + tileY * (tileDisplaySize + 10);

            // Draw tile background
            if (selectedTile == i) {
                p.fill(255, 220, 50);
                p.rect(x - 3, y - 3, tileDisplaySize + 6, tileDisplaySize + 6);
            } else {
                p.fill(100);
                p.rect(x, y, tileDisplaySize, tileDisplaySize);
            }

            // Draw tile
            p.image(images[i], x, y, tileDisplaySize, tileDisplaySize);

            // Draw tile number
            p.fill(255);
            p.textSize(10);
            p.textAlign(PApplet.LEFT);
            p.text(i, x + 2, y + tileDisplaySize - 2);
        }

        // Draw controls section
        int controlsY = startY + (images.length / tilesPerRow + 1) * (tileDisplaySize + 8);
        p.fill(255);
        p.textAlign(PApplet.CENTER);
        p.textSize(16);
        p.text("Controls", p.width - editorPanelWidth/2, controlsY);

        // List controls
        p.textAlign(PApplet.LEFT);
        p.textSize(14);
        int textY = controlsY + 25;
        int lineHeight = 20;

        p.text("[F] Toggle Layers", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;
        p.text("[M] Save Map", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;
        p.text("[W,A,S,D] Move Camera", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;
        p.text("Left Click: Place Tile", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;
        p.text("Right Click: Erase Tile", p.width - editorPanelWidth + 20, textY);
    }

    /**
     * Toggles between editing foreground and background layers.
     */
    public void toggleEditingForeground() {
        editingForeground = !editingForeground;
    }
}