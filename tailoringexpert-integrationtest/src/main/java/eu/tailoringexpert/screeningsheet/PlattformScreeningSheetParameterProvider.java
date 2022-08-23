package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.Tenant;
import lombok.extern.log4j.Log4j2;

import java.io.InputStream;
import java.util.Collection;

import static java.util.List.of;


@Log4j2
@Tenant("plattform")
public class PlattformScreeningSheetParameterProvider implements ScreeningSheetParameterProvider {

    @Override
    public Collection<ScreeningSheetParameterEintrag> parse(InputStream is) {
        return of(
            ScreeningSheetParameterEintrag.builder().category("Project").name("Kuerzel").label("SAMPLE").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("0").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("A").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("B").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("C").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("D").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("E").label("YES").build(),
            ScreeningSheetParameterEintrag.builder().category("phase").name("F").label("YES").build()
        );
    }
}
