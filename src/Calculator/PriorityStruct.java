package Calculator;

public class PriorityStruct<F,S,T>{
    private F first;
    private S second;
    private T third;

    public PriorityStruct(F first, S second, T third) {
        assert first != null;
        assert second != null;
        assert third != null;
        this.first = first;
        this.second = second; 
        this.third = third; 
    }

    public F getFirst() { 
        return first; 
    }

    public S getSecond() { 
        return second; 
    }

    public T getThird() { 
        return third; 
    }

    public void setFirst(F first) {
        this.first = first;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    public void setThird(T third) {
        this.third = third;
    }
}
