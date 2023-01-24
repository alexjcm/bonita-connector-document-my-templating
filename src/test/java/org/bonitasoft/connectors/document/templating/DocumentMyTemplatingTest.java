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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.api.APIAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.document.impl.DocumentImpl;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentMyTemplatingTest {

    private final long processInstanceId = 4861356546L;
    @Mock
    private APIAccessor apiAccessor;
    @Mock
    private EngineExecutionContext engineExecutionContext;
    @Mock
    private ProcessAPI processAPI;
    @Spy
    @InjectMocks
    private DocumentMyTemplating documentTemplating;

    @BeforeEach
    public void before() {
        doReturn(processAPI).when(apiAccessor).getProcessAPI();
        doReturn(processInstanceId).when(engineExecutionContext).getProcessInstanceId();
    }

    @Test
    void should_execute_return_result_of_convert_method() throws ConnectorException, DocumentNotFoundException {
        //given
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("doc.docx");
        document.setContentStorageId("TheStorageID");
        byte[] content = new byte[] { 4, 5, 6 };
        final byte[] contentAfter = { 1, 2, 3 };
        final List<List<Object>> replacements = Collections.singletonList(Arrays.asList("theKey", (Object) "theValue"));
        doReturn(contentAfter).when(documentTemplating).applyReplacements(content, replacements, false);
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");
        doReturn(content).when(processAPI).getDocumentContent("TheStorageID");

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        //when
        final Map<String, Object> execute = documentTemplating.execute();

        //then
        assertThat(execute).containsOnlyKeys(DocumentMyTemplating.OUTPUT_DOCUMENT);
        assertThat(execute.get(DocumentMyTemplating.OUTPUT_DOCUMENT))
                .isEqualToComparingFieldByField(new DocumentValue(contentAfter, "theMimeType", "doc.docx"));
    }

    @Test
    void should_execute_return_result_of_convert_method_with_outputFileName()
            throws ConnectorException, DocumentNotFoundException {
        //given
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("doc.docx");
        document.setContentStorageId("TheStorageID");
        byte[] content = new byte[] { 4, 5, 6 };
        final byte[] contentAfter = { 1, 2, 3 };
        final List<List<Object>> replacements = Collections.singletonList(Arrays.asList("theKey", (Object) "theValue"));
        doReturn(contentAfter).when(documentTemplating).applyReplacements(content, replacements, false);
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");
        doReturn(content).when(processAPI).getDocumentContent("TheStorageID");

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);
        parameters.put(DocumentMyTemplating.INPUT_RESULTING_DOC_FILENAME, "outDocument.docx");

        documentTemplating.setInputParameters(parameters);

        //when
        final Map<String, Object> execute = documentTemplating.execute();

        //then
        assertThat(execute).containsOnlyKeys(DocumentMyTemplating.OUTPUT_DOCUMENT);
        assertThat(execute.get(DocumentMyTemplating.OUTPUT_DOCUMENT))
                .isEqualToComparingFieldByField(new DocumentValue(contentAfter, "theMimeType", "outDocument.docx"));
    }

    @Test
    void should_execute_throw_exception_when_document_not_found()
            throws ConnectorException, DocumentNotFoundException {
        //given
        final Map<String, String> replacements = Collections.singletonMap("theKey", "theValue");
        doThrow(new DocumentNotFoundException("")).when(processAPI).getLastDocument(anyLong(), anyString());
        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);
        documentTemplating.setInputParameters(parameters);

        //then
        assertThrows(ConnectorException.class, () -> documentTemplating.execute());
    }
/*
    @Test
    void process_docx_document()
            throws ConnectorException, DocumentNotFoundException, IOException, XDocReportException {
        //given
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("doc.docx");
        document.setContentStorageId("TheStorageID");
        byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream("/velocitytest.docx"));

        final List<List<Object>> replacements = new ArrayList<>();

        replacements.add(Arrays.asList("champ", (Object) "FIELD"));
        replacements.add(Arrays.asList("espace", (Object) "SPPPPPAAAAAAACeeee"));
        replacements.add(Arrays.asList("MyField", (Object) "Mon champ :)\n toto"));
        Project project = new Project("The project name");
        replacements.add(Arrays.asList("project", project));
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");
        doReturn(content).when(processAPI).getDocumentContent("TheStorageID");

        final HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        //when
        final Map<String, Object> execute = documentTemplating.execute();

        //then
        assertThat(execute).containsOnlyKeys(DocumentMyTemplating.OUTPUT_DOCUMENT);
        final IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
                new ByteArrayInputStream(
                        ((DocumentValue) execute.get(DocumentMyTemplating.OUTPUT_DOCUMENT)).getContent()),
                TemplateEngineKind.Velocity);
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Options options = Options.getTo(ConverterTypeTo.XHTML).via(
                    ConverterTypeVia.XWPF);
            report.convert(report.createContext(), options, out);
            final String actual = new String(out.toByteArray());
            assertThat(actual).contains("mon FIELD avec SPPPPPAAAAAAACeeee");
            assertThat(actual).contains("The project name");
            assertThat(actual).contains("Mon champ :)");
            assertThat(actual).contains("toto");
            assertThat(actual).contains("my task");
            assertThat(actual).contains("[my task, another task, last task]");
            assertThat(actual).contains("[another task, last task, my task]");
        }
    }
*/
    @Test
   void should_sanitize_input_with_invalid_char_for_docx() throws Exception {
        //given
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("template.docx");
        document.setContentStorageId("TheStorageID");
        byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream("/template.docx"));

        List<List<Object>> replacements = new ArrayList<>();
        replacements.add(Arrays.asList("field", (Object) "invalidchar")); // There is an invalid char between 'd' and 'c' -> 0x19 invalidchar
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");
        doReturn(content).when(processAPI).getDocumentContent("TheStorageID");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        //when
        Map<String, Object> res = documentTemplating.execute();

        //then
        assertThat(res).containsOnlyKeys(DocumentMyTemplating.OUTPUT_DOCUMENT);
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
                new ByteArrayInputStream(((DocumentValue) res.get(DocumentMyTemplating.OUTPUT_DOCUMENT)).getContent()),
                TemplateEngineKind.Velocity);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Options options = Options.getTo(ConverterTypeTo.XHTML).via(ConverterTypeVia.XWPF);
            report.convert(report.createContext(), options, out);
            String actual = new String(out.toByteArray());
            assertThat(actual).doesNotContain("invalidchar");// There is an invalid char between 'd' and 'c' -> 0x19 invalidchar
            assertThat(actual).contains("invalidchar");
        }
    }

    @Test
    void should_sanitize_input_with_invalid_char_for_odt() throws Exception {
        //given
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("template.odt");
        document.setContentStorageId("TheStorageID");
        byte[] content = IOUtils.toByteArray(this.getClass().getResourceAsStream("/template.odt"));

        List<List<Object>> replacements = new ArrayList<>();
        replacements.add(Arrays.asList("field", (Object) "invalidchar")); // There is an invalid char between 'd' and 'c' -> 0x19 invalidchar
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");
        doReturn(content).when(processAPI).getDocumentContent("TheStorageID");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        //when
        Map<String, Object> res = documentTemplating.execute();

        //then
        assertThat(res).containsOnlyKeys(DocumentMyTemplating.OUTPUT_DOCUMENT);
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(
                new ByteArrayInputStream(((DocumentValue) res.get(DocumentMyTemplating.OUTPUT_DOCUMENT)).getContent()),
                TemplateEngineKind.Velocity);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Options options = Options.getTo(ConverterTypeTo.XHTML);
            report.convert(report.createContext(), options, out);
            String actual = new String(out.toByteArray());
            assertThat(actual).doesNotContain("invalidchar");// There is an invalid char between 'd' and 'c' -> 0x19 invalidchar
            assertThat(actual).contains("invalidchar");
        }
    }

    @Test
    void should_not_validate_unsuported_documents() throws Exception {
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("template.txt");
        document.setContentStorageId("TheStorageID");

        List<List<Object>> replacements = new ArrayList<>();
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        //then
        assertThrows(ConnectorValidationException.class, () -> documentTemplating.validateInputParameters());
    }

    @Test
    void should_validate_suported_documents() throws Exception {
        DocumentImpl document = new DocumentImpl();
        document.setContentMimeType("theMimeType");
        document.setFileName("template.odt");
        document.setContentStorageId("TheStorageID");

        List<List<Object>> replacements = new ArrayList<>();
        doReturn(document).when(processAPI).getLastDocument(processInstanceId, "documentName");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put(DocumentMyTemplating.INPUT_DOCUMENT_INPUT, "documentName");
        parameters.put(DocumentMyTemplating.INPUT_REPLACEMENTS, replacements);

        documentTemplating.setInputParameters(parameters);

        documentTemplating.validateInputParameters();

        document.setFileName("template.docx");
        documentTemplating.validateInputParameters();
    }

    @Test
    void should_detect_corrupted_documents() throws Exception {
        Path fileCorrupted = new File(this.getClass().getResource("/corrupted.xml").toURI()).toPath();
        Path fileNotCorrupted = new File(this.getClass().getResource("/notCorrupted.xml").toURI()).toPath();

        assertThat(documentTemplating.isCorrupted(fileCorrupted)).isTrue();
        assertThat(documentTemplating.isCorrupted(fileNotCorrupted)).isFalse();
    }

    public class Project {

        private String name;

        public Project(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public List<String> getTasks() {
            return Arrays.asList("my task", "another task", "last task");
        }
    }

}
