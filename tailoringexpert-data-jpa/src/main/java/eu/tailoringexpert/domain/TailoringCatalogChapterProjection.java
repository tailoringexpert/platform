package eu.tailoringexpert.domain;

import java.util.List;


public interface TailoringCatalogChapterProjection {

    String getName();

    /**
     * Position in chapter list.
     */
    int getPosition();

    /**
     * (Full) Number of chapter.
     */
    String getNumber();

    /**
     * List of subchapters.
     */
    List<TailoringCatalogChapterProjection> getChapters();
}
