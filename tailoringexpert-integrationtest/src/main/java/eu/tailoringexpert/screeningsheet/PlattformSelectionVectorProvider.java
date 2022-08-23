package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.Tenant;
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.SelectionVector;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
@Tenant("plattform")
public class PlattformSelectionVectorProvider implements SelectionVectorProvider {

    public static final String PRODUCTASSURANCE = "A";
    public static final String QUALITYASSURANCE = "Q";
    public static final String MAINTAINABILITY = "M";
    public static final String RELIABILITY = "R";
    public static final String EEE = "E";
    public static final String PMMP = "P";
    public static final String SOFTWARE = "W";
    public static final String SAFETY = "S";
    public static final String SPACEDEBRIS = "J";
    public static final String PLANETARYPROTECTION = "F";
    public static final String SPACECYBERSECURITY = "C";
    public static final String GROUNDSEGMENT = "B";

    @Override
    public SelectionVector apply(Collection<Parameter> parameterNames) {


        return SelectionVector.builder()
            .level(PRODUCTASSURANCE, 5)
            .level(QUALITYASSURANCE, 5)
            .level(EEE, 5)
            .level(PMMP, 5)
            .level(RELIABILITY, 5)
            .level(SAFETY, 5)
            .level(SOFTWARE, 5)
            .level(SPACEDEBRIS, 5)
            .level(MAINTAINABILITY, 5)
            .level(SPACECYBERSECURITY, 5)
            .level(PLANETARYPROTECTION, 5)
            .level(GROUNDSEGMENT, 5)
            .build();
    }
}
