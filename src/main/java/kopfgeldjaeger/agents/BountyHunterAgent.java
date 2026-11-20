package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.BountyTarget;
import kopfgeldjaeger.logic.HunterState;
import kopfgeldjaeger.logic.Upgrade;

public class BountyHunterAgent
{
    private final HunterState state;
    private final Position position;
    private BountyTarget carriedTarget;

    public BountyHunterAgent(HunterState state, Position startPosition)
    {
        if (state == null || startPosition == null)
        {
            throw new IllegalArgumentException("Zustand und Startposition dürfen nicht null sein.");
        }

        this.state = state;
        this.position = startPosition;
    }

    public HunterState getState()
    {
        return state;
    }

    public Position getPosition()
    {
        return position;
    }

    public BountyTarget getCarriedTarget()
    {
        return carriedTarget;
    }

    public boolean hasCarriedTarget()
    {
        return carriedTarget != null;
    }

    // Kommunikation: Kopfgeldjäger <-> Umgebungsagent
    public boolean moveBy(double deltaX, double deltaY, CityEnvironmentAgent environment)
    {
        return environment.requestMove(this, deltaX, deltaY);
    }

    public void captureTarget(BountyTarget target)
    {
        if (target == null || !target.isDefeated())
        {
            throw new IllegalStateException("Nur ein besiegtes Ziel kann festgenommen werden.");
        }

        this.carriedTarget = target;
    }

    public void clearCarriedTarget()
    {
        this.carriedTarget = null;
    }

    public void receiveMoney(int amount)
    {
        state.addMoney(amount);
    }

    public boolean applyUpgrade(Upgrade upgrade)
    {
        return state.purchaseUpgrade(upgrade);
    }

    public void heal(int amount)
    {
        state.heal(amount);
    }
}