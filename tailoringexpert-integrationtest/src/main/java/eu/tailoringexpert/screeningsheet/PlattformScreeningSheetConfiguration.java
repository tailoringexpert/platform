package eu.tailoringexpert.screeningsheet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlattformScreeningSheetConfiguration {

    @Bean
    SelectionVectorProvider plattformSelectionVectorProvider() {
        return new PlattformSelectionVectorProvider();
    }

    @Bean
    PlattformScreeningSheetParameterProvider plattformScreeningSheetParameterProvider() {
        return new PlattformScreeningSheetParameterProvider();
    }
}
