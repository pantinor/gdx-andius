/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import utils.Utils;

/**
 *
 * @author Paul
 */
public class MutableMonster extends Monster {

    private int currentHitPoints;
    private final int maxHitPoints;
    private TextureRegion healthBar;

    public MutableMonster(Monster m) {
        clone(m);
        this.maxHitPoints = this.hitPoints.roll();
        this.currentHitPoints = this.maxHitPoints;
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
            healthBar = new TextureRegion(Utils.fillRectangle(76, 10, Color.GREEN, .5f));
        }
        return this.healthBar;
    }

    public void adjustHealthBar() {
        double percent = (double) currentHitPoints / maxHitPoints;
        double bar = percent * (double) 76;
        if (currentHitPoints < 0) {
            bar = 0;
        }
        if (bar > 76) {
            bar = 76;
        }
        getHealthBar().setRegion(0, 0, (int) bar, 10);
    }

}
