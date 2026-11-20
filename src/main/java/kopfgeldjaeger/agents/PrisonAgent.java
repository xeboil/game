package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.PrisonState;

public class PrisonAgent
{
    private final PrisonState prisonState;

    public PrisonAgent()
    {
        this.prisonState = new PrisonState();
    }

    public PrisonState getPrisonState()
    {
        return prisonState;
    }

    public boolean buyPrison(BountyHunterAgent hunter)
    {
        if (hunter == null)
        {
            return false;
        }

        return prisonState.purchasePrison(hunter.getState());
    }

    public boolean buyCell(BountyHunterAgent hunter)
    {
        if (hunter == null)
        {
            return false;
        }

        return prisonState.purchaseCell(hunter.getState());
    }

    // Kommunikation: Kopfgeldjäger <-> Gefängnisagent
    public boolean imprisonSpecialTarget(BountyHunterAgent hunter, TargetAgent targetAgent)
    {
        if (hunter == null || targetAgent == null || !hunter.hasCarriedTarget())
        {
            return false;
        }

        if (!targetAgent.isSpecialTarget())
        {
            return false;
        }

        boolean success = prisonState.imprisonTarget(hunter.getCarriedTarget());
        if (success)
        {
            hunter.clearCarriedTarget();
        }

        return success;
    }

    public int collectPeriodicIncome(BountyHunterAgent hunter)
    {
        if (hunter == null)
        {
            return 0;
        }

        return prisonState.collectIncome(hunter.getState());
    }
}