import processing.core.PApplet;

public class Main extends PApplet {

    public static void main(String[] args) {
        PApplet.main(Main.class);
    }


    public void settings() {
        size(1000, 1000);
    }


    public void draw() {
        clear();
        circle(50, 50, 60);
    }
}
