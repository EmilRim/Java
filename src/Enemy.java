import processing.core.PApplet;
import processing.core.PImage;


public class Enemy {
    private PApplet p;
    private PImage[] sprites;
    private PImage[] deathSprites;

    // Position and movement
    private float x;
    private float y;
    private float speed;
    private int direction = 1; // 1 = right, -1 = left
    private int minX;
    private int maxX;

    // Sprite information
    private int tileWidth;
    private int tileHeight;
    private int scaleFactor;

    // Animation
    private int frame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 10;

    private int sideOffsetX = 50;
    private int sideOffsetY = 60;
    private int sideWidth;
    private int sideHeight;

    // Flags for enemy state
    private boolean isActive = true; // Is the enemy active or has it died?
    public boolean isDying = false; // Is the enemy in the death animation phase?
    private int deathFrame = 0; // Frame of the death animation

    public Enemy(PApplet p, PImage spritesheet, PImage deathSpritesheet, float startX, float startY,
                 float speed, int cols, int rows, int minX, int maxX, int scaleFactor) {
        this.p = p;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.minX = minX;
        this.maxX = maxX;
        this.scaleFactor = scaleFactor;

        // Regular enemy spritesheet (normal movement)
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
        int deathCols = 1; // Change this based on how many columns the death spritesheet has
        int deathRows = 6; // Change this based on how many rows the death spritesheet has
        this.deathSprites = new PImage[deathCols * deathRows];

        // Calculate death animation frames
        for (int j = 0; j < deathRows; j++) {
            for (int i = 0; i < deathCols; i++) {
                deathSprites[i + j * deathCols] = deathSpritesheet.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }

        // Set collision box sizes
        sideWidth = tileWidth * scaleFactor - 2 * sideOffsetX;
        sideHeight = tileHeight * scaleFactor - 2 * sideOffsetY;
    }

    private void drawCollisionBox() {
        if (isActive) {
            // Draw a collision box around the enemy
            float offsetX = sideOffsetX;
            float offsetY = sideOffsetY;
            float boxWidth = sideWidth;
            float boxHeight = sideHeight;
            p.fill(255, 0, 0, 100); // Red with transparency for side view collision box
            p.noStroke();
            p.rect(x + offsetX, y + offsetY, boxWidth, boxHeight);
            p.noFill();
            p.stroke(0); // Reset stroke after drawing the box
        }
    }

    public void update() {
        if (!isActive) return; // If the enemy is not active, don't update it

        // If the enemy is dying, handle death animation
        if (isDying) {
            deathFrame++;
            if (deathFrame >= deathSprites.length) {
                // End the death animation after it's done
                isActive = false; // Deactivate the enemy
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
                frame = (frame + 1) % sprites.length; // Cycle between frames
            }
        }
    }
    public boolean isDeathAnimationComplete() {
        return isDying && deathFrame >= deathSprites.length - 1;
    }
    public void draw() {
        if (isActive) {
            if (isDying) {
                // Draw death animation if enemy is dying
                p.image(deathSprites[deathFrame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
            } else {
                // Draw regular enemy animation
                p.image(sprites[frame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
            }

            // Draw collision box for debugging (you can comment this out in the final game version)
            drawCollisionBox();
        }
    }

    public boolean checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        if (isActive && !isDying) {
            // Checking collision with the adjusted size and offset
            if (playerX + playerWidth > x + sideOffsetX && playerX < x + sideWidth + sideOffsetX &&
                    playerY + playerHeight > y + sideOffsetY && playerY < y + sideHeight + sideOffsetY) {
                // Collision detected, start death animation
                isDying = true;
                deathFrame = 0; // Reset death frame
                return true; // Return true as collision occurred
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