package andius.objects;

import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Objects;

public class Actor {

    private final String name;
    private TextureRegion tr;
    private Role role;
    private int wx;
    private int wy;
    private float x;
    private float y;
    private int dir;
    private MovementBehavior movement = MovementBehavior.FIXED;
    private Mutable enemy;
    private CharacterRecord player;
    private PlayerCursor playerCursor;

    private String hash;

    public Actor(String name) {
        this.name = name;
    }

    public void set(Mutable enemy, Role role, int wx, int wy, float x, float y, MovementBehavior movement, TextureRegion tr) {
        this.role = role;
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.movement = movement;
        this.enemy = enemy;
        this.tr = tr;
        this.hash = "M:" + x + ":" + y;
    }

    public void set(CharacterRecord player, int wx, int wy, float x, float y) {
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.player = player;
        this.hash = "P:" + x + ":" + y;
    }

    public String hash() {
        return this.hash;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.hash);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Actor other = (Actor) obj;
        return Objects.equals(this.hash, other.hash);
    }

    public String getName() {
        return this.name;
    }

    public Role getRole() {
        return role;
    }

    public TextureRegion getIcon() {
        return this.tr;
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

    public Mutable getEnemy() {
        return this.enemy;
    }

    public CharacterRecord getPlayer() {
        return player;
    }

    public void adjustHP(int amt) {
        this.player.adjustHP(amt);
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
