/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2023 Michael Bädorf and others
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
package eu.tailoringexpert.renderer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.ITemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ThymeleafTemplateEngineTest {

    ITemplateEngine templateEngineMock;
    RendererRequestConfigurationSupplier requestConfigurationSupplierMock;
    ThymeleafTemplateEngine engine;

    @BeforeEach
    void beforeEach() {
        this.templateEngineMock = mock(ITemplateEngine.class);
        this.requestConfigurationSupplierMock = mock(RendererRequestConfigurationSupplier.class);
        this.engine = new ThymeleafTemplateEngine(templateEngineMock, requestConfigurationSupplierMock);
    }

    @Test
    void toXHTML_ExistingPlaceholder_PlaceholderResolved() {
        // arrange
        Map<String, Object> placeholder = Map.of("${PLACEHOLDER}", "DUMMY");
        // act
        String actual = engine.toXHTML("Some really important requirement with placeholder ${PLACEHOLDER}", placeholder);

        // assert
        assertThat(actual).isEqualTo("Some really important requirement with placeholder DUMMY");
    }

    @Test
    void toXHTML_NullParameterValue_PlaceholderPreserved() {
        // arrange
        Map<String, Object> placeholder =  new HashMap<>();
        placeholder.put("${PLACEHOLDER}", null);

        // act
        String actual = engine.toXHTML("Some really important requirement with placeholder ${PLACEHOLDER}", placeholder);

        // assert
        assertThat(actual).isEqualTo("Some really important requirement with placeholder ${PLACEHOLDER}");
    }


}
