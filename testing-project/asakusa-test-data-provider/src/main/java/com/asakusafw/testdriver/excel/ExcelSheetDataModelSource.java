/**
 * Copyright 2011-2017 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.testdriver.excel;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * {@link DataModelSource} from Excel Sheet.
 * @since 0.2.0
 */
@SuppressWarnings("deprecation") // FIXME POI API is currently transitive
public class ExcelSheetDataModelSource implements DataModelSource {

    static final Logger LOG = LoggerFactory.getLogger(ExcelSheetDataModelSource.class);

    private final DataModelDefinition<?> definition;

    private final URI id;

    private final Sheet sheet;

    private final Map<PropertyName, Integer> names;

    private int nextRowNumber;

    /**
     * Creates a new instance.
     * @param definition the model definition
     * @param id sheet ID (nullable)
     * @param sheet target cheet
     * @throws IOException if failed to extract property info from the sheet
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelSheetDataModelSource(DataModelDefinition<?> definition, URI id, Sheet sheet) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        this.definition = definition;
        this.id = id;
        this.sheet = sheet;
        this.names = extractProperties();
    }

    private Map<PropertyName, Integer> extractProperties() throws IOException {
        // first row must be property names
        Row row = sheet.getRow(0);
        if (row == null) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("ExcelSheetDataModelSource.errorInvalidHeader"), //$NON-NLS-1$
                    id));
        }
        nextRowNumber = 1;
        Map<PropertyName, Integer> results = new LinkedHashMap<>();
        for (Iterator<Cell> iter = row.cellIterator(); iter.hasNext();) {
            Cell cell = iter.next();
            CellType type = cell.getCellTypeEnum();
            if (type == CellType.BLANK) {
                continue;
            }
            if (type != CellType.STRING || cell.getStringCellValue().isEmpty()) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("ExcelSheetDataModelSource.errorInvalidHeaderCell"), //$NON-NLS-1$
                        id,
                        cell.getColumnIndex() + 1));
            }
            String name = cell.getStringCellValue();
            PropertyName property = toPropertyName(cell, name);
            if (definition.getType(property) == null) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("ExcelSheetDataModelSource.errorMissingProperty"), //$NON-NLS-1$
                        definition.getModelClass().getName(),
                        property,
                        id,
                        cell.getColumnIndex() + 1));
            }
            results.put(property, cell.getColumnIndex());
        }
        if (results.isEmpty()) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("ExcelSheetDataModelSource.errorEmptyProperty"), //$NON-NLS-1$
                    id));
        }
        return results;
    }

    private static PropertyName toPropertyName(Cell cell, String name) {
        assert cell != null;
        assert name != null;
        String[] words = name.split("(_|-)+"); //$NON-NLS-1$
        return PropertyName.newInstance(words);
    }

    @Override
    public DataModelReflection next() throws IOException {
        while (nextRowNumber <= sheet.getLastRowNum()) {
            Row row = sheet.getRow(nextRowNumber++);
            if (row == null) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("ExcelSheetDataModelSource.warnSkipEmptyRow"), //$NON-NLS-1$
                        id,
                        nextRowNumber));
                continue;
            }
            boolean sawFilled = false;
            ExcelDataDriver driver = new ExcelDataDriver(definition, id);
            for (Map.Entry<PropertyName, Integer> entry : names.entrySet()) {
                Cell cell = row.getCell(entry.getValue(), MissingCellPolicy.CREATE_NULL_AS_BLANK);
                CellType type = cell.getCellTypeEnum();
                if (type == CellType.FORMULA) {
                    evaluateInCell(cell);
                    type = cell.getCellTypeEnum();
                }
                if (type == CellType.ERROR) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("ExcelSheetDataModelSource.errorErroneousCell"), //$NON-NLS-1$
                            id,
                            row.getRowNum() + 1,
                            cell.getColumnIndex() + 1));
                }
                sawFilled |= (type != CellType.BLANK);
                driver.process(entry.getKey(), cell);
            }
            if (sawFilled) {
                return driver.getReflection();
            } else {
                LOG.warn(MessageFormat.format(
                        Messages.getString("ExcelSheetDataModelSource.warnSkipEmptyRow"), //$NON-NLS-1$
                        id,
                        row.getRowNum() + 1));
            }
        }
        return null;
    }

    private void evaluateInCell(Cell cell) throws IOException {
        try {
            Workbook workbook = cell.getSheet().getWorkbook();
            FormulaEvaluator formulaEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
            formulaEvaluator.evaluateInCell(cell);
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("ExcelSheetDataModelSource.errorFailedToResolveFormulaCell"), //$NON-NLS-1$
                    id,
                    cell.getRowIndex() + 1,
                    cell.getColumnIndex() + 1), e);
        }
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
