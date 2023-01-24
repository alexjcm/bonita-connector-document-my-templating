/**
 * Copyright (C) 2020 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil extends SimpleFileVisitor<Path> implements java.lang.AutoCloseable {

    public static final int BUFFER_SIZE = 4096;
    private Path source;
    private FileOutputStream fos;
    private ZipOutputStream zos;

    /**
     * 
     * @param source
     * @param target
     * @throws IOException 
     */
    public static void zip(Path source, Path target) throws IOException {
        try (ZipUtil zippingVisitor = new ZipUtil(source, target)) {
            Files.walkFileTree(source, zippingVisitor);
        }
    }

    public ZipUtil(Path source, Path target) throws FileNotFoundException {
        this.source = source;
        fos = new FileOutputStream(target.toFile());
        zos = new ZipOutputStream(fos);
    }

    /**
     * 
     * @param file
     * @param attrs
     * @return
     * @throws IOException 
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (!file.toFile().exists()) {
            throw new IOException("File " + file.toString() + " not found.");
        }
        Path zipEntryPath = source.relativize(file);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (FileInputStream fis = new FileInputStream(file.toFile())) {
            zos.putNextEntry(new ZipEntry(normalizePath(zipEntryPath)));
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();
        }
        return CONTINUE;
    }

    /**
     * Ensure that the zip entry separator is '/', which is not the case by default on windows ('\\'),
     * So the client doesn't have to manage this.
     * 
     * @param path the path to normaliza
     * @return a unix style path
     */
    public static String normalizePath(Path path) {
        return path.toString().replaceAll("\\\\", "/");
    }

    @Override
    public void close() throws IOException {
        zos.close();
        fos.close();
    }

    public static Path unzip(String targetDirName, ZipInputStream zis) throws IOException {
        Path targetDir = Files.createTempDirectory(targetDirName);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            byte[] data = new byte[BUFFER_SIZE];
            File target = targetDir.toFile().toPath().resolve(entry.getName()).toFile();
            target.getParentFile().mkdirs();
            boolean isDirectory = entry.isDirectory();
            if ((isDirectory ? target.mkdirs() : target.createNewFile()) && !isDirectory) {
                writeFile(zis, data, target);
            }
        }
        return targetDir;
    }

    private static void writeFile(ZipInputStream zis, byte[] data, File target) throws IOException {
        int count;
        FileOutputStream fos = new FileOutputStream(target);
        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);) {
            while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
        }
    }

}
