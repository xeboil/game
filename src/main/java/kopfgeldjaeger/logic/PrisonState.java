package kopfgeldjaeger.logic;

public class PrisonState
{
    private static final int PRISON_PRICE = 2000;
    private static final int CELL_PRICE = 750;

    private static final int START_CAPACITY = 2;

    private static final int INCOME_PER_PRISONER = 100;

    private boolean purchased = false;

    private int capacity = 0;
    private int prisoners = 0;


    public boolean isPurchased()
    {
        return purchased;
    }


    public int getCapacity()
    {
        return capacity;
    }


    public int getPrisoners()
    {
        return prisoners;
    }


    public int getFreeCells()
    {
        return capacity - prisoners;
    }


    public int getPrisonPrice()
    {
        return PRISON_PRICE;
    }


    public int getCellPrice()
    {
        return CELL_PRICE;
    }


    public int getIncomePerPrisoner()
    {
        return INCOME_PER_PRISONER;
    }


    public boolean isFull()
    {
        return purchased
                && prisoners >= capacity;
    }


    // Gefängnis kaufen
    public boolean purchasePrison(HunterState hunter)
    {
        if (purchased)
        {
            return false;
        }

        if (!hunter.spendMoney(PRISON_PRICE))
        {
            return false;
        }

        purchased = true;

        capacity = START_CAPACITY;

        return true;
    }


    // Zusätzliche Zelle kaufen
    public boolean purchaseCell(HunterState hunter)
    {
        if (!purchased)
        {
            return false;
        }

        if (!hunter.spendMoney(CELL_PRICE))
        {
            return false;
        }

        capacity++;

        return true;
    }


    // Sonderziel einsperren
    public boolean imprisonTarget(BountyTarget target)
    {
        if (!purchased)
        {
            return false;
        }

        if (target == null)
        {
            return false;
        }

        if (isFull())
        {
            return false;
        }

        if (!target.isDefeated())
        {
            return false;
        }

        // Ziel gilt danach als verarbeitet,
        // bekommt aber KEINE direkte Kopfgeldauszahlung.
        target.markDelivered();

        prisoners++;

        return true;
    }


    // Regelmäßige Einnahmen
    public int collectIncome(HunterState hunter)
    {
        if (!purchased || prisoners == 0)
        {
            return 0;
        }

        int income =
                prisoners
                        * INCOME_PER_PRISONER;

        hunter.addMoney(income);

        return income;
    }
}