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
import eu.tailoringexpert.renderer.TenantTemplateEngine;
import eu.tailoringexpert.renderer.HTMLTemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.Map;

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
    HTMLTemplateEngine tenantTemplateEngineMock;

    @BeforeEach
    void beforeEach() {
        this.tenantTemplateEngineMock = mock(HTMLTemplateEngine.class);
        this.engine = new TenantTemplateEngine(Map.ofEntries(
            new AbstractMap.SimpleEntry("TENANT", tenantTemplateEngineMock)
        ));
    }

    @Test
    void process_TenantNotExists_EmptyReturned() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");

        // act
        String actual = engine.process("template", emptyMap());

        // assert
        assertThat(actual).isNull();
        verify(tenantTemplateEngineMock, times(0)).process("template", emptyMap());

    }

    @Test
    void process_TenantVorhanden_StringWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        given(tenantTemplateEngineMock.process(anyString(), anyMap())).willReturn("HTML");

        //act
        String actual = engine.process("template", emptyMap());

        // assert
        assertThat(actual).isEqualTo("HTML");
        verify(tenantTemplateEngineMock, times(1)).process("template", emptyMap());
    }

    @Test
    void toXHTML_TenantNichtVorhanden_NullWirdGeworfen() {
        // arrange
        TenantContext.setCurrentTenant("INVALD");

        // act
        String actual = engine.toXHTML("HTML", emptyMap());

        // assert
        assertThat(actual).isNull();
        verify(tenantTemplateEngineMock, times(0)).toXHTML("HTML", emptyMap());

    }

    @Test
    void toXHTML_TenantVorhanden_StringWirdZurueckGegeben() {
        // arrange
        TenantContext.setCurrentTenant("TENANT");
        given(tenantTemplateEngineMock.toXHTML(anyString(), anyMap())).willReturn("HTML");

        //act
        String actual = engine.toXHTML("HTML", emptyMap());

        // assert
        assertThat(actual).isEqualTo("HTML");
        verify(tenantTemplateEngineMock, times(1)).toXHTML(eq("HTML"), anyMap());
    }

}
