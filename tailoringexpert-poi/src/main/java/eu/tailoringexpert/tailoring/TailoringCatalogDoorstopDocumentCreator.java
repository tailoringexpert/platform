package eu.tailoringexpert.tailoring;

import eu.tailoringexpert.domain.Chapter;
import eu.tailoringexpert.domain.File;
import eu.tailoringexpert.domain.Tailoring;
import eu.tailoringexpert.domain.TailoringRequirement;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.Objects.nonNull;
import static java.util.stream.IntStream.of;

@Log4j2
public class TailoringCatalogDoorstopDocumentCreator implements DocumentCreator {

    private static int UID = 0;
    private static int LEVEL = 1;
    private static int TEXT = 2;
    private static int REFERENCES = 3;
    private static int LINKS = 4;
    private static int ACTIVE = 5;
    private static int DERIVED = 6;
    private static int HEADER = 7;
    private static int NORMATIVE = 8;
    private static int REVIEWED = 9;

    /**
     * {@inheritDoc}
     */
    @Override
    public File createDocument(String docId, Tailoring tailoring, Map<String, Object> placeholders) {
        log.traceEntry(() -> docId);

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = createSheet(wb, tailoring);

            AtomicInteger i = new AtomicInteger();
            tailoring.getCatalog().getToc().getChapters().forEach(gruppe -> addChapter(gruppe, sheet, i));

//            applyTextStyle(sheet);
//            applyValidationToColumn(sheet, 2);
//            Arrays.stream(new int[]{2, 4, 5}).forEach(column -> applyValidationToColumn(sheet, column));
//            Arrays.stream(new int[]{1, 2, 4, 5}).forEach(sheet::autoSizeColumn);

            byte[] content;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                wb.write(os);
                content = os.toByteArray();
            }

            File result = File.builder().name(docId + ".xlsx").data(content).build();
            log.traceExit();
            return result;
        } catch (Exception e) {
            log.catching(e);
        }
        log.traceExit();
        return null;
    }

    /**
     * Add chapter to sheet object.
     * All subchapter will be evaluated as well.
     *
     * @param chapter chapter evaluate
     * @param sheet   sheet to add elements to
     */
    private void addChapter(Chapter<TailoringRequirement> chapter, Sheet sheet, AtomicInteger number) {
        addRow(sheet, chapter.getName(), chapter.getNumber(), number);

        IntStream.range(0, chapter.getRequirements().size())
            .forEach(index -> addRow(sheet, chapter.getRequirements().get(index), chapter.getNumber() + "." + (index+1), number));


//        chapter.getRequirements().forEach(requirement -> addRow(sheet, requirement));

        chapter.getChapters().forEach(subChapter -> addChapter(subChapter, sheet, number));
    }

    /**
     * Create sheet in workbook.
     *
     * @param wb workbook to add worksheet
     * @return created worksheet
     */
    private Sheet createSheet(Workbook wb, Tailoring tailoring) {
        Sheet result = wb.createSheet(tailoring.getName() + "-" + tailoring.getCatalog().getVersion() + "-IMPORT");

        CellStyle headerCellStyle = wb.createCellStyle();
        headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setVerticalAlignment(VerticalAlignment.TOP);

        Row row = result.createRow((short) 0);
        row.createCell(UID).setCellValue("uid");
        row.getCell(UID).setCellStyle(headerCellStyle);

        row.createCell(LEVEL).setCellValue("level");
        row.getCell(LEVEL).setCellStyle(headerCellStyle);
        row.createCell(TEXT).setCellValue("text");
        row.getCell(TEXT).setCellStyle(headerCellStyle);
        row.createCell(REFERENCES).setCellValue("references");
        row.getCell(REFERENCES).setCellStyle(headerCellStyle);
        row.createCell(LINKS).setCellValue("links");
        row.getCell(LINKS).setCellStyle(headerCellStyle);
        row.createCell(ACTIVE).setCellValue("active");
        row.getCell(ACTIVE).setCellStyle(headerCellStyle);
        row.createCell(DERIVED).setCellValue("derived");
        row.getCell(DERIVED).setCellStyle(headerCellStyle);
        row.createCell(HEADER).setCellValue("header");
        row.getCell(HEADER).setCellStyle(headerCellStyle);
        row.createCell(NORMATIVE).setCellValue("normative");
        row.getCell(NORMATIVE).setCellStyle(headerCellStyle);
        row.createCell(REVIEWED).setCellValue("reviewed");
        row.getCell(REVIEWED).setCellStyle(headerCellStyle);


//        result.setColumnWidth(0, 40 * 256);
//        result.setColumnWidth(3, 60 * 256);
        result.setAutoFilter(new CellRangeAddress(0, 0, 0, 9));

        return result;
    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet    sheet to add row to
     * @param label    value of cell 0
     * @param position value of cell 1
     */
    private void addRow(Sheet sheet, String label, String position, AtomicInteger number) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        row.createCell(UID).setCellValue(String.format("PA%05d", number.addAndGet(1)));
        row.createCell(LEVEL).setCellValue(position + ".0");
        row.createCell(TEXT).setCellValue("");
        row.createCell(REFERENCES).setCellValue("");
        row.createCell(LINKS).setCellValue("");
        row.createCell(ACTIVE).setCellValue(true);
        row.createCell(DERIVED).setCellValue(false);
        row.createCell(HEADER).setCellValue(label);
        row.createCell(NORMATIVE).setCellValue(true);
        row.createCell(REVIEWED).setCellValue("");

    }

    /**
     * Add a row to provided sheet with provided parameters.
     *
     * @param sheet       sheet to add row to
     * @param requirement tailoring requirement to be displayed in row
     */
    private void addRow(Sheet sheet, TailoringRequirement requirement, String position, AtomicInteger number) {
        Row row = sheet.createRow((short) sheet.getLastRowNum() + 1);

        row.createCell(UID).setCellValue(String.format("PA%05d", number.addAndGet(1)));
        row.createCell(LEVEL).setCellValue(position);
        row.createCell(TEXT).setCellValue(requirement.getText());
        row.createCell(REFERENCES).setCellValue("");
        row.createCell(LINKS).setCellValue("");
        row.createCell(ACTIVE).setCellValue(requirement.getSelected() ? true : false);
        row.createCell(DERIVED).setCellValue(false);
        row.createCell(HEADER).setCellValue("");
        row.createCell(NORMATIVE).setCellValue(true);
        row.createCell(REVIEWED).setCellValue("");

    }


    void applyTextStyle(Sheet sheet) {
//        CellStyle wrapStyle = sheet.getWorkbook().createCellStyle();
//        wrapStyle.setWrapText(true);
//        wrapStyle.setVerticalAlignment(VerticalAlignment.TOP);
//        StreamSupport.stream(sheet.spliterator(), false)
//            .skip(1)
//            .filter(row -> nonNull(row.getCell(3)))
//            .forEach(row -> row.getCell(3).setCellStyle(wrapStyle));
    }

    void applyValidationToColumn(Sheet sheet, int column) {
        DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(new String[]{"YES", "NO"});

        CellRangeAddressList addressList = new CellRangeAddressList(1, sheet.getLastRowNum(), column, column);
        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);

        validation.setSuppressDropDownArrow(false);
        validation.setShowErrorBox(true);

        sheet.addValidationData(validation);
    }
}
