package Entities;

public class Pair<L,R> {
    private L left;
    private R right;

    public Pair(L left, R right) {
        assert left != null;
        assert right != null;
        this.left = left;
        this.right = right;
    }

    public L getX() { 
        return left; 
    }

    public R getY() { 
        return right; 
    }

    public void setX(L left) {
        this.left = left;
    }

    public void setY(R right) {
        this.right = right;
    }

    @Override
    public int hashCode() { 
        return left.hashCode() ^ right.hashCode(); 
    }

    public boolean equals(Pair<L,R> p) {
        return this.left.equals(p.getX()) && this.right.equals(p.getY());
    }
}