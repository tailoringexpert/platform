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
package eu.tailoringexpert.catalog;

import eu.tailoringexpert.domain.Logo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

class LogoProviderTest {

    private ToLogoFunction provider;

    @BeforeEach
    void setup() {
        this.provider = new ToLogoFunction();
    }

    @Test
    void apply_NameNull_NullReturned() {
        // arrange
        Map<String, Logo> logos = emptyMap();

        // act
        Logo actual = provider.apply(null, logos);

        // assert
        assertThat(actual).isNull();
    }

    @Test
    void apply_LogoNotExists_NullReturned() {
        // arrange
        Map<String, Logo> logos = emptyMap();

        // act
        Logo actual = provider.apply("ECSS", logos);

        // assert
        assertThat(actual).isNull();
    }


    @Test
    void apply_LogoExists_LogoReturned() {
        // arrange
        Logo ecss = Logo.builder().name("ECSS").url("ecss.png").build();
        Map<String, Logo> logos = Map.of("ECSS", ecss);

        // act
        Logo actual = provider.apply("ECSS", logos);

        // assert
        assertThat(actual).isNotNull();
    }
}
