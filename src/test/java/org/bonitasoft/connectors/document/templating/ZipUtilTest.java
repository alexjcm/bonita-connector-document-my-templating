/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.document.templating;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;

import org.bonitasoft.engine.io.IOUtil;
import org.junit.jupiter.api.Test;

class ZipUtilTest {

    @Test
    void should_unzip_document() throws IOException {
        Path target = null;
        try (ZipInputStream zis = new ZipInputStream(ZipUtilTest.class.getResourceAsStream("/template.docx"))) {
            target = ZipUtil.unzip("testZipOutput", zis);

            assertThat(target).exists();
            assertThat(target.resolve("[Content_Types].xml")).exists();
            assertThat(target.resolve("word")).isNotEmptyDirectory();
        } finally {
            if (target != null) {
                IOUtil.deleteDir(target.toFile());
            }
        }
    }
    
    @Test
    void should_unzip_document_with_empty_folder() throws IOException {
        Path target = null;
        try (ZipInputStream zis = new ZipInputStream(ZipUtilTest.class.getResourceAsStream("/unzipTest.docx"))) {
            target = ZipUtil.unzip("testZipOutput", zis);

            assertThat(target).exists();
            assertThat(target.resolve("emptyFolder")).isEmptyDirectory();
        } finally {
            if (target != null) {
                IOUtil.deleteDir(target.toFile());
            }
        }
    }

    @Test
    void should_normalize_path() {
        File file = new File("");

        String normalizedPath = ZipUtil.normalizePath(Paths.get(file.getAbsolutePath()));

        assertThat(normalizedPath).doesNotContain("\\");
    }

}
