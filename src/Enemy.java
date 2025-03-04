import processing.core.PApplet;
import processing.core.PImage;

public class Enemy {
    private PApplet p;
    private PImage[] sprites;

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

    public Enemy(PApplet p, PImage spritesheet, float startX, float startY,
                 float speed, int cols, int rows, int minX, int maxX, int scaleFactor) {
        this.p = p;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.minX = minX;
        this.maxX = maxX;
        this.scaleFactor = scaleFactor;

        // Extract sprites from spritesheet
        this.tileWidth = spritesheet.width / cols;
        this.tileHeight = spritesheet.height / rows;
        this.sprites = new PImage[cols * rows];

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                sprites[i + j * cols] = spritesheet.get(i * tileWidth, j * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    public void update() {
        // Move the enemy
        x += speed * direction;

        // Reverse direction when reaching bounds
        if (x <= minX || x >= maxX) {
            direction *= -1;
        }

        // Animate enemy
        animationCounter++;
        if (animationCounter >= animationSpeed) {
            animationCounter = 0;
            frame = (frame + 1) % sprites.length; // Cycle between frames
        }
    }

    public void draw() {
        p.image(sprites[frame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);
    }

    public boolean checkCollision(float playerX, float playerY, float playerWidth, float playerHeight) {
        return playerX + playerWidth > x && playerX < x + tileWidth * scaleFactor &&
                playerY + playerHeight > y && playerY < y + tileHeight * scaleFactor;
    }

    // Getters
    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}