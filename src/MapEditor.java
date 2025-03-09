import processing.core.PApplet;
import processing.core.PImage;

public class MapEditor {
    private PApplet p;
    private GameMap gameMap;
    private int screenWidth;
    private int screenHeight;
    private int scaleFactor;

    // Map and tile data
    private int[][] map;
    private int[][] foregroundMap;
    private PImage[] images;
    private int tileW;
    private int tileH;

    // Editor variables
    private int selectedTile = 0;
    private boolean editingForeground = false;
    private int editorPanelWidth = 200;
    private int editorPanelHeight = 800;
    private int editMapWidth;
    private int editMapHeight;
    private boolean showGrid = true;
    private boolean showCollisions = false;

    // Editor camera/scroll
    private int editorCameraX = 0;
    private int editorCameraY = 0;
    private int editorScrollSpeed = 20;

    // Solid tiles
    private int[] solidTiles;
    private int[] solidForegroundTiles;

    // Constructor modified to match MyGame's call
    public MapEditor(PApplet p, GameMap gameMap, int screenWidth, int screenHeight, int scaleFactor) {
        this.p = p;
        this.gameMap = gameMap;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.scaleFactor = scaleFactor;

        // Use existing methods from GameMap
        this.images = gameMap.getTiles();
        this.map = new int[gameMap.getMapHeight()][gameMap.getMapWidth()];
        this.foregroundMap = new int[gameMap.getMapHeight()][gameMap.getMapWidth()];

        // Copy existing map data
        for (int i = 0; i < gameMap.getMapHeight(); i++) {
            for (int j = 0; j < gameMap.getMapWidth(); j++) {
                this.map[i][j] = gameMap.getBackgroundTile(i, j);
                this.foregroundMap[i][j] = gameMap.getForegroundTile(i, j);
            }
        }

        this.tileW = gameMap.getTileWidth();
        this.tileH = gameMap.getTileHeight();
        this.solidTiles = gameMap.getSolidTiles();
        this.solidForegroundTiles = gameMap.getSolidTiles();

        // Set edit map dimensions
        this.editMapWidth = map[0].length;
        this.editMapHeight = map.length;
    }
    // Add this method to your MapEditor class
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

    // Draw method to match MyGame's call
    public void draw() {
        handleEditorScroll();
        handleMousePress();
        drawEditorMap();
        drawEditorPanel();
    }

    // Updated handleEditorScroll method with improved camera control
    public void handleEditorScroll() {
        // Scroll with arrow keys and IJKL alternative keys
        if (p.keyPressed) {
            if (p.keyCode == PApplet.UP || p.key == 'w' || p.key == 'W') {
                editorCameraY -= editorScrollSpeed;
            }
            if (p.keyCode == PApplet.DOWN || p.key == 's' || p.key == 'S') {
                editorCameraY += editorScrollSpeed;
            }
            if (p.keyCode == PApplet.LEFT || p.key == 'a' || p.key == 'A') {
                editorCameraX -= editorScrollSpeed;
            }
            if (p.keyCode == PApplet.RIGHT || p.key == 'd' || p.key == 'D') {
                editorCameraX += editorScrollSpeed;
            }
        }

        // Constrain camera to map bounds
        int mapPixelWidth = editMapWidth * tileW * scaleFactor;
        int mapPixelHeight = editMapHeight * tileH * scaleFactor;

        editorCameraX = p.constrain(editorCameraX, 0,
                Math.max(0, mapPixelWidth - (screenWidth - editorPanelWidth)));
        editorCameraY = p.constrain(editorCameraY, 0,
                Math.max(0, mapPixelHeight - screenHeight));
    }

    // Updated drawEditorMap method with camera offset
    public void drawEditorMap() {
        // Push matrix to handle camera translation
        p.pushMatrix();
        p.translate(-editorCameraX, -editorCameraY);

        // Draw background tiles
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                p.image(images[map[i][j]],
                        j * tileW * scaleFactor,
                        i * tileH * scaleFactor,
                        tileW * scaleFactor,
                        tileH * scaleFactor);

                // Draw grid if enabled
                if (showGrid) {
                    p.noFill();
                    p.stroke(200, 200, 200, 128);
                    p.rect(j * tileW * scaleFactor, i * tileH * scaleFactor,
                            tileW * scaleFactor, tileH * scaleFactor);
                }

                // Highlight solid tiles if showing collisions
                if (showCollisions && contains(solidTiles, map[i][j])) {
                    p.fill(255, 0, 0, 50);
                    p.noStroke();
                    p.rect(j * tileW * scaleFactor, i * tileH * scaleFactor,
                            tileW * scaleFactor, tileH * scaleFactor);
                }
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

                    // Highlight solid foreground tiles if showing collisions
                    if (showCollisions && contains(solidForegroundTiles, foregroundMap[i][j])) {
                        p.fill(255, 0, 0, 50);
                        p.noStroke();
                        p.rect(j * tileW * scaleFactor, i * tileH * scaleFactor,
                                tileW * scaleFactor, tileH * scaleFactor);
                    }
                }
            }
        }

        // Pop matrix to reset translation
        p.popMatrix();

        // Highlight tile under mouse if within map bounds
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

    // Updated handleMousePress method with improved tile selection
    public void handleMousePress() {
        // Handle clicks on the map area
        if (p.mouseX < p.width - editorPanelWidth) {
            // Adjust tile calculation to account for camera offset
            int tileX = (int)((p.mouseX + editorCameraX) / (tileW * scaleFactor));
            int tileY = (int)((p.mouseY + editorCameraY) / (tileH * scaleFactor));

            // Make sure we're within map bounds
            if (tileX >= 0 && tileX < map[0].length && tileY >= 0 && tileY < map.length) {
                if (p.mouseButton == PApplet.LEFT) {
                    // Left click - place tile
                    if (editingForeground) {
                        foregroundMap[tileY][tileX] = selectedTile;
                    } else {
                        map[tileY][tileX] = selectedTile;
                    }
                } else if (p.mouseButton == PApplet.RIGHT) {
                    // Right click - erase tile (set to 0)
                    if (editingForeground) {
                        foregroundMap[tileY][tileX] = 0;
                    } else {
                        map[tileY][tileX] = 0;
                    }
                }
            }
        } else {
            // Existing panel click handling (no changes needed)
            handlePanelClicks();
        }
    }


    // Extracted panel click handling for clarity
    private void handlePanelClicks() {
        // Only handle tile selection
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

    // Key pressed method
    public void keyPressed() {
        // Toggle grid
        if (p.key == 'g' || p.key == 'G') {
            showGrid = !showGrid;
        }
        // Toggle collisions
        else if (p.key == 'c' || p.key == 'C') {
            showCollisions = !showCollisions;
        }
        // Toggle foreground/background layer
        else if (p.key == 'f' || p.key == 'F') {
            editingForeground = !editingForeground;
        }
        // Save map (changed from 'S' to 'M')
        else if (p.key == 'm' || p.key == 'M') {
            saveMapToJSON("map.json");
        }
    }

    // Existing methods for mouse and key interactions remain the same
    public void mousePressed(int mouseX, int mouseY, int mouseButton) {
        // Set processing mouse variables
        p.mouseX = mouseX;
        p.mouseY = mouseY;
        p.mouseButton = mouseButton;

        handleMousePress();
    }

    // Rest of the methods (saveMapToJSON, createNewMap, updateGameMap, etc.) remain unchanged
    private void updateGameMap() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                gameMap.setBackgroundTile(i, j, map[i][j]);
                gameMap.setForegroundTile(i, j, foregroundMap[i][j]);
            }
        }
    }

    public void saveMapToJSON(String filename) {
        updateGameMap();
        gameMap.saveMapToJSON(filename);
    }

    // Draws the editor panel (implementation remains the same as before)
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

        // Show current editing layer without buttons
        p.fill(255);
        p.textAlign(PApplet.CENTER);
        p.textSize(16);
        p.text("Current Layer: " + (editingForeground ? "Foreground" : "Background"), p.width - editorPanelWidth/2, 65);
        p.textSize(14);
        p.text("Press [F] to toggle layers", p.width - editorPanelWidth/2, 85);

        // Draw tileset title
        p.fill(255);
        p.textAlign(PApplet.CENTER);
        p.textSize(16);
        p.text("Tileset", p.width - editorPanelWidth/2, 110);

        // Draw all available tiles in a grid with enhanced highlighting
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
                // Enhanced highlighting for selected tile
                p.fill(255, 220, 50);  // Brighter yellow
                p.rect(x - 3, y - 3, tileDisplaySize + 6, tileDisplaySize + 6);  // Larger highlight area

                // Add a border
                p.stroke(255);
                p.strokeWeight(2);
                p.noFill();
                p.rect(x, y, tileDisplaySize, tileDisplaySize);
                p.noStroke();
                p.strokeWeight(1);

                p.fill(80); // Darker background under the tile
            } else {
                p.fill(100);
            }
            p.rect(x, y, tileDisplaySize, tileDisplaySize);

            // Draw tile
            p.image(images[i], x, y, tileDisplaySize, tileDisplaySize);

            // Draw tile index
            p.fill(255);
            p.textSize(10);
            p.textAlign(PApplet.LEFT);
            p.text(i, x + 2, y + tileDisplaySize - 2);
        }

        // Draw controls section - text only, no buttons
        int controlsY = startY + (images.length / tilesPerRow + 1) * (tileDisplaySize + 8);

        p.fill(255);
        p.textAlign(PApplet.CENTER);
        p.textSize(16);
        p.text("Controls", p.width - editorPanelWidth/2, controlsY);

        // List of controls as text only
        p.textAlign(PApplet.LEFT);
        p.textSize(14);
        int textY = controlsY + 25;
        int lineHeight = 20;

        p.text("[G] Toggle Grid " + (showGrid ? "(ON)" : "(OFF)"), p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;

        p.text("[C] Show Collisions " + (showCollisions ? "(ON)" : "(OFF)"), p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;

        // Change save to a different key - using 'M' for Map Save
        p.text("[M] Save Map", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;

        p.text("[W,A,S,D] Move Camera", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;

        p.text("[F] Toggle Layers", p.width - editorPanelWidth + 20, textY);
        textY += lineHeight;

        p.text("[E] Exit Editor", p.width - editorPanelWidth + 20, textY);
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
    // Getters and setters (remain unchanged)
    public boolean isEditingForeground() {
        return editingForeground;
    }

    public void toggleEditingForeground() {
        editingForeground = !editingForeground;
    }

    public int getSelectedTile() {
        return selectedTile;
    }

    public void setSelectedTile(int tile) {
        selectedTile = tile;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void toggleShowGrid() {
        showGrid = !showGrid;
    }

    public boolean isShowCollisions() {
        return showCollisions;
    }

    public void toggleShowCollisions() {
        showCollisions = !showCollisions;
    }

    public int[][] getMap() {
        return map;
    }

    public int[][] getForegroundMap() {
        return foregroundMap;
    }
}