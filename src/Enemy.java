import processing.core.PApplet;
import processing.core.PImage;

/**
 * Enemy class for handling enemies in the game with movement, animation, and collision detection.
 */
public class Enemy {
    private PApplet p;
    private PImage[] sprites;
    private PImage[] deathSprites;

    // Position and movement
    private float x, y, speed;
    private int direction = 1; // 1 = right, -1 = left
    private int minX, maxX;

    // Sprite information
    private int tileWidth, tileHeight;
    private int scaleFactor;

    // Animation
    private int frame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 10;

    // Collision box
    private int sideOffsetX = 50;
    private int sideOffsetY = 60;
    private int sideWidth, sideHeight;

    // State flags
    private boolean isActive = true;
    public boolean isDying = false;
    private int deathFrame = 0;

    /**
     * Creates a new enemy with specified sprites and movement parameters.
     */
    public Enemy(PApplet p, PImage spritesheet, PImage deathSpritesheet, float startX, float startY,
                 float speed, int cols, int rows, int minX, int maxX, int scaleFactor) {
        this.p = p;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.minX = minX;
        this.maxX = maxX;
        this.scaleFactor = scaleFactor;

        // Regular enemy spritesheet
        this.tileWidth = spritesheet.width / cols;
        this.tileHeight = spritesheet.height / rows;
        this.sprites = new PImage[cols * rows];

        // Extract frames for regular spritesheet
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                sprites[i + j * cols] = spritesheet.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }

        // Death animation spritesheet
        int deathCols = 1;
        int deathRows = 6;
        this.deathSprites = new PImage[deathCols * deathRows];

        // Extract frames for death animation
        for (int j = 0; j < deathRows; j++) {
            for (int i = 0; i < deathCols; i++) {
                deathSprites[i + j * deathCols] = deathSpritesheet.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }

        // Set collision box sizes
        sideWidth = tileWidth * scaleFactor - 2 * sideOffsetX;
        sideHeight = tileHeight * scaleFactor - 2 * sideOffsetY;
    }

    /**
     * Draws the enemy's collision box for debugging.
     */
    private void drawCollisionBox() {
        if (isActive) {
            float offsetX = sideOffsetX;
            float offsetY = sideOffsetY;
            float boxWidth = sideWidth;
            float boxHeight = sideHeight;
            p.fill(255, 0, 0, 100);
            p.noStroke();
            p.rect(x + offsetX, y + offsetY, boxWidth, boxHeight);
            p.noFill();
            p.stroke(0);
        }
    }

    /**
     * Updates the enemy's position and animation state.
     */
    public void update() {
        if (!isActive) return;

        if (isDying) {
            // Handle death animation
            deathFrame++;
            if (deathFrame >= deathSprites.length) {
                isActive = false;
                return;
            }
        } else {
            // Move the enemy
            x += speed * direction;

            // Reverse direction when reaching bounds
            if (x <= minX || x >= maxX) {
                direction *= -1;
            }

            // Animate regular enemy sprites
            animationCounter++;
            if (animationCounter >= animationSpeed) {
                animationCounter = 0;
                frame = (frame + 1) % sprites.length;
            }
        }
    }

    /**
     * Checks if the death animation has completed.
     */
    public boolean isDeathAnimationComplete() {
        return isDying && deathFrame >= deathSprites.length - 1;
    }

    /**
     * Draws the enemy with the appropriate animation frame.
     */
    public void draw() {
        if (isActive) {
            if (isDying) {
                // Draw death animation
                p.image(deathSprites[deathFrame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
            } else {
                // Draw regular animation
                p.image(sprites[frame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
            }

            // Debug: uncomment to show collision box
            // drawCollisionBox();
        }
    }

    /**
     * Checks for collision with the player and starts death animation if collision occurs.
     */
    public boolean checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (isActive && !isDying) {
            if (playerX + playerWidth > x + sideOffsetX &&
                    playerX < x + sideWidth + sideOffsetX &&
                    playerY + playerHeight > y + sideOffsetY &&
                    playerY < y + sideHeight + sideOffsetY) {
                // Collision detected, start death animation
                isDying = true;
                deathFrame = 0;
                return true;
            }
        }
        return false;
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}