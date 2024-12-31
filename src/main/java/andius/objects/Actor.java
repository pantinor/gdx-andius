package andius.objects;

import andius.Constants.MovementBehavior;
import andius.Constants.Role;
import andius.TibianSprite;
import andius.TibianSprite.TibianAnimation;
import andius.objects.SaveGame.CharacterRecord;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Actor {

    private final int id;
    private final String name;
    private TibianAnimation anim;
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

    public Actor(int id, String name, TibianAnimation anim) {
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

    public Vector2 iconCenter() {
        return this.anim.center;
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

    public TextureRegion getFrame(float time) {
        return (TextureRegion) this.anim.getKeyFrame(time, true);
    }

    public TibianAnimation getAnimation() {
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
