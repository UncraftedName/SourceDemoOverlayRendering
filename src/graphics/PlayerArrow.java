package graphics;

import main.Main;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class PlayerArrow implements Drawable {

    // this is multiplied by the player diameter
    private static final float baseWidth = .3f;
    private static final float tipWidth = .3f;
    private static final float baseLength = 0.8f;
    private static final float tipLength = 0.5f;
    private final Player playerRef;
    private final PShape arrow;


    PlayerArrow(PApplet applet, Player player) {
        playerRef = player;
        arrow = applet.createShape();
        arrow.beginShape();
        arrow.fill(player.textColor);
        arrow.noStroke();
        arrow.vertex(0, baseWidth / 2);
        arrow.vertex(baseLength, baseWidth / 2);
        arrow.vertex(baseLength, (baseWidth + tipWidth) / 2);
        arrow.vertex(baseLength + tipLength, 0);
        arrow.vertex(baseLength, -(baseWidth + tipWidth) / 2);
        arrow.vertex(baseLength, -baseWidth / 2);
        arrow.vertex(0, -baseWidth / 2);
        arrow.endShape(PConstants.CLOSE);
    }


    @Override
    public void draw(Main canvas) {
        canvas.push();
        float yaw = (float)canvas.mapper.getScreenYaw(playerRef.currentPosAndRot.viewAngles[0]);
        canvas.translate(
                playerRef.x + playerRef.diameter / 2 * (float)Math.cos(yaw),
                playerRef.y + playerRef.diameter / 2 * (float)Math.sin(yaw));
        canvas.scale(playerRef.diameter);
        canvas.rotate(yaw);
        canvas.shape(arrow, 0, 0);
        canvas.pop();
    }
}
