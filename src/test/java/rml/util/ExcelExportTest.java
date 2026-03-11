package rml.util;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ExcelExportTest {

    /**
     * Minimal bean whose every declared field has a non-null getter.
     * ExcelExport uses reflection to invoke getXxx() for each declared field;
     * any null return causes an NPE in the current implementation, so all
     * fields here are initialised.
     */
    public static class SimpleBean {
        private String name;
        private String code;

        public SimpleBean(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() { return name; }
        public String getCode() { return code; }
    }

    // --- basic export ---

    @Test
    public void exportExcel_producesNonEmptyOutputStream() throws Exception {
        ExcelExport<SimpleBean> exporter = new ExcelExport<SimpleBean>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<SimpleBean> data = Arrays.asList(new SimpleBean("Widget", "W001"));
        String[] headers = {"Name", "Code"};

        exporter.exportExcel("TestSheet", headers, data, out, "yyyy-MM-dd");

        assertTrue("Output stream should contain bytes", out.size() > 0);
    }

    @Test
    public void exportExcel_sheetNameMatchesTitle() throws Exception {
        ExcelExport<SimpleBean> exporter = new ExcelExport<SimpleBean>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<SimpleBean> data = Arrays.asList(new SimpleBean("Alpha", "A1"));
        String[] headers = {"Name", "Code"};

        exporter.exportExcel("MyReport", headers, data, out, null);

        HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        assertEquals("MyReport", wb.getSheetName(0));
    }

    @Test
    public void exportExcel_headerRowContainsExpectedValues() throws Exception {
        ExcelExport<SimpleBean> exporter = new ExcelExport<SimpleBean>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<SimpleBean> data = Arrays.asList(new SimpleBean("Gadget", "G99"));
        String[] headers = {"Item Name", "Item Code"};

        exporter.exportExcel("Headers", headers, data, out, null);

        HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow headerRow = sheet.getRow(0);

        assertEquals("Item Name", headerRow.getCell(0).getStringCellValue());
        assertEquals("Item Code", headerRow.getCell(1).getStringCellValue());
    }

    @Test
    public void exportExcel_dataRowCountMatchesDatasetSize() throws Exception {
        ExcelExport<SimpleBean> exporter = new ExcelExport<SimpleBean>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<SimpleBean> data = Arrays.asList(
            new SimpleBean("A", "1"),
            new SimpleBean("B", "2"),
            new SimpleBean("C", "3")
        );
        String[] headers = {"Name", "Code"};

        exporter.exportExcel("Data", headers, data, out, null);

        HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        HSSFSheet sheet = wb.getSheetAt(0);
        // row 0 = header, rows 1-3 = data
        assertEquals(4, sheet.getPhysicalNumberOfRows());
    }

    @Test
    public void exportExcel_emptyDataset_onlyWritesHeaderRow() throws Exception {
        ExcelExport<SimpleBean> exporter = new ExcelExport<SimpleBean>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<SimpleBean> data = Arrays.<SimpleBean>asList();
        String[] headers = {"Name", "Code"};

        exporter.exportExcel("Empty", headers, data, out, null);

        HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        HSSFSheet sheet = wb.getSheetAt(0);
        assertEquals(1, sheet.getPhysicalNumberOfRows());
    }
}
