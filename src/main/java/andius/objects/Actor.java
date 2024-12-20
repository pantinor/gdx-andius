/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.TibianSprite;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 *
 * @author Paul
 */
public class Actor {

    private static TextureRegion PLAYER_HLTH_BAR = null;

    static {
        try {
            PLAYER_HLTH_BAR = new TextureRegion(new Texture(Gdx.files.classpath("assets/skin/imgBtn.png")), 381, 82, 124, 6);
        } catch (Throwable t) {
        }
    }

    private final int id;
    private final String name;
    private Animation anim;
    private Role role;
    private int wx;
    private int wy;
    private float x;
    private float y;
    private int dir;
    private MovementBehavior movement;
    private MutableMonster monster;
    private CharacterRecord player;
    private PlayerCursor playerCursor;

    private String hash;

    public Actor(int id, String name, Animation anim) {
        this.id = id;
        this.name = name;
        this.anim = anim;
    }

    public void set(MutableMonster monster, Role role, int wx, int wy, float x, float y, MovementBehavior movement) {
        this.role = role;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
        this.monster = monster;

        if (this.monster != null) {
            this.anim = TibianSprite.animation(this.monster.getIconId());
        }
        this.hash = "M:" + x + ":" + y;
    }

    public void set(CharacterRecord player, int wx, int wy, float x, float y) {
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.player = player;
        this.hash = "P";
    }

    public String hash() {
        return this.hash;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Role getRole() {
        return role;
    }

    public TextureRegion getIcon() {
        return (TextureRegion) this.anim.getKeyFrames()[this.dir];
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

    public void adjustHP(int amt) {
        player.adjustHP(amt);
        this.playerCursor.adjust(player.hp, player.maxhp);
    }

    public PlayerCursor getPlayerCursor() {
        return playerCursor;
    }

    public void setPlayerCursor(PlayerCursor playerCursor) {
        this.playerCursor = playerCursor;
    }

    public int getDirection() {
        return dir;
    }

    public void setDirection(int dir) {
        this.dir = dir;
    }

}
