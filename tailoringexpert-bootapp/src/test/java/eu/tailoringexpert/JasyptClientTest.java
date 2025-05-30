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
package eu.tailoringexpert;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import eu.tailoringexpert.JasyptClient.JasyptConfig;
import eu.tailoringexpert.JasyptClient.JasyptParameter;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
class JasyptClientTest {

    @Test
    void parse_MissingMandatoryPassword_ParameterExceptionThrown() {
        // arrange
        JasyptConfig config = new JasyptConfig();
        JasyptParameter parameter = new JasyptParameter();
        String[] args = {"--parameter", "test1234", "-parameter", "1234test"};
        // act

        Throwable actual = catchThrowable(() -> JCommander.newBuilder()
            .addObject(new Object[]{config, parameter})
            .build()
            .parse(args)
        );

        // assert
        assertThat(actual).isInstanceOf(ParameterException.class);
    }


    @Test
    void parse_2Parameter_FilledWith2Values() {
        // arrange
        JasyptConfig config = new JasyptConfig();
        JasyptParameter parameter = new JasyptParameter();
        String[] args = {"--password", "DasIstDasHausVomNikolaus", "--parameter", "test1234", "-parameter", "1234test"};

        // act
        JCommander.newBuilder()
            .addObject(new Object[]{config, parameter})
            .build()
            .parse(args);

        // assert
        assertThat(parameter.getParameters()).hasSize(2);
    }

    @Test
    void encrypt() {
        // arrange
        JasyptConfig config = JasyptConfig.builder()
            .algorithm("PBEWithMD5AndTripleDES")
            .password("TailoringForDemo")
            .build();
        JasyptClient client = new JasyptClient();

        // act
        String actual = client.encrypt(config, "PZxLBL7m8HSoK2EJWv7P");

        // assert
        client.decrypt(config, "4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=");

        // assert
        log.debug(actual);
        assertThatNoException();
    }

    @Test
    void decrypt() {
        // arrange
        JasyptConfig config = JasyptConfig.builder()
            .algorithm("PBEWithMD5AndTripleDES")
            .password("TailoringForDemo")
            .build();
        JasyptClient client = new JasyptClient();

        // act
        String actual = client.decrypt(config, "4qBa1ScLN/2lSdLpjRcdqnBtlN5zQrVW54n04C3f90U=");

        // assert
        assertThatNoException();
        assertThat(actual).isEqualTo("PZxLBL7m8HSoK2EJWv7P");
    }

    @Test
    void encryptDecryptRoundtrip() {
        // arrange
        JasyptConfig config = JasyptConfig.builder()
            .algorithm("PBEWithMD5AndTripleDES")
            .password("TailoringForDemo")
            .build();
        JasyptClient client = new JasyptClient();
        String encrypted = client.encrypt(config, "PZxLBL7m8HSoK2EJWv7P");

        // act
        String actual = client.decrypt(config, encrypted);

        // assert
        assertThatNoException();
        assertThat(actual).isEqualTo("PZxLBL7m8HSoK2EJWv7P");
    }

    @Test
    void main_ParametersAvailable_AllParametersEncrypted() {
        // arrange
        String[] args = new String[]{"--parameter", "test1234", "--parameter", "1234test", "--password", "DasIstDasHausVomNikolaus"};
        JasyptClient clientMock = mock(JasyptClient.class);

        // act
        try (MockedStatic<JasyptClient> client = mockStatic(JasyptClient.class)) {
            client.when(() -> JasyptClient.createInstance()).thenReturn(clientMock);
            client.when(() -> JasyptClient.main(any())).thenCallRealMethod();
            JasyptClient.main(args);
        }

        // assert
        verify(clientMock, times(1)).encrypt(any(JasyptConfig.class), eq("test1234"));
        verify(clientMock, times(1)).encrypt(any(JasyptConfig.class), eq("1234test"));
    }

    @Test
    void main_ParameteNotAvailable_ExceptionThrown() {
        // arrange
        String[] args = new String[]{"--password", "DasIstDasHausVomNikolaus"};

        // act
        Throwable actual = catchThrowable(() -> JasyptClient.main(args));

        // assert
        assertThat(actual).isInstanceOf(ParameterException.class);
    }
}
