/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.Icons;
import andius.Constants.MovementBehavior;
import andius.Constants.Role;

/**
 *
 * @author Paul
 */
public class Creature {

    private final Icons icon;
    private final Role role;
    private final String surname;
    private int wx;
    private int wy;
    private float x;
    private float y;
    private final MovementBehavior movement;

    public Creature(Icons icon, Role role, String surname, int wx, int wy, float x, float y, MovementBehavior movement) {
        this.icon = icon;
        this.role = role;
        this.surname = surname;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
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

}
