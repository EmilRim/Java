import processing.core.PApplet;
import processing.core.PImage;
import java.util.ArrayList;

/**
 * Player class representing the user-controlled character in the game.
 * Handles movement, animation, collision detection, and input processing.
 */
public class Player {
    // Core references
    private PApplet p;
    private PImage[] sprites;

    // Position and movement
    private float x, y, speed;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Animation properties
    private int tileWidth, tileHeight;
    private int direction = 0;       // 0 = down, 1 = left, 2 = up, 3 = right
    private int frame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 2;
    private int scaleFactor;

    // Collision box properties
    private int frontBackOffsetX = 40;
    private int frontBackOffsetY = 30;
    private int frontBackWidth, frontBackHeight;

    private int sideOffsetX = 20;
    private int sideOffsetY = 30;
    private int sideWidth, sideHeight;

    /**
     * Creates a new player with the specified spritesheet and properties.
     */
    public Player(PApplet p, PImage spritesheet, float startX, float startY, float speed,
                  int cols, int rows, int scaleFactor) {
        this.p = p;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.scaleFactor = scaleFactor;

        // Extract sprite frames from spritesheet
        this.tileWidth = spritesheet.width / cols;
        this.tileHeight = spritesheet.height / rows;
        this.sprites = new PImage[cols * rows];

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                sprites[i + j * cols] = spritesheet.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }

        // Set collision box dimensions
        frontBackWidth = tileWidth * scaleFactor - 2 * frontBackOffsetX;
        frontBackHeight = tileHeight * scaleFactor - 2 * frontBackOffsetY;
        sideWidth = tileWidth * scaleFactor - 2 * sideOffsetX;
        sideHeight = tileHeight * scaleFactor - 2 * sideOffsetY;
    }

    /**
     * Updates the player's state for the current frame.
     */
    public void update(GameMap map, ArrayList<Enemy> enemies) {
        updateAnimation();
        updatePosition(map, enemies);
    }

    /**
     * Updates the player's animation frame based on movement direction.
     */
    private void updateAnimation() {
        animationCounter++;

        if (animationCounter >= animationSpeed) {
            animationCounter = 0;

            // Update animation frame based on direction
            if (movingUp) {
                direction = 2;
                if (frame < 8 || frame > 11) frame = 8;
                frame = 8 + (frame + 1 - 8) % 4;
            } else if (movingDown) {
                direction = 0;
                if (frame < 0 || frame > 3) frame = 0;
                frame = (frame + 1) % 4;
            } else if (movingLeft) {
                direction = 1;
                if (frame < 4 || frame > 7) frame = 4;
                frame = 4 + (frame + 1 - 4) % 4;
            } else if (movingRight) {
                direction = 3;
                if (frame < 12 || frame > 15) frame = 12;
                frame = 12 + (frame + 1 - 12) % 4;
            }
        }
    }

    /**
     * Updates the player's position based on movement flags and collisions.
     */
    private void updatePosition(GameMap map, ArrayList<Enemy> enemies) {
        float newX = x;
        float newY = y;

        // Calculate new position based on movement flags
        if (movingUp) newY -= speed;
        if (movingDown) newY += speed;
        if (movingLeft) newX -= speed;
        if (movingRight) newX += speed;

        // Apply movement if no collision
        if (newX != x && !checkCollision(newX, y, map, enemies)) {
            x = newX;
        }
        if (newY != y && !checkCollision(x, newY, map, enemies)) {
            y = newY;
        }
    }

    /**
     * Checks for collision with map tiles and enemies.
     */
    private boolean checkCollision(float testX, float testY, GameMap map, ArrayList<Enemy> enemies) {
        // Select the appropriate collision box based on direction
        float offsetX, offsetY, boxWidth, boxHeight;

        if (direction == 1 || direction == 3) {
            // Side view (left/right)
            offsetX = sideOffsetX;
            offsetY = sideOffsetY;
            boxWidth = sideWidth;
            boxHeight = sideHeight;
        } else {
            // Front/back view (down/up)
            offsetX = frontBackOffsetX;
            offsetY = frontBackOffsetY;
            boxWidth = frontBackWidth;
            boxHeight = frontBackHeight;
        }

        // Check collision with map tiles
        if (map.checkCollision(testX + offsetX, testY + offsetY, boxWidth, boxHeight)) {
            return true;
        }

        // Check collision with enemies
        for (Enemy enemy : enemies) {
            if (enemy.checkCollision(testX + offsetX, testY + offsetY, boxWidth, boxHeight)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Draws the player sprite at the current position.
     */
    public void draw() {
        p.image(sprites[frame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
        // Debug: uncomment to show collision box
        // drawCollisionBox();
    }

    /**
     * Draws the player's collision box for debugging purposes.
     */
    private void drawCollisionBox() {
        float offsetX, offsetY, boxWidth, boxHeight;

        if (direction == 1 || direction == 3) {
            // Side view collision box
            offsetX = sideOffsetX;
            offsetY = sideOffsetY;
            boxWidth = sideWidth;
            boxHeight = sideHeight;
            p.fill(255, 0, 0, 100); // Red for side view
        } else {
            // Front/back view collision box
            offsetX = frontBackOffsetX;
            offsetY = frontBackOffsetY;
            boxWidth = frontBackWidth;
            boxHeight = frontBackHeight;
            p.fill(0, 0, 255, 100); // Blue for front/back view
        }

        p.noStroke();
        p.rect(x + offsetX, y + offsetY, boxWidth, boxHeight);
        p.noFill();
        p.stroke(0);
    }

    /**
     * Handles key press events for player movement.
     */
    public void keyPressed(char key) {
        if (key == 'w' && !movingRight && !movingLeft && !movingDown) {
            movingUp = true;
        } else if (key == 's' && !movingRight && !movingLeft && !movingUp) {
            movingDown = true;
        } else if (key == 'a' && !movingUp && !movingDown && !movingRight) {
            movingLeft = true;
        } else if (key == 'd' && !movingUp && !movingDown && !movingLeft) {
            movingRight = true;
        }
    }

    /**
     * Handles key release events for player movement.
     */
    public void keyReleased(char key) {
        if (key == 'w') {
            movingUp = false;
        } else if (key == 's') {
            movingDown = false;
        } else if (key == 'a') {
            movingLeft = false;
        } else if (key == 'd') {
            movingRight = false;
        }
    }

    // Getters and setters
    public void setPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }
}