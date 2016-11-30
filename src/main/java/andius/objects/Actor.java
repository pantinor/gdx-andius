/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.objects.Conversations.Conversation;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.g2d.Animation;
import utils.Utils;

/**
 *
 * @author Paul
 */
public class Actor {

    private final String name;
    private final Animation anim;
    private Role role;
    private int wx;
    private int wy;
    private float x;
    private float y;
    private MovementBehavior movement;
    private MutableMonster monster;
    private CharacterRecord player;
    private Conversation conversation;

    public Actor(Icons icon, String name) {
        this.name = name;
        float fr = (float) Utils.getRandomBetween(4, 9) / 10;
        this.anim = new Animation(fr, Icons.ATLAS.findRegions(icon.toString()));
    }

    public void set(MutableMonster monster, Role role, int wx, int wy, float x, float y, MovementBehavior movement) {
        this.role = role;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
        this.monster = monster;
    }

    public void set(CharacterRecord player, int wx, int wy, float x, float y) {
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.player = player;
    }
    
    public String getName() {
        return this.name;
    }

    public Role getRole() {
        return role;
    }

    public Animation getAnimation() {
        return anim;
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

    public MutableMonster getMonster() {
        return monster;
    }

    public CharacterRecord getPlayer() {
        return player;
    }

    public boolean isDisabled() {
        return false;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

}
