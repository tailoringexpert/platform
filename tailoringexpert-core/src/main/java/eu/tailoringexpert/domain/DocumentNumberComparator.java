package eu.tailoringexpert.domain;

import lombok.NonNull;

import java.util.Comparator;

public class DocumentNumberComparator implements Comparator<Document> {
    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(@NonNull Document reference, @NonNull Document compare) {
        int referenceLength = reference.getNumber().length();
        int compareLength = compare.getNumber().length();

        if (referenceLength == compareLength) {
            return reference.getNumber().compareToIgnoreCase(compare.getNumber());
        }

        return referenceLength < compareLength ? -1 : 1;
    }
}
