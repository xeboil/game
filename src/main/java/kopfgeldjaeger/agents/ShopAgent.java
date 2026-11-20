package kopfgeldjaeger.agents;

import kopfgeldjaeger.logic.Upgrade;

public class ShopAgent
{
    public Upgrade[] getAvailableUpgrades()
    {
        return Upgrade.values();
    }

    public boolean canPurchase(BountyHunterAgent hunter, Upgrade upgrade)
    {
        if (hunter == null || upgrade == null)
        {
            return false;
        }

        return !hunter.getState().hasUpgrade(upgrade)
                && hunter.getState().getMoney() >= upgrade.getPrice();
    }

    // Kommunikation: Kopfgeldjäger <-> Shopagent
    public boolean sellUpgrade(BountyHunterAgent hunter, Upgrade upgrade)
    {
        if (!canPurchase(hunter, upgrade))
        {
            return false;
        }

        return hunter.applyUpgrade(upgrade);
    }
}