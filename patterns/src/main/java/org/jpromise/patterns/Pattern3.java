package org.jpromise.patterns;

public class Pattern3<V1, V2, V3> extends Pattern2<V1, V2> {
    public final V3 item3;

    public Pattern3(V1 item1, V2 item2, V3 item3) {
        super(item1, item2);
        this.item3 = item3;
    }

    @Override
    public String toString() {
        return String.format("( %s, %s, %s )", item1, item2, item3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern3)) return false;
        if (!super.equals(o)) return false;

        Pattern3 that = (Pattern3) o;

        return !(item3 != null ? !item3.equals(that.item3) : that.item3 != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (item3 != null ? item3.hashCode() : 0);
        return result;
    }
}
