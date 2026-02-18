package api.utilities;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XLUtility {

    private String path;
    private FileInputStream file;
    private Workbook workbook;
    private Sheet sheet;

    public XLUtility(String path) {
        this.path = path;
    }

    // Get total rows
    public int getRowCount(String sheetName) throws IOException {
        file = new FileInputStream(path);
        workbook = new XSSFWorkbook(file);
        sheet = workbook.getSheet(sheetName);

        int rowCount = sheet.getLastRowNum();
        workbook.close();
        file.close();

        return rowCount;
    }

    // Get total columns
    public int getCellCount(String sheetName, int rowNum) throws IOException {
        file = new FileInputStream(path);
        workbook = new XSSFWorkbook(file);
        sheet = workbook.getSheet(sheetName);

        Row row = sheet.getRow(rowNum);
        int cellCount = row.getLastCellNum();

        workbook.close();
        file.close();

        return cellCount;
    }

    // Get cell data (handles different data types)
    public String getCellData(String sheetName, int rowNum, int colNum) throws IOException {

        file = new FileInputStream(path);
        workbook = new XSSFWorkbook(file);
        sheet = workbook.getSheet(sheetName);

        Row row = sheet.getRow(rowNum);
        Cell cell = row.getCell(colNum);

        String data;

        if (cell.getCellType() == CellType.STRING) {
            data = cell.getStringCellValue();
        }
        else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                data = cell.getDateCellValue().toString();
            } else {
                data = String.valueOf((long) cell.getNumericCellValue());
            }
        }
        else if (cell.getCellType() == CellType.BOOLEAN) {
            data = String.valueOf(cell.getBooleanCellValue());
        }
        else {
            data = "";
        }

        workbook.close();
        file.close();

        return data;
    }
}
