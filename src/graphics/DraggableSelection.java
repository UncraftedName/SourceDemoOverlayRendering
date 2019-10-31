package graphics;

import main.Main;
import processing.core.PApplet;
import processing.core.PConstants;
import utils.helperClasses.HelperFuncs;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class DraggableSelection implements Drawable {


    private final static int rectColor = (255 << 24) | (255 << 16) | 255; // purple
    private final static int selectedColor = (255 << 24) | (255 << 16) | (255 << 8); // yellow
    private final static int nonSelectedColor = (255 << 24) | (255 << 16); //red
    private boolean visible = false;
    private float startX, startY;
    private float endX, endY, pEndX, pEndY;
    private boolean leftMouse;
    private HashSet<Player> permanentPlayers = new HashSet<>();
    private HashSet<Player> playersInCurrentSelection = new HashSet<>();
    private final List<Player> players;


    public DraggableSelection(Main canvas) {
        players = HelperFuncs.filterForType(canvas.drawables.stream(), Player.class).collect(Collectors.toUnmodifiableList());
    }


    @Override
    public void draw(Main canvas) {
        if (visible) {
            canvas.pushStyle();
            canvas.noFill();
            canvas.stroke(rectColor);
            canvas.strokeWeight(5);
            canvas.rectMode(PConstants.CORNERS);
            canvas.rect(startX, startY, endX, endY);
            canvas.popStyle();
        }
    }


    @Override
    public void mousePressed(PApplet applet, float scaleFactor, float transX, float transY) {
        if (!visible) {
            leftMouse = applet.mouseButton == PConstants.LEFT;
            visible = true;
            startX = endX = pEndX = applet.mouseX;
            startY = endY = pEndY = applet.mouseY;
        }
    }


    @Override
    public void mouseDragged(PApplet applet, float scaleFactor, float transX, float transY) {
        pEndX = endX;
        pEndY = endY;
        endX = applet.mouseX;
        endY = applet.mouseY;
        if (areaUnderSelection() > pAreaUnderSelection()) { // area increased, only look for new players
            for (Player player : players) {
                player.backgroundColor = nonSelectedColor;
                if (playerInSelection(player))
                    playersInCurrentSelection.add(player);
            }
        } else {
            for (Player player : players) {
                player.backgroundColor = nonSelectedColor;
                if (!playerInSelection(player))
                    playersInCurrentSelection.remove(player);
            }
        }
        for (Player player : permanentPlayers)
            player.backgroundColor = selectedColor;
        for (Player player : playersInCurrentSelection)
            player.backgroundColor = leftMouse ? selectedColor : nonSelectedColor;
    }


    @Override
    public void mouseReleased(PApplet applet, float scaleFactor, float transX, float transY) {
        visible = false;
        if (leftMouse)
            permanentPlayers.addAll(playersInCurrentSelection);
        else
            permanentPlayers.removeAll(playersInCurrentSelection);
        playersInCurrentSelection.clear();
    }


    private float areaUnderSelection() {
        return Math.abs((startX - endX) * (startY - endY));
    }


    private float pAreaUnderSelection() {
        return Math.abs((startX - pEndX) * (startY - pEndY));
    }


    private boolean playerInSelection(Player player) {
        return player.x >= Math.min(startX, endX)
                && player.x <= Math.max(startX, endX)
                && player.y >= Math.min(startY, endY)
                && player.y <= Math.max(startY, endY);
    }
}
