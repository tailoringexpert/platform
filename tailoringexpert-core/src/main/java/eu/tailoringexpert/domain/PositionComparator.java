package eu.tailoringexpert.domain;

import lombok.NonNull;

import java.util.Comparator;

/**
 * Comparator for comparison of postions with same or different length ignoring caseing.
 *
 * @author Michael Baedorf
 */
public class PositionComparator implements Comparator<String> {

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(@NonNull String reference, @NonNull String compare) {
        int referenceLength = reference.length();
        int compareLength = compare.length();

        if (referenceLength == compareLength) {
            return reference.compareToIgnoreCase(compare);
        }

        return referenceLength < compareLength ? -1 : 1;
    }
}
