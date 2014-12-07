package org.jpromise.patterns;

public class Pattern2<V1, V2> {
    public final V1 item1;
    public final V2 item2;

    public Pattern2(V1 item1, V2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    @Override
    public String toString() {
        return String.format("( %s, %s )", item1, item2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern2)) return false;

        Pattern2 that = (Pattern2) o;

        return !(item1 != null ? !item1.equals(that.item1) : that.item1 != null)
                && !(item2 != null ? !item2.equals(that.item2) : that.item2 != null);

    }

    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        return result;
    }
}
