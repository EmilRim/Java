import processing.core.PApplet;
import processing.core.PImage;

public class Player {
    private PApplet p;
    private PImage[] sprites;

    // Position and movement
    private float x;
    private float y;
    private float speed;
    private boolean movingUp = false;
    private boolean movingDown = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;

    // Animation
    private int tileWidth;
    private int tileHeight;
    private int direction = 0; // 0 = down, 1 = left, 2 = up, 3 = right
    private int frame = 0;
    private int animationCounter = 0;
    private int animationSpeed = 2; // Lower value = faster animation
    private int scaleFactor;

    // Collision boxes
    private int frontBackOffsetX = 50;
    private int frontBackOffsetY = 50;
    private int frontBackWidth;
    private int frontBackHeight;

    private int sideOffsetX = 30;
    private int sideOffsetY = 50;
    private int sideWidth;
    private int sideHeight;

    public Player(PApplet p, PImage spritesheet, float startX, float startY, float speed,
                  int cols, int rows, int scaleFactor) {
        this.p = p;
        this.x = startX;
        this.y = startY;
        this.speed = speed;
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

        // Set collision box sizes
        frontBackWidth = tileWidth * scaleFactor - 2 * frontBackOffsetX;
        frontBackHeight = tileHeight * scaleFactor - 2 * frontBackOffsetY;
        sideWidth = tileWidth * scaleFactor - 2 * sideOffsetX;
        sideHeight = tileHeight * scaleFactor - 2 * sideOffsetY;
    }

    public void update(GameMap map, Enemy enemy) {
        updateAnimation();
        updatePosition(map, enemy);
    }

    private void updateAnimation() {
        animationCounter++;

        if (animationCounter >= animationSpeed) {
            animationCounter = 0;

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

    private void updatePosition(GameMap map, Enemy enemy) {
        float newX = x;
        float newY = y;

        if (movingUp) newY -= speed;
        if (movingDown) newY += speed;
        if (movingLeft) newX -= speed;
        if (movingRight) newX += speed;

        // Check and apply horizontal movement if valid
        if (newX != x && !checkCollision(newX, y, map, enemy)) {
            x = newX;
        }

        // Check and apply vertical movement if valid
        if (newY != y && !checkCollision(x, newY, map, enemy)) {
            y = newY;
        }
    }

    private boolean checkCollision(float testX, float testY, GameMap map, Enemy enemy) {
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

        // Check collision with enemy
        if (enemy.checkCollision(testX + offsetX, testY + offsetY, boxWidth, boxHeight)) {
            return true;
        }

        return false;
    }

    public void draw() {
        p.image(sprites[frame], x, y, tileWidth * scaleFactor, tileHeight * scaleFactor);

        // Draw collision box for debugging
        drawCollisionBox();
    }

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

    public void keyPressed(char key) {
        if (key == 'w') {
            movingUp = true;
        } else if (key == 's') {
            movingDown = true;
        } else if (key == 'a') {
            movingLeft = true;
        } else if (key == 'd') {
            movingRight = true;
        }
    }

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

    // Getters
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