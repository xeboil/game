package kopfgeldjaeger.agents;

public class Position
{
    private double x;
    private double y;

    public Position(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public double distanceTo(Position other)
    {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public String toString()
    {
        return String.format("(%.1f, %.1f)", x, y);
    }
}