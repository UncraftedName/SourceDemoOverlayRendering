package graphics;

public abstract class Selectable implements Drawable {

    public boolean isSelected;

    public abstract boolean mouseSelects(int mouseX, int mouseY);
}
