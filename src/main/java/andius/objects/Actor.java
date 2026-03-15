package andius.objects;

import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.Objects;

public class Actor {

    private final String name;
    private Animation<TextureRegion> anim;
    private int icon;
    private Role role;
    private int wx;
    private int wy;
    private int dir;
    private MovementBehavior movement = MovementBehavior.FIXED;
    private Mutable enemy;
    private CharacterRecord player;
    private String hash;

    public Actor(String name) {
        this.name = name;
    }

    public void set(Mutable enemy, Role role, int wx, int wy, MovementBehavior movement, int icon) {
        this.role = role;
        this.wx = wx;
        this.wy = wy;
        this.movement = movement;
        this.enemy = enemy;
        this.icon = icon;
        this.anim = UltimaSprite.anim(icon);
        this.hash = "M:" + wx + ":" + wy;

        if (anim == null) {
            throw new RuntimeException("anim cannot be null");
        }
    }

    public void set(CharacterRecord player, int wx, int wy) {
        this.wx = wx;
        this.wy = wy;
        this.player = player;
        this.hash = "P:" + wx + ":" + wy;
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

    public Animation<TextureRegion> getAnim() {
        return this.anim;
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

    public int getDirection() {
        return dir;
    }

    public void setDirection(int dir) {
        this.dir = dir;
    }

    public int icon() {
        return icon;
    }

}
