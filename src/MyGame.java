import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;
import processing.data.JSONArray;
import processing.data.JSONObject;

/**
 * Main game class that manages the game loop, rendering, input handling, and game states
 */
public class MyGame extends PApplet {
    // Game components
    private Player player;
    private GameMap gameMap;
    private MapEditor editor;
    private ArrayList<Enemy> enemies;

    // Game state flags
    private boolean inEditorMode = false;
    private boolean gameWon = false;
    private boolean showLevelSelect = false;
    private int selectedLevel = 1;

    // Display settings
    private int scaleFactor = 4;
    private int cameraX = 0;
    private int cameraY = 0;

    public static void main(String[] args) {
        PApplet.main("MyGame");
    }

    @Override
    public void settings() {
        size(1000, 800);
    }

    /**
     * Initialize the game and load assets
     */
    @Override
    public void setup() {
        background(125, 175, 225);

        // Load game assets
        PImage tileset = loadImage("data/spritesheet.png");
        PImage playerImg = loadImage("data/player.png");
        PImage enemyImg = loadImage("data/MouseIdle.png");
        PImage deathSpritesheet = loadImage("data/MouseDie.png");

        // Initialize game components
        gameMap = new GameMap(this, tileset, 8, 5, scaleFactor);
        gameMap.loadMapFromJSON("data/map01.json");

        player = new Player(this, playerImg, 200, 200, 11.0f, 4, 4, scaleFactor);

        // Initialize enemies from map data
        enemies = new ArrayList<>();
        JSONArray enemiesArray = gameMap.getEnemiesFromJSON("data/map01.json");

        for (int i = 0; i < enemiesArray.size(); i++) {
            JSONObject enemyData = enemiesArray.getJSONObject(i);

            float x = enemyData.getFloat("x");
            float y = enemyData.getFloat("y");
            float speed = enemyData.getFloat("speed");
            int minX = enemyData.getInt("minX");
            int maxX = enemyData.getInt("maxX");

            enemies.add(new Enemy(this, enemyImg, deathSpritesheet, x, y, speed,
                    1, 6, minX, maxX, scaleFactor));
        }

        editor = new MapEditor(this, gameMap, width, height, scaleFactor);
    }

    /**
     * Main game loop
     */
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

    /**
     * Update game state and handle collisions
     */
    private void updateGame() {
        if (gameWon || showLevelSelect) return;

        player.update(gameMap, enemies);

        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            if (!enemy.isDying) {
                enemy.checkCollision(player.getX(), player.getY(),
                        player.getTileWidth() * scaleFactor,
                        player.getTileHeight() * scaleFactor
                );
            }

            if (enemy.isDeathAnimationComplete()) {
                enemies.remove(i);
            }
        }

        // Check win condition
        if (enemies.isEmpty()) {
            gameWon = true;
        }
    }

    /**
     * Render game elements and UI
     */
    private void drawGame() {
        if (gameWon) {
            drawWinningScreen();
            return;
        }
        if (showLevelSelect) {
            drawLevelSelection();
            return;
        }

        // Camera follows player
        cameraX = constrain(
                (int) player.getX() - width / 2,
                0,
                gameMap.getWidthInPixels() - width
        );
        cameraY = constrain(
                (int) player.getY() - height / 2,
                0,
                gameMap.getHeightInPixels() - height
        );

        // Draw game world
        pushMatrix();
        translate(-cameraX, -cameraY);

        gameMap.drawBackgroundLayer();
        player.draw();

        for (Enemy enemy : enemies) {
            enemy.draw();
        }

        gameMap.drawForegroundLayer();

        popMatrix();
    }

    private void drawWinningScreen() {
        fill(0, 150);
        rect(0, 0, width, height);

        fill(255);
        textSize(50);
        textAlign(CENTER, CENTER);
        text("YOU WIN!", width / 2, height / 3);

        fill(0, 255, 0);
        rect(width / 2 - 100, height / 2, 200, 50);
        fill(0);
        textSize(25);
        text("NEXT", width / 2, height / 2 + 25);
    }

    private void drawLevelSelection() {
        fill(0, 150);
        rect(0, 0, width, height);

        fill(255);
        textSize(40);
        textAlign(CENTER, CENTER);
        text("SELECT LEVEL", width / 2, height / 4);

        for (int i = 1; i <= 3; i++) {
            int buttonX = width / 2 - 150 + (i - 1) * 100;
            int buttonY = height / 2;

            fill(200);
            rect(buttonX, buttonY, 80, 50);

            fill(0);
            textSize(25);
            text("Level " + i, buttonX + 40, buttonY + 30);
        }
    }

    /**
     * Handle input events
     */
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

    @Override
    public void mousePressed() {
        if (inEditorMode) {
            editor.mousePressed(mouseX, mouseY, mouseButton);
            return;
        }

        // Handle UI button clicks
        if (gameWon) {
            if (mouseX > width / 2 - 100 && mouseX < width / 2 + 100 &&
                    mouseY > height / 2 && mouseY < height / 2 + 50) {
                gameWon = false;
                showLevelSelect = true;
            }
        }
        else if (showLevelSelect) {
            for (int i = 1; i <= 3; i++) {
                int buttonX = width / 2 - 150 + (i - 1) * 100;
                int buttonY = height / 2;

                if (mouseX > buttonX && mouseX < buttonX + 80 &&
                        mouseY > buttonY && mouseY < buttonY + 50) {
                    selectedLevel = i;
                    restartGame();
                }
            }
        }
    }

    /**
     * Reset the game with a new level
     */
    private void restartGame() {
        showLevelSelect = false;
        gameWon = false;

        String mapFile = "map0" + selectedLevel + ".json";
        gameMap.loadMapFromJSON(mapFile);

        editor = new MapEditor(this, gameMap, width, height, scaleFactor);
        editor.updateFromGameMap(gameMap);

        player.setPosition(200, 200);

        enemies.clear();
        JSONArray enemiesArray = gameMap.getEnemiesFromJSON(mapFile);

        for (int i = 0; i < enemiesArray.size(); i++) {
            JSONObject enemyData = enemiesArray.getJSONObject(i);

            float x = enemyData.getFloat("x");
            float y = enemyData.getFloat("y");
            float speed = enemyData.getFloat("speed");
            int minX = enemyData.getInt("minX");
            int maxX = enemyData.getInt("maxX");

            PImage enemyImg = loadImage("data/MouseIdle.png");
            PImage deathSpritesheet = loadImage("data/MouseDie.png");
            enemies.add(new Enemy(this, enemyImg, deathSpritesheet, x, y, speed,
                    1, 6, minX, maxX, scaleFactor));
        }
    }
}