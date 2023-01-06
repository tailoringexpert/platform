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
package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.renderer.RendererRequestConfiguration;
import eu.tailoringexpert.renderer.RendererRequestConfigurationSupplier;
import eu.tailoringexpert.renderer.TenantTemplateEngine;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class TenantTemplateEngineTest {

    TenantTemplateEngine engine;
    HTMLTemplateEngine templateEngineMock;
    RendererRequestConfigurationSupplier supplierMock;

    @BeforeEach
    void beforeEach() {
        this.templateEngineMock = mock(HTMLTemplateEngine.class);
        this.supplierMock = mock(RendererRequestConfigurationSupplier.class);
        this.engine = new TenantTemplateEngine(templateEngineMock, supplierMock);
    }

    @Test
    void process_TenantVorhanden_StringWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        given(templateEngineMock.process(anyString(), anyMap())).willReturn("HTML");
        given(supplierMock.get()).willReturn(RendererRequestConfiguration.builder().id("TENANT").build());

        //act
        String actual = engine.process("template", emptyMap());

        // assert
        assertThat(actual).isEqualTo("HTML");
        verify(templateEngineMock, times(1)).process("/TENANT/template", emptyMap());
        verify(supplierMock, times(1)).get();
    }

    @Test
    void toXHTML_TenantVorhanden_StringWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        given(templateEngineMock.toXHTML(anyString(), anyMap())).willReturn("HTML");

        //act
        String actual = engine.toXHTML("HTML", emptyMap());

        // assert
        assertThat(actual).isEqualTo("HTML");
        verify(templateEngineMock, times(1)).toXHTML(eq("HTML"), anyMap());
        verify(supplierMock, times(0)).get();
    }

}
