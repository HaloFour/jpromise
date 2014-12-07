package org.jpromise.patterns;

public class Pattern5<V1, V2, V3, V4, V5> extends Pattern4<V1, V2, V3, V4> {
    public final V5 item5;

    public Pattern5(V1 item1, V2 item2, V3 item3, V4 item4, V5 item5) {
        super(item1, item2, item3, item4);
        this.item5 = item5;
    }

    @Override
    public String toString() {
        return String.format("( %s, %s, %s, %s, %s )", item1, item2, item3, item4, item5);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern5)) return false;
        if (!super.equals(o)) return false;

        Pattern5 that = (Pattern5) o;

        return !(item5 != null ? !item5.equals(that.item5) : that.item5 != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (item5 != null ? item5.hashCode() : 0);
        return result;
    }
}