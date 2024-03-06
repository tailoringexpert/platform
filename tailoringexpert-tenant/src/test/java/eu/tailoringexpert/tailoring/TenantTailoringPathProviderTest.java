/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2024 Michael Bädorf and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.TenantContext;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@Log4j2
class TenantTailoringPathProviderTest {

    private String basedir;
    private BiFunction<String, String, String> identifierFunctionMock;

    private TenantTailoringPathProvider provider;

    @BeforeEach
    void setup() throws Exception {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();
        this.basedir = env.get("FILES_BASEDIR", "target/files");

        this.identifierFunctionMock = mock(BiFunction.class);
        this.provider = new TenantTailoringPathProvider(this.basedir, this.identifierFunctionMock);

        TenantContext.setCurrentTenant("DEMO");
    }

    @Test
    void apply_TenantSet_CorrectPathReturned() {
        // arrange
        given(identifierFunctionMock.apply("test", "master"))
            .willReturn("1000");

        // act
        Path actual = provider.apply("test", "master");

        // assert
        log.debug(actual.toAbsolutePath());
        assertThat(actual).isEqualTo(Path.of(this.basedir, "DEMO", "test", "1000"));
    }

    @Test
    void apply_TailoringNotExisting_NullReturned() {
        // arrange
        given(identifierFunctionMock.apply("test", "master"))
            .willReturn(null);

        // act
        Path actual = provider.apply("test", "master");

        // assert
        assertThat(actual).isNull();
    }
}


