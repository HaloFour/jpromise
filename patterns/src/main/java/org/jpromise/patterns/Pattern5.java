package org.jpromise.patterns;

/**
 * Represents the result of five asynchronous operations.
 * @param <V1> The type of the first result.
 * @param <V2> The type of the second result.
 * @param <V3> The type of the third result.
 * @param <V4> The type of the fourth result.
 * @param <V5> The type of the fifth result.
 */
public class Pattern5<V1, V2, V3, V4, V5> extends Pattern4<V1, V2, V3, V4> {
    /**
     * The fifth result.
     */
    public final V5 item5;

    Pattern5(V1 item1, V2 item2, V3 item3, V4 item4, V5 item5) {
        super(item1, item2, item3, item4);
        this.item5 = item5;
    }

    /**
     * {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object get(int index) {
        switch (index) {
            case 4:
                return item5;
            default:
                return super.get(index);
        }
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("( %s, %s, %s, %s, %s )", item1, item2, item3, item4, item5);
    }

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern5)) return false;
        if (!super.equals(o)) return false;

        Pattern5 that = (Pattern5) o;

        return !(item5 != null ? !item5.equals(that.item5) : that.item5 != null);

    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (item5 != null ? item5.hashCode() : 0);
        return result;
    }
}