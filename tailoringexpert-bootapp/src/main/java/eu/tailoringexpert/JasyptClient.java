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
package eu.tailoringexpert;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.IParameterSplitter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Client to use to encrypt value with jasypt to be used by boot-app.
 *
 * @author Michael Bädorf
 */
@Log4j2
public class JasyptClient {


    public static JasyptClient createInstance() {
        return new JasyptClient();
    }

    public static void main(String[] args) {
        JasyptConfig config = new JasyptConfig();
        JasyptParameter parameter = new JasyptParameter();
        JasyptClient client = createInstance();

        JCommander.newBuilder()
            .addObject(new Object[]{config, parameter})
            .build()
            .parse(args);

        parameter.getParameters()
            .forEach(toEncrypt -> log.info(toEncrypt + " -> " + client.encrypt(config, toEncrypt)));
    }

    public String encrypt(JasyptConfig config, String parameter) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(config.getAlgorithm());
        encryptor.setPassword(config.getPassword());
        return encryptor.encrypt(parameter);
    }

    public String decrypt(JasyptConfig config, String parameter) {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm(config.getAlgorithm());
        encryptor.setPassword(config.getPassword());
        return encryptor.decrypt(parameter);
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JasyptConfig {
        @Getter
        @Parameter(names = {"-algorithm", "--algorithm"}, description = "Algorithm for encryption")
        private String algorithm = "PBEWithMD5AndTripleDES";

        @Getter
        @Parameter(names = {"-password", "--password"}, description = "Password to use for encryption", required = true)
        private String password;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JasyptParameter {
        @Getter
        @Parameter(names = {"-parameter", "--parameter"}, description = "Parameter to be encrypted", required = true, splitter = PipeParameterSplitter.class)
        private List<String> parameters;
    }


    public static class PipeParameterSplitter implements IParameterSplitter {
        public List<String> split(String value) {
            return Arrays.asList(value.split(Pattern.quote("|")));

        }
    }

}
