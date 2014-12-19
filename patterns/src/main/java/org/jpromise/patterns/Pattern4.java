package org.jpromise.patterns;

/**
 * Represents the result of four completed asynchronous operations.
 * @param <V1> The type of the first result.
 * @param <V2> The type of the second result.
 * @param <V3> The type of the third result.
 * @param <V4> The type of the fourth result.
 */
public class Pattern4<V1, V2, V3, V4> extends Pattern3<V1, V2, V3> {
    /**
     * The fourth result.
     */
    public final V4 item4;

    Pattern4(V1 item1, V2 item2, V3 item3, V4 item4) {
        super(item1, item2, item3);
        this.item4 = item4;
    }

    /**
     * {@inheritDoc}
     * @param index {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Object get(int index) {
        switch (index) {
            case 3:
                return item4;
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
        return String.format("( %s, %s, %s, %s )", item1, item2, item3, item4);
    }

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern4)) return false;
        if (!super.equals(o)) return false;

        Pattern4 that = (Pattern4) o;

        return !(item4 != null ? !item4.equals(that.item4) : that.item4 != null);

    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (item4 != null ? item4.hashCode() : 0);
        return result;
    }
}
