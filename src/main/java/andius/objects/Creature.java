/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.CreatureStatus;
import andius.Creatures;
import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.Icons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import utils.Utils;
import utils.XORShiftRandom;

/**
 *
 * @author Paul
 */
public class Creature {
    
    private final Icons icon;
    
    private Role role;
    private String surname;
    private TextureRegion healthBar;
    private int wx;
    private int wy;
    private float x;
    private float y;
    private MovementBehavior movement;
    private int basehp;
    private int hp;
    private int exp;
    
    public Creature(Icons icon) {
        this.icon = icon;
    }

    public void set(Creatures monster, Role role, String surname, int wx, int wy, float x, float y, MovementBehavior movement) {
        this.role = role;
        this.surname = surname;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
        this.exp = monster.getExp();
        this.basehp = monster.getBasehp();
        this.hp = this.basehp;
    }

    public Role getRole() {
        return role;
    }

    public Icons getIcon() {
        return icon;
    }

    public String getSurname() {
        return surname;
    }

    public int getWx() {
        return wx;
    }

    public void setWx(int wx) {
        this.wx = wx;
    }

    public int getWy() {
        return wy;
    }

    public void setWy(int wy) {
        this.wy = wy;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public MovementBehavior getMovement() {
        return movement;
    }

    public int getExp() {
        return exp;
    }

    public int getDefense() {
        return 128;
    }

    public int getDamage() {
        int damage, val, x;
        val = basehp;
        x = new XORShiftRandom().nextInt(val >> 2);
        damage = (x >> 4) + ((x >> 2) & 0xfc);
        damage += x % 10;
        return damage;
    }

    public int getHP() {
        return this.hp;
    }

    public void setHP(int h) {
        this.hp = h;
        this.adjustHealthBar(h);
    }

    public CreatureStatus getDamageStatus() {

        int crit_threshold = basehp / 4;
        int heavy_threshold = basehp / 2;
        int light_threshold = crit_threshold + heavy_threshold;

        if (hp <= 0) {
            return CreatureStatus.DEAD;
        } else if (hp < crit_threshold) {
            return CreatureStatus.CRITICAL;
        } else if (hp < heavy_threshold) {
            return CreatureStatus.HEAVILYWOUNDED;
        } else if (hp < light_threshold) {
            return CreatureStatus.LIGHTLYWOUNDED;
        } else {
            return CreatureStatus.BARELYWOUNDED;
        }

    }

    public TextureRegion getHealthBar() {
        if (healthBar == null) {
            healthBar = new TextureRegion(Utils.fillRectangle(48, 3, Color.GREEN, .5f));
        }
        return this.healthBar;
    }

    private void adjustHealthBar(int current) {
        double percent = (double) current / 100;
        double bar = percent * (double) 48;
        if (current < 0) {
            bar = 0;
        }
        if (bar > 48) {
            bar = 48;
        }
        getHealthBar().setRegion(0, 0, (int) bar, 3);
    }

}
