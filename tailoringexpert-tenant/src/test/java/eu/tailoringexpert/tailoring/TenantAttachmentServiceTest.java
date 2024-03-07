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

import eu.tailoringexpert.domain.File;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Paths.get;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@Log4j2
class TenantAttachmentServiceTest {

    BiFunction<String, String, Path> pathProviderMock;
    String basedir;

    MessageDigest md;
    TenantAttachmentService service;

    @BeforeEach
    void setup() throws Exception {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();
        this.basedir = env.get("ATTACHMENT_HOME", "target/attachments");

        this.pathProviderMock = mock(BiFunction.class);
        this.md = MessageDigest.getInstance("SHA-256");
        this.service = new TenantAttachmentService(this.pathProviderMock, this.md);
    }

    @AfterEach
    void afterEach() {
        Path dir = Path.of(this.basedir + "/PLATFORM/test/1000");
        if (dir.toFile().exists()) {
            Stream.of(dir.toFile().listFiles())
                .forEach(java.io.File::delete);
            dir.toFile().delete();
        }

    }


    @Test
    void save_CreateDirectoryMockedExcepion_RuntimeExceptionIsThrown() {
        // arrange
        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        File toSave = File.builder()
            .name("dummy.pdf")
            .build();

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.createDirectories(Path.of(this.basedir + "/PLATFORM/test/1000")))
                .thenThrow(new IOException("Mocked createDirectories Exception"));
            actual = catchThrowable(() -> service.save("test", "master", toSave));

        }

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void save_WriteStringMockedException_RuntimeExceptionIsThrown() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        File toSave = File.builder()
            .name("dummy.pdf")
            .data("dummy content".getBytes(StandardCharsets.UTF_8))
            .build();

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.writeString(eq(Path.of(this.basedir + "/PLATFORM/test/1000/dummy.pdf.hash")), any()))
                .thenThrow(new IOException("Mocked writeString Exception"));
            actual = catchThrowable(() -> service.save("test", "master", toSave));

        }

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void save_NoExistingTailoring_EmptyReturned() {
        // arrange
        File toSave = File.builder()
            .name("dummy.pdf")
            .build();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(null);

        // act
        Optional<File> actual = service.save("test", "master", toSave);

        // assert
        assertThat(actual).isEmpty();
    }

    @Test
    void save() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/PLATFORM/test/1000/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }
        File toSave = File.builder()
            .name("screeningsheet.pdf")
            .data(data)
            .build();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));


        // act
        Optional<File> actual = service.save("test", "master", toSave);

        // assert
        assertThat(actual).isNotEmpty();
        assertThat(actual.get().getHash())
            .isNotNull()
            .isNotBlank();
        log.debug(actual);

    }

    @Test
    void load_InputStreamExecptionMocked() throws IOException {
        // arrange
        Path file = Paths.get(this.basedir + "/PLATFORM/test/1000/dummy.pdf");

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.newInputStream(file))
                .thenThrow(new IOException("Mocked newInputStream Exception"));
            actual = catchThrowable(() -> service.load("test", "master", "dummy.pdf"));
        }

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }


    @Test
    void load() throws IOException {
        // arrange
        byte[] data;
        try (InputStream is = newInputStream(get("src/test/resources/PLATFORM/test/1000/screeningsheet.pdf"))) {
            assert nonNull(is);
            data = is.readAllBytes();
        }

        Path src = Paths.get("src/test/resources/PLATFORM/test/1000/screeningsheet.pdf");
        Path target = Paths.get(this.basedir, "PLATFORM", "test", "1000", "screeningsheet.pdf");
        Files.createDirectories(target.getParent());
        Files.copy(src, target, REPLACE_EXISTING);

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Optional<File> actual = service.load("test", "master", "screeningsheet.pdf");

        // assert
        assertThat(actual).isPresent();
        assertThat(actual.get().getName()).isEqualTo("screeningsheet.pdf");
        assertThat(actual.get().getData()).isEqualTo(data);

    }

    @Test
    void list_MockedReadString_NullPointerExceptionThrown() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        Path file = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf");
        file.toFile().createNewFile();
        assert file.toFile().exists();

        Path hashFile = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf.hash");
        hashFile.toFile().createNewFile();
        assert hashFile.toFile().exists();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.readString(hashFile))
                .thenThrow(new IOException("Mocked readHash Exception"));
            actual = catchThrowable(() -> service.list("test", "master"));
        }

        // assert
        assertThat(actual).isInstanceOf(NullPointerException.class);

    }

    @Test
    void list_NonExistingTailoringDir_EmptyCollectionReturned() throws IOException {
        // arrange
        Path dir = Paths.get(this.basedir, "PLATFORM", "test", "1000");
        assert !dir.toFile().exists();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Collection<File> actual = service.list("test", "master");

        // assert
        log.debug(actual);
        assertThat(actual).isEmpty();
    }

    @Test
    void list() throws IOException {
        // arrange
        Path src = Paths.get("src/test/resources/PLATFORM/test/1000/screeningsheet.pdf");
        Path target = Paths.get(this.basedir, "PLATFORM", "test", "1000", "screeningsheet.pdf");
        Files.createDirectories(target.getParent());
        Files.copy(src, target, REPLACE_EXISTING);

        src = Paths.get("src/test/resources/PLATFORM/test/1000/screeningsheet.pdf.hash");
        target = Paths.get(this.basedir, "PLATFORM", "test", "1000", "screeningsheet.pdf.hash");
        Files.createDirectories(target.getParent());
        Files.copy(src, target, REPLACE_EXISTING);

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Collection<File> actual = service.list("test", "master");

        // assert
        log.debug(actual);
        assertThat(actual).isNotEmpty();
        ArrayList<File> files = new ArrayList<>(actual);
        assertThat(files.get(0).getHash()).isNotNull();

        actual.stream().forEach(file -> log.debug(file.getName()));

    }

    @Test
    void delete_FileAndHashExists_TrueReturnedBothFilesDeleted() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        Path file = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf");
        file.toFile().createNewFile();
        assert file.toFile().exists();

        Path hash = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf.hash");
        hash.toFile().createNewFile();
        assert hash.toFile().exists();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        boolean actual = service.delete("test", "master", "dummy.pdf");

        // assert
        assertThat(actual).isTrue();
        assertThat(file.toFile()).doesNotExist();
        assertThat(hash.toFile()).doesNotExist();
    }

    @Test
    void delete_FileNoHashExists_TrueReturnedFileDeleted() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        Path file = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf");
        file.toFile().createNewFile();
        assert file.toFile().exists();

        Path hash = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf.hash");
        hash.toFile().delete();
        assert !hash.toFile().exists();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        boolean actual = service.delete("test", "master", "dummy.pdf");

        // assert
        assertThat(actual).isTrue();
        assertThat(file.toFile()).doesNotExist();
        assertThat(hash.toFile()).doesNotExist();
    }

    @Test
    void delete_FileNotExistHashExists_FalseReturnedHashDeleted() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        Path file = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf");
        file.toFile().delete();
        assert !file.toFile().exists();

        Path hash = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf.hash");
        hash.toFile().createNewFile();
        assert hash.toFile().exists();

        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        boolean actual = service.delete("test", "master", "dummy.pdf");

        // assert
        assertThat(actual).isFalse();
        assertThat(file.toFile()).doesNotExist();
        assertThat(hash.toFile()).doesNotExist();
    }

    @Test
    void delete_MockedExceptionWithDeletingFile_IOExceptionThrown() throws Exception {
        // arrange
        Files.createDirectories(Paths.get(this.basedir, "PLATFORM", "test", "1000"));

        Path file = Paths.get(this.basedir, "PLATFORM", "test", "1000", "dummy.pdf");
        file.toFile().delete();
        assert !file.toFile().exists();


        given(pathProviderMock.apply("test", "master"))
            .willReturn(Path.of(this.basedir + "/PLATFORM/test/1000"));

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.deleteIfExists(file))
                .thenThrow(new IOException("Mocked deleteIfExists Exception"));
            actual = catchThrowable(() -> service.delete("test", "master", "dummy.pdf"));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);

    }
}
