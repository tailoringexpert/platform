package eu.tailoringexpert.reqif;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

@Log4j2
class XhtmlConverterTest {

    XhtmlConverter xhtml;

    @BeforeEach
    void setUp() {
        this.xhtml = new XhtmlConverter();
    }

    @Test
    void apply_NoPlaceholder() {
        // arrange
        String html = "Based on classified functions, units and/or assemblies into criticality categories acc. para. 4.8.2, the following components class conditions shall be used: <table style=\"background-color: #ffffff; width: 99%\"> <tbody> <tr> <td style=\"width:250px;\"> <ol> <li><u>Severity level-1</u><br> <strong>Class-1</strong> components or<br> <strong>Class-2</strong> components if unit or assembly will be redundant<br> <strong>Class-1</strong> commercial components or<br> <strong>Class-2</strong> commercial components if unit or assembly will be redundant<br> &nbsp;</li> <li><u>Severity level-2/3</u><br> <strong>Class-3</strong> components if unit or assembly will be redundant<br> <strong>Class-2</strong> commercial components or<br> <strong>Class-3</strong> commercial components if unit or assembly will be redundant<br> &nbsp;</li> <li><u>Severity level-4</u><br> <strong>Class-2</strong>/<strong>Class-3</strong> components<br> <strong>Class-2</strong>/<strong>Class-3</strong> commercial components</li> </ol> </td> <td><img border=\"0px\" width=\"90%\" src= \"/assets/arsu/9.0.0/catalog/level-2-eee.png\"></td> </tr> </tbody> </table> <blockquote dir=\"ltr\" style=\"margin-right: 0px\"> <strong>NOTE:</strong><br> FMECA shall be performed to identify and classify the criticality of functions, units and/or assemblies.</blockquote>";

        // act
        String actual = xhtml.apply(html, Map.of());

        // assert
        log.debug(actual);

    }
}
