package eu.tailoringexpert.renderer;

import eu.tailoringexpert.domain.Datei;
import org.apache.pdfbox.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

class PDFEngineTest {

    PDFEngine engine;

    @BeforeEach
    void beforeEach() {
        this.engine = new PDFEngine("JUnit", "baseuri");
    }

    @Test
    void process_KeineDocId_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process(null, "html", "suffix"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_KeinHTML_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process("docid", null, "suffix"));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_KeinSuffix_NullPointerExceptionWirdGeworfen() {
        // arrange

        // act
        Exception actual = catchException(() -> engine.process("docid", "html", null));

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);
    }

    @Test
    void process_toByteArrayFehlerhaft_DateiIstNull() {
        // arrange

        // act
        Datei actual;
        try (MockedStatic<IOUtils> io = mockStatic(IOUtils.class)) {
            io.when(() -> IOUtils.toByteArray(any())).thenThrow(new IOException());
            actual = engine.process("4711", "tailoring", "parameter");
        }

        // assert
        assertThat(actual).isNull();
    }
}
