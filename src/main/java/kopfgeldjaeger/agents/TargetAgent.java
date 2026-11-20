package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.BountyTarget;
import kopfgeldjaeger.logic.TargetType;

public class TargetAgent
{
    private final BountyTarget target;
    private final TargetType type;
    private Position position;
    private boolean active;

    public TargetAgent(TargetType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Der Zieltyp darf nicht null sein.");
        }

        this.type = type;
        this.target = type.createTarget();
        this.active = false;
    }

    // Kommunikation: Umgebungsagent -> Zielpersonenagent
    public void spawnAt(Position spawnPosition)
    {
        if (spawnPosition == null)
        {
            throw new IllegalArgumentException("Spawnposition darf nicht null sein.");
        }

        this.position = spawnPosition;
        this.active = true;
    }

    public BountyTarget getTarget()
    {
        return target;
    }

    public TargetType getType()
    {
        return type;
    }

    public Position getPosition()
    {
        return position;
    }

    public boolean isActive()
    {
        return active;
    }

    public void deactivate()
    {
        this.active = false;
    }

    public boolean isSpecialTarget()
    {
        return type == TargetType.SPECIAL_TARGET;
    }

    public boolean isDefeated()
    {
        return target.isDefeated();
    }
}