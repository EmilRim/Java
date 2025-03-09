import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;

import processing.data.JSONArray;
import processing.data.JSONObject;

public class MyGame extends PApplet {
    // Game components
    private Player player;
    private GameMap gameMap;
    private MapEditor editor;
    private ArrayList<Enemy> enemies; // List of enemies

    // Game state
    private boolean inEditorMode = false;

    // Game settings
    private int scaleFactor = 4;
    private int screenWidth = 800;
    private int screenHeight = 800;
    private int cameraX = 0;
    private int cameraY = 0;

    private boolean gameWon = false; // Track if the player has won
    private boolean showLevelSelect = false;
    private int selectedLevel = 1; // Default to level 1




    // Override settings method to set window size
    @Override
    public void settings() {
        size(1000, 800); // Increased width to accommodate editor panel
    }

    // Override setup method to initialize game components
    @Override
    public void setup() {
        background(125, 175, 225);

        // Load tileset
        PImage tileset = loadImage("spritesheet.png");

        // Setup game map
        gameMap = new GameMap(this, tileset, 8, 5, scaleFactor);
        gameMap.loadMapFromJSON("map01.json");

        // Setup player
        PImage playerImg = loadImage("player.png");
        player = new Player(this, playerImg, 200, 200, 11.0f, 4, 4, scaleFactor);

        // Load enemy images
        PImage enemyImg = loadImage("MouseIdle.png");

        // Load enemy death animation spritesheet
        PImage deathSpritesheet = loadImage("MouseDie.png"); // Make sure this path is correct and the image exists!

        // Initialize the enemy list
        enemies = new ArrayList<>();

        // Load enemies from JSON
        JSONArray enemiesArray = gameMap.getEnemiesFromJSON("map01.json");

        for (int i = 0; i < enemiesArray.size(); i++) {
            JSONObject enemyData = enemiesArray.getJSONObject(i);

            float x = enemyData.getFloat("x");
            float y = enemyData.getFloat("y");
            float speed = enemyData.getFloat("speed");
            int minX = enemyData.getInt("minX");
            int maxX = enemyData.getInt("maxX");

            // Create new enemy with death animation spritesheet
            enemies.add(new Enemy(this, enemyImg, deathSpritesheet, x, y, speed, 1, 6, minX, maxX, scaleFactor));
        }

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
        if (gameWon || showLevelSelect) return; // Stop updating when game is won or in level selection

        player.update(gameMap, enemies);

        // Check for collisions and update enemies
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);

            // Update the enemy first (this will advance animation frames)
            enemy.update();

            // Check if the player collides with the enemy
            if (!enemy.isDying) { // Only check collision if enemy is not already dying
                enemy.checkCollision(player.getX(), player.getY(),
                        player.getTileWidth() * scaleFactor,
                        player.getTileHeight() * scaleFactor);
            }

            // Remove enemy only if death animation is complete
            if (enemy.isDeathAnimationComplete()) {
                enemies.remove(i);
            }
        }

        // Check if all enemies are gone
        if (enemies.isEmpty()) {
            gameWon = true; // Player has won!
        }
    }





    private void drawGame() {
        if (gameWon) {
            drawWinningScreen();
            return;
        }
        if (showLevelSelect) {
            drawLevelSelection();
            return;
        }

        // Update camera to follow player
        cameraX = constrain((int) player.getX() - width / 2, 0, gameMap.getWidthInPixels() - width);
        cameraY = constrain((int) player.getY() - height / 2, 0, gameMap.getHeightInPixels() - height);

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
        rect(0, 0, width, height); // Dark overlay

        fill(255);
        textSize(50);
        textAlign(CENTER, CENTER);
        text("YOU WIN!", width / 2, height / 3);

        // Draw "Next" button
        fill(0, 255, 0);
        rect(width / 2 - 100, height / 2, 200, 50);
        fill(0);
        textSize(25);
        text("NEXT", width / 2, height / 2 + 25);
    }
    private void drawLevelSelection() {
        fill(0, 150);
        rect(0, 0, width, height); // Dark overlay

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
            return;
        }

        if (gameWon) {
            if (mouseX > width / 2 - 100 && mouseX < width / 2 + 100 &&
                    mouseY > height / 2 && mouseY < height / 2 + 50) {
                println("Next button clicked!");
                gameWon = false;
                showLevelSelect = true;
            }
        } else if (showLevelSelect) {
            // Your existing level selection code
            for (int i = 1; i <= 3; i++) {
                int buttonX = width / 2 - 150 + (i - 1) * 100;
                int buttonY = height / 2;

                if (mouseX > buttonX && mouseX < buttonX + 80 &&
                        mouseY > buttonY && mouseY < buttonY + 50) {
                    selectedLevel = i;
                    println("Level " + i + " selected! Restarting game...");
                    restartGame();
                }
            }
        }
    }

    private void restartGame() {
        showLevelSelect = false;
        gameWon = false;

        // Load the correct level map
        String mapFile = "map0" + selectedLevel + ".json";
        gameMap.loadMapFromJSON(mapFile);

        // Update the editor with the new map data
        editor = new MapEditor(this, gameMap, width, height, scaleFactor);

        // Reset player position
        player.setPosition(200, 200);


// Update the editor with the new map
        editor.updateFromGameMap(gameMap);

        // Reload enemies
        enemies.clear();
        JSONArray enemiesArray = gameMap.getEnemiesFromJSON(mapFile);
        for (int i = 0; i < enemiesArray.size(); i++) {
            JSONObject enemyData = enemiesArray.getJSONObject(i);

            float x = enemyData.getFloat("x");
            float y = enemyData.getFloat("y");
            float speed = enemyData.getFloat("speed");
            int minX = enemyData.getInt("minX");
            int maxX = enemyData.getInt("maxX");

            PImage enemyImg = loadImage("MouseIdle.png");
            PImage deathSpritesheet = loadImage("MouseDie.png");
            enemies.add(new Enemy(this, enemyImg, deathSpritesheet, x, y, speed, 1, 6, minX, maxX, scaleFactor));
        }
    }


    // Main method to run the sketch
    public static void main(String[] args) {
        PApplet.main("MyGame");
    }
}
