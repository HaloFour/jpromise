package org.jpromise.patterns;

/**
 * Represents the result of two completed asynchronous operations.
 * @param <V1> The type of the first result.
 * @param <V2> The type of the second result.
 */
public class Pattern2<V1, V2> {
    /**
     * The first result.
     */
    public final V1 item1;
    /**
     * The second result.
     */
    public final V2 item2;

    Pattern2(V1 item1, V2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }

    /**
     * Gets the result at the specified index.
     * @param index The 0-based index of the result.
     * @return The result at the specified index.
     */
    public Object get(int index) {
        switch (index) {
            case 0:
                return item1;
            case 1:
                return item2;
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("( %s, %s )", item1, item2);
    }

    /**
     * {@inheritDoc}
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pattern2)) return false;

        Pattern2 that = (Pattern2) o;

        return !(item1 != null ? !item1.equals(that.item1) : that.item1 != null)
                && !(item2 != null ? !item2.equals(that.item2) : that.item2 != null);

    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = item1 != null ? item1.hashCode() : 0;
        result = 31 * result + (item2 != null ? item2.hashCode() : 0);
        return result;
    }
}
