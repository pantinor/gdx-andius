/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 *
 * @author Paul
 */
public class MutableMonster extends Monster {

    private static final TextureRegion HLTH_BAR;
    static {
        HLTH_BAR = new TextureRegion(new Texture(Gdx.files.classpath("assets/skin/imgBtn.png")), 381, 82, 74, 8);
    }
    
    private int currentHitPoints;
    private final int maxHitPoints;
    private TextureRegion healthBar;

    public MutableMonster(Monster m) {
        clone(m);
        this.maxHitPoints = this.hitPoints.roll();
        this.currentHitPoints = this.maxHitPoints;
    }

    public void setIcon(Icons icon) {
        this.icon = icon;
    }

    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
    }

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public TextureRegion getHealthBar() {
        if (healthBar == null) {
            healthBar = new TextureRegion(HLTH_BAR);
        }
        return this.healthBar;
    }

    public void adjustHealthBar() {
        double percent = (double) currentHitPoints / maxHitPoints;
        double bar = percent * (double) 74;
        if (currentHitPoints < 0) {
            bar = 0;
        }
        if (bar > 74) {
            bar = 74;
        }
        getHealthBar().setRegion(381, 82, (int) bar, 8);
    }

}
