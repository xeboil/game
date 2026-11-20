package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.BountyTarget;

public class AuthorityAgent
{
    // Kommunikation: Kopfgeldjäger -> Behördenagent -> Kopfgeldjäger (Auszahlung)
    public int processHandover(BountyHunterAgent hunter)
    {
        if (hunter == null || !hunter.hasCarriedTarget())
        {
            return 0;
        }

        BountyTarget target = hunter.getCarriedTarget();

        if (!target.isDefeated() || target.isDelivered())
        {
            return 0;
        }

        int bounty = target.getBounty();
        hunter.getState().deliverTarget(target);
        hunter.clearCarriedTarget();

        return bounty;
    }
}