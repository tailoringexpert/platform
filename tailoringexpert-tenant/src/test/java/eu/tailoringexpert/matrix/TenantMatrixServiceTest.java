/*-
 * #%L
 * TailoringExpert
 * %%
 * Copyright (C) 2022 - 2026 Michael Bädorf and others
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
package eu.tailoringexpert.matrix;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import eu.tailoringexpert.TenantContext;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.MatrixFile;
import eu.tailoringexpert.domain.MatrixFileMeta;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import tools.jackson.databind.ObjectMapper;

@Log4j2
class TenantMatrixServiceTest {
    String basedir;

    MessageDigest md;
    ObjectMapper objectMapperMock;
    Function<Path, Optional<File>> downloadMock;
    TenantMatrixService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setup() throws Exception {
        Dotenv env = Dotenv.configure().systemProperties().ignoreIfMissing().load();
        this.basedir = env.get("MATRIX_HOME", "target/matrix");
        this.objectMapperMock = mock(ObjectMapper.class);
        this.md = MessageDigest.getInstance("SHA-256");
        this.downloadMock = mock(Function.class);
        this.service = new TenantMatrixService(this.basedir, this.md, this.objectMapperMock, this.downloadMock);
    }

    @AfterEach
    void afterEach() {
        Path dir = Path.of(this.basedir + "/platform");
        if (dir.toFile().exists()) {
            Stream.of(dir.toFile().listFiles())
                    .forEach(java.io.File::delete);
            dir.toFile().delete();
        }

    }

    @Test
    void list_DirDoesNotExists_EmptyReturned() {
        // arrange

        // act
        Collection<MatrixFileMeta> actual;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class);
                MockedStatic<Paths> p = mockStatic(Paths.class)) {
            p.when(() -> Paths.get(this.basedir, "platform"))
                    .thenReturn(Path.of("nonExistingDir"));
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.list();

            p.reset();
        }

        // assert
        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void list_NoMatrices_EmptyCollectionReurned() {
        // arrange

        // act
        Collection<MatrixFileMeta> actual;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.list();
        }

        // assert
        assertThat(actual)
                .isNotNull()
                .isEmpty();
    }

    @Test
    void list_MatricesExist_CollectionReturned() throws IOException {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));
        Files.createFile(Paths.get(this.basedir, "platform", "matrix1.xlsx.json"));
        Files.createFile(Paths.get(this.basedir, "platform", "matrix2.xlsx.json"));

        given(objectMapperMock.readValue(any(java.io.File.class), eq(MatrixFileMeta.class)))
                .willAnswer(invocation -> MatrixFileMeta.builder()
                        .name(((java.io.File) invocation.getArgument(0)).getName()).build());

        // act
        Collection<MatrixFileMeta> actual;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.list();
        }

        // assert
        assertThat(actual)
                .isNotNull()
                .hasSize(2);
    }

    void save_CreateDirectoryMockedExcepion_RuntimeExceptionIsThrown() {
        // arrange
        MatrixFile toSave = MatrixFile.builder().build();

        // act
        Throwable actual = null;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class);
                MockedStatic<Files> files = mockStatic(Files.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            files.when(() -> Files.createDirectories(Path.of(this.basedir + "/platform")))
                    .thenThrow(new IOException("Mocked createDirectories Exception"));
            actual = catchThrowable(() -> service.save(toSave));

        }

        // assert
        assertThat(actual)
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void save_WriteMetaJsonMockedException_RuntimeExceptionIsThrown() throws Exception {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));

        MatrixFile toSave = MatrixFile.builder()
                .name("MATRIX01.xlsx")
                .data("some dummy content".getBytes(UTF_8))
                .build();

        given(objectMapperMock.writeValueAsString(any())).willReturn("""
                {\r
                  "description" : "A dummy description",\r
                  "name" : "MATRIX01.xlsx",\r
                  "data" : null,\r
                  "hash" : "aafc9cfe7f88f054340593dfe872472c17c5386d7c228bd74bb88c64c633a62f",\r
                  "creationTimestamp" : null\r
                }""");

        // act
        Throwable actual = null;
        try (MockedStatic<Files> files = mockStatic(Files.class)) {
            files.when(() -> Files.writeString(eq(Path.of(this.basedir + "/platform/MATRIX01.xlsx.json")), any()))
                    .thenThrow(new IOException("Mocked writeString Exception"));
            actual = catchThrowable(() -> service.save(toSave));

        }

        // assert
        assertThat(actual).isInstanceOf(RuntimeException.class);
    }

    @Test
    void save_ValidMatrixFile_FilesSaved() {
        // arrange
        byte[] data = "some dummy content".getBytes(UTF_8);

        MatrixFile toSave = MatrixFile.builder()
                .name("MATRIX01.xlsx")
                .data(data)
                .build();

        given(objectMapperMock.writeValueAsString(any())).willReturn("""
                {\r
                  "description" : "A dummy description",\r
                  "name" : "MATRIX01.xlsx",\r
                  "data" : null,\r
                  "hash" : "aafc9cfe7f88f054340593dfe872472c17c5386d7c228bd74bb88c64c633a62f",\r
                  "creationTimestamp" : null\r
                }""");

        // act
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            service.save(toSave);
        }

        // assert
        assertThat(Paths.get(this.basedir, "platform", "MATRIX01.xlsx").toAbsolutePath())
                .exists();
        assertThat(Paths.get(this.basedir, "platform", "MATRIX01.xlsx.json").toAbsolutePath())
                .exists();
    }

    @Test
    void delete_FileAndMetaExists_TrueReturnedBothFilesDeleted() throws Exception {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));

        Path file = Paths.get(this.basedir, "platform", "matrix01.xlsx").toAbsolutePath();
        file.toFile().createNewFile();
        assert file.toFile().exists();

        Path hash = Paths.get(this.basedir, "platform", "matrix01.xlsx.json").toAbsolutePath();
        hash.toFile().createNewFile();
        assert hash.toFile().exists();

        // act
        boolean actual = false;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.delete("matrix01.xlsx");
        }

        // assert
        assertThat(actual).isTrue();
        assertThat(file.toFile()).doesNotExist();
        assertThat(hash.toFile()).doesNotExist();
    }

    @Test
    void delete_FileNoMetaExists_TrueReturnedFileDeleted() throws Exception {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));

        Path file = Paths.get(this.basedir, "platform", "matrix01.xlsx").toAbsolutePath();
        file.toFile().createNewFile();
        assert file.toFile().exists();

        Path meta = Paths.get(this.basedir, "platform", "matrix01.xlsx.json").toAbsolutePath();
        meta.toFile().delete();
        assert !meta.toFile().exists();

        // act
        boolean actual = false;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.delete("matrix01.xlsx");
        }

        // assert
        assertThat(actual).isTrue();
        assertThat(file.toFile()).doesNotExist();
        assertThat(meta.toFile()).doesNotExist();
    }

    @Test
    void delete_FileNotExistMetaExists_TrueReturnedMetaDeleted() throws Exception {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));

        Path file = Paths.get(this.basedir, "platform", "matrix01.xlsx").toAbsolutePath();
        file.toFile().delete();
        assert !file.toFile().exists();

        Path meta = Paths.get(this.basedir, "platform", "matrix01.xlsx.json").toAbsolutePath();
        meta.toFile().createNewFile();
        assert meta.toFile().exists();

        // act
        boolean actual = false;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            actual = service.delete("matrix01.xlsx");
        }

        // assert
        assertThat(actual).isTrue();
        assertThat(file.toFile()).doesNotExist();
        assertThat(meta.toFile()).doesNotExist();
    }

    @Test
    void delete_MockedExceptionWithDeletingFile_IOExceptionThrown() throws Exception {
        // arrange
        Files.createDirectories(get(this.basedir, "platform"));

        Path file = Paths.get(this.basedir, "platform", "matrix01.xlsx").toAbsolutePath();
        file.toFile().delete();
        assert !file.toFile().exists();

        // act
        Throwable actual = null;
        try (MockedStatic<TenantContext> tc = mockStatic(TenantContext.class);
                MockedStatic<Files> files = mockStatic(Files.class)) {
            tc.when(TenantContext::getCurrentTenant).thenReturn("platform");
            files.when(() -> Files.deleteIfExists(file))
                    .thenThrow(new IOException("Mocked deleteIfExists Exception"));
            actual = catchThrowable(() -> service.delete("matrix01.xlsx"));
        }

        // assert
        assertThat(actual).isInstanceOf(IOException.class);

    }
}
