import processing.core.PApplet;
import processing.core.PImage;

public class MyGame extends PApplet {
    // Game components
    private Player player;
    private Enemy enemy;
    private GameMap gameMap;
    private MapEditor editor;

    // Game state
    private boolean inEditorMode = false;

    // Game settings
    private int scaleFactor = 4;
    private int screenWidth = 800;
    private int screenHeight = 800;
    private int cameraX = 0;
    private int cameraY = 0;

    // Override settings method to set window size
    @Override
    public void settings() {
        size(1000, 800); // Increased width to accommodate editor panel
    }

    // Override setup method to initialize game components
    @Override
    public void setup() {
        // Initialize background
        background(125, 175, 225);

        // Load tileset
        PImage tileset = loadImage("spritesheet.png");

        // Setup game map
        gameMap = new GameMap(this, tileset, 8, 5, scaleFactor);
        gameMap.loadMapFromJSON("map.json");

        // Setup player
        PImage playerImg = loadImage("player.png");
        player = new Player(this, playerImg, 200, 200, 11.0f, 4, 4, scaleFactor);

        // Setup enemy
        PImage enemyImg = loadImage("MouseIdle.png");
        enemy = new Enemy(this, enemyImg, 400, 450, 2.0f, 1, 6, 300, 500, scaleFactor);

        // Setup map editor
        editor = new MapEditor(this, gameMap, width, height, scaleFactor);
    }

    // Override draw method to update and render game
    @Override
    public void draw() {
        background(125, 175, 225);

        if (inEditorMode) {
            editor.draw();
        } else {
            updateGame();
            drawGame();
        }
    }

    private void updateGame() {
        player.update(gameMap, enemy);
        enemy.update();
    }

    private void drawGame() {
        // Update camera to follow player
        cameraX = constrain((int)player.getX() - width / 2, 0,
                gameMap.getWidthInPixels() - width);
        cameraY = constrain((int)player.getY() - height / 2, 0,
                gameMap.getHeightInPixels() - height);

        // Apply camera translation
        pushMatrix();
        translate(-cameraX, -cameraY);

        // Draw map background layer
        gameMap.drawBackgroundLayer();

        // Draw player
        player.draw();

        // Draw enemy
        enemy.draw();

        // Draw map foreground layer (over player and enemy)
        gameMap.drawForegroundLayer();

        popMatrix();
    }

    // Override key event methods
    @Override
    public void keyPressed() {
        if (key == 'e' || key == 'E') {
            inEditorMode = !inEditorMode;
            return;
        }

        if (inEditorMode) {
            editor.keyPressed();
        } else {
            player.keyPressed(key);
        }
    }

    @Override
    public void keyReleased() {
        if (!inEditorMode) {
            player.keyReleased(key);
        }
    }

    // Override mouse event method
    @Override
    public void mousePressed() {
        if (inEditorMode) {
            editor.mousePressed(mouseX, mouseY, mouseButton);
        }
    }

    // Main method to run the sketch
    public static void main(String[] args) {
        PApplet.main("MyGame");
    }
}