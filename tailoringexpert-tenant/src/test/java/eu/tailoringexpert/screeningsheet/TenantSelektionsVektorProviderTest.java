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
import eu.tailoringexpert.domain.Parameter;
import eu.tailoringexpert.domain.SelektionsVektor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TenantSelektionsVektorProviderTest {

    TenantSelektionsVektorProvider service;
    SelektionsVektorProvider tenentSelektionsVektorProvider;

    @BeforeEach
    void beforeEach() {
        this.tenentSelektionsVektorProvider = mock(SelektionsVektorProvider.class);
        this.service = new TenantSelektionsVektorProvider(Map.ofEntries(
            new AbstractMap.SimpleEntry("TENANT", tenentSelektionsVektorProvider)
        ));
    }


    @Test
    void apply_MandantNichtVorhanden_EmptyWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");

        // act
        SelektionsVektor actual = service.apply(emptyList());

        // assert
        assertThat(actual).isNull();
        verify(tenentSelektionsVektorProvider, times(0)).apply(any());
    }

    @Test
    void apply_MandantVorhanden_ErgebnisDesMandantAufrufsZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        List<Parameter> parameters = emptyList();

        given(tenentSelektionsVektorProvider.apply(parameters))
            .willReturn(SelektionsVektor.builder().build());

        // act
        SelektionsVektor actual = service.apply(parameters);

        // assert
        verify(tenentSelektionsVektorProvider, times(1)).apply(parameters);
        assertThat(actual).isNotNull();
    }
}
