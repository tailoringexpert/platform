package eu.tailoringexpert.domain;

import lombok.NonNull;

import java.util.Comparator;

/**
 * Comparator for comparison of postions with same or different lengtn
 *
 * @author Michael Baedorf
 */
public class PositionComparator implements Comparator<String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(@NonNull String reference, @NonNull String compare) {
        int lengthO1 = reference.length();
        int lengthO2 = compare.length();

        if (lengthO1 == lengthO2) {
            return reference.compareToIgnoreCase(compare);
        }

        return lengthO1 < lengthO2 ? -1 : 1;
    }
}
