package eu.tailoringexpert.screeningsheet;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ScreeningSheetParameterEintrag {

    private String kategorie;
    private String name;
    private String bezeichnung;

}
