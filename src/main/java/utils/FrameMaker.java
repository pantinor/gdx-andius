package utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class FrameMaker {

    private final Pixmap pix;

    private final float height;

    private static final Color BACKGRND = new Color(0x181818ff);
    private static final Color DARKEST = new Color(0x2e2e2eff);
    private static final Color DARK = new Color(0x575757ff);
    private static final Color LIGHT = new Color(0x7a7a7aff);
    private static final Color LIGHTEST = new Color(0xabababff);

    public FrameMaker(int w, int h) {
        this.height = h;
        this.pix = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        this.pix.setColor(BACKGRND);
        this.pix.fillRectangle(0, 0, w, h);
    }

    public FrameMaker(int w, int h, Color bck) {
        this.height = h;
        this.pix = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        this.pix.setColor(bck);
        this.pix.fillRectangle(0, 0, w, h);
    }

    public FrameMaker setBounds(Actor a, float x, float y, float w, float h) {

        if (a != null) {
            a.setBounds(x, y, w, h);
        }

        int ix = (int) x;
        int iy = (int) (this.height - y - h);
        int iw = (int) w;
        int ih = (int) h;

        this.pix.setColor(DARK);
        this.pix.fillRectangle(ix - 6, iy - 6, iw + 12, ih + 12);

        this.pix.setColor(LIGHT);
        this.pix.fillRectangle(ix - 4, iy - 4, iw + 8, ih + 8);

        this.pix.setColor(LIGHTEST);
        this.pix.fillRectangle(ix - 2, iy - 2, iw + 4, ih + 4);

        this.pix.setColor(DARKEST);
        this.pix.fillRectangle(ix, iy, iw, ih);

        return this;
    }

    public FrameMaker drawPixmap(Pixmap pixmap, int x, int y) {
        this.pix.drawPixmap(pixmap, x, y);
        return this;
    }

    public Texture build() {
        Texture t = new Texture(this.pix);
        this.pix.dispose();
        return t;
    }

}
