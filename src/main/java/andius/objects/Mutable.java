package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
import java.util.List;

public interface Mutable {

    public String name();

    public int icon();

    public int getArmourClass();

    public int getLevel();

    public Object baseType();

    public ClassType getType();

    public CharacterType getMonsterType();

    public List<Dice> getDamage();

    public int hitModifier();

    public int getCurrentHitPoints();

    public void adjustHitPoints(int points);

    public int getCurrentMageSpellLevel();

    public int getCurrentPriestSpellLevel();

    public void decrementSpellPoints(Spells spell);

    public Breath breath();

    public boolean isUnaffected(Spells spell, CharacterType type);

    public State status();

    public boolean isDead();

    public void processStatusAffects();

    public int getMaxHitPoints();

    public HealthCursor getHealthCursor();

    public void setHealthCursor(HealthCursor monsterCursor);

    public void adjustHealthCursor();

    public void setACModifier(int acmodifier);

    public int getACModifier();

    public Spells castMageSpell();

    public Spells castPriestSpell();

}
