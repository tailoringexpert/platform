/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 Michael Bädorf and others
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
package eu.tailoringexpert.screeningsheet;

import eu.tailoringexpert.TenantContext;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RequiredArgsConstructor
class TenantScreeningSheetParameterProviderTest {

    TenantScreeningSheetParameterProvider provder;
    ScreeningSheetParameterProvider tenentScreeningSheetParameterProvider;

    @BeforeEach
    void beforeEach() {
        this.tenentScreeningSheetParameterProvider = mock(ScreeningSheetParameterProvider.class);
        this.provder = new TenantScreeningSheetParameterProvider(Map.ofEntries(
            new AbstractMap.SimpleEntry("TENANT", tenentScreeningSheetParameterProvider)
        ));
    }

    @Test
    void parse_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");
        InputStream is = new ByteArrayInputStream("Blindobjekt".getBytes(UTF_8));

        // act
        Collection<ScreeningSheetParameterEintrag> actual = provder.parse(is);

        // assert
        verify(tenentScreeningSheetParameterProvider, times(0)).parse(is);
        assertThat(actual).isEmpty();
    }

    @Test
    void parse_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        InputStream is = new ByteArrayInputStream("Blindobjekt".getBytes(UTF_8));

        given(tenentScreeningSheetParameterProvider.parse(is))
            .willReturn(List.of(ScreeningSheetParameterEintrag.builder().kategorie("Param1").name("Value1").build()));

        // act
        Collection<ScreeningSheetParameterEintrag> actual = provder.parse(is);

        // assert
        verify(tenentScreeningSheetParameterProvider, times(1)).parse(is);
        assertThat(actual).hasSize(1);
    }
}
