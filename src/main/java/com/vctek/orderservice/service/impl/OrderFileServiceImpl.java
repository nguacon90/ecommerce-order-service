package com.vctek.orderservice.service.impl;

import com.vctek.orderservice.dto.request.OrderFileParameter;
import com.vctek.orderservice.service.OrderFileService;
import com.vctek.orderservice.util.DownloadRedisLockKey;
import com.vctek.util.DateUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.poi.ss.usermodel.CellType.BLANK;

@Service
public class OrderFileServiceImpl implements OrderFileService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderFileServiceImpl.class);
    private static final String ORDER_FILE_NAME = "order_list.xlsx";
    private static final String ORDER_SPLIT_FILE_NAME = "order_list_{fileNum}.xlsx";
    private String fileRootPath;
    private RedisTemplate redisTemplate;
    private int redisCachedKeyTimeout;

    @Override
    public void writeToFile(byte[] contents, OrderFileParameter orderFileParameter) {
        try {
            Path path = getPathFile(orderFileParameter);
            Files.createDirectories(path.getParent());
            Files.write(path, contents, StandardOpenOption.CREATE);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public byte[] readFile(OrderFileParameter orderFileParameter) {
        try {
            Path path = getPathFile(orderFileParameter);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return new byte[0];
    }

    @Override
    public void deleteFile(OrderFileParameter orderFileParameter) {
        Path path = getPathFile(orderFileParameter);
        Path parent = path.getParent();
        File file = new File(parent.toUri());
        File[] listFiles = file.listFiles();
        if (ArrayUtils.isNotEmpty(listFiles)) {
            Arrays.stream(listFiles).forEach(File::delete);
        }
    }

    @Override
    public boolean isExistedFile(OrderFileParameter orderFileParameter) {
        Path path = getPathFile(orderFileParameter);
        return Files.exists(path, LinkOption.NOFOLLOW_LINKS);
    }

    @Override
    public void mergeFile(OrderFileParameter orderFileParameter, int numberOfFileToMerge) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            orderFileParameter.setFileNum(null);
            Path targetFilePath = getPathFile(orderFileParameter);

            File targetFile = new File(targetFilePath.toUri());
            List<FileInputStream> mergeList = new ArrayList<>();
            for (int i = 0; i <= numberOfFileToMerge; i++) {
                orderFileParameter.setFileNum(i);
                Path sourceFilePath = getPathFile(orderFileParameter);
                FileInputStream inputStream = new FileInputStream(new File(sourceFilePath.toUri()));
                mergeList.add(inputStream);
            }

            String sheetName = "Order " + DateUtil.dateToStr(Calendar.getInstance().getTime(), DateUtil.VN_DATE_PATTERN);
            SXSSFSheet sheet = workbook.createSheet(sheetName);
            for (int j = 0; j <= numberOfFileToMerge; j++) {
                FileInputStream inputStream = mergeList.get(j);
                XSSFWorkbook mergeBook = new XSSFWorkbook(inputStream);
                if (j == 0) {
                    copySheets(workbook, sheet, mergeBook.getSheetAt(0), 0);
                } else {
                    copySheets(workbook, sheet, mergeBook.getSheetAt(0), 1);
                }
                mergeBook.close();
            }

            Long start1 = System.currentTimeMillis();
            LOGGER.info(" ======================== START WRITE FILE ");
            writeFile(workbook, targetFile);
            LOGGER.info("========================  FINISHED WRITE FILE {} time ", System.currentTimeMillis() - start1);

        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isProcessingExportExcel(OrderFileParameter orderFileParameter) {
        String key = generateRedisKey(orderFileParameter);
        if (key == null) return false;
        Object isProcessing = redisTemplate.opsForValue().get(key);
        return Boolean.TRUE.equals(isProcessing);
    }

    @Override
    public void setProcessExportExcel(OrderFileParameter orderFileParameter, boolean isProcessing) {
        String key = generateRedisKey(orderFileParameter);
        if (key == null) return;
        redisTemplate.opsForValue().set(key, isProcessing, redisCachedKeyTimeout, TimeUnit.HOURS);
    }

    private Path getPathFile(OrderFileParameter orderFileParameter) {
        String pathFile = fileRootPath + "/orderList";
        if (orderFileParameter.getFileNum() != null) {
            String fileName = ORDER_SPLIT_FILE_NAME.replace("{fileNum}", orderFileParameter.getFileNum().toString());
            return Paths.get(generatePathFile(orderFileParameter, fileName, pathFile));
        }

        return Paths.get(generatePathFile(orderFileParameter, ORDER_FILE_NAME, pathFile));
    }


    private String generatePathFile(OrderFileParameter orderFileParameter, String fileName, String pathFile) {
        return pathFile + "/" + orderFileParameter.getOrderType() + "/" + orderFileParameter.getUserId() + "/" + fileName;
    }

    private void writeFile(SXSSFWorkbook book, File file) throws IOException {
        FileOutputStream out = new FileOutputStream(file);
        book.write(out);
        out.close();
    }

    private void copySheets(SXSSFWorkbook newWorkbook, SXSSFSheet newSheet, XSSFSheet sheet, int ignoreRowNumber) {
        copySheets(newWorkbook, newSheet, sheet, false, ignoreRowNumber);
    }

    private void copySheets(SXSSFWorkbook newWorkbook, SXSSFSheet newSheet, XSSFSheet sheet, boolean copyStyle, int ignoreRowNumber) {
        int newRownumber = ignoreRowNumber == 0 ? 0 : newSheet.getLastRowNum() + 1;
        int maxColumnNum = 0;
        Map<Integer, CellStyle> styleMap = (copyStyle) ? new HashMap<>() : null;

        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            if (ignoreRowNumber > 0 && i < ignoreRowNumber) {
                continue;
            }
            XSSFRow srcRow = sheet.getRow(i);
            SXSSFRow destRow = newSheet.createRow(i - ignoreRowNumber + newRownumber);
            if (srcRow != null) {
                copyRow(newWorkbook, sheet, newSheet, srcRow, destRow, styleMap);
                if (srcRow.getLastCellNum() > maxColumnNum) {
                    maxColumnNum = srcRow.getLastCellNum();
                }
            }
        }
        for (int i = 0; i <= maxColumnNum; i++) {
            newSheet.setColumnWidth(i, sheet.getColumnWidth(i));
        }
    }

    private void copyRow(SXSSFWorkbook newWorkbook, XSSFSheet srcSheet, SXSSFSheet destSheet,
                         XSSFRow srcRow, SXSSFRow destRow, Map<Integer, CellStyle> styleMap) {
        destRow.setHeight(srcRow.getHeight());
        for (int j = srcRow.getFirstCellNum(); j <= srcRow.getLastCellNum(); j++) {
            XSSFCell oldCell = srcRow.getCell(j);
            SXSSFCell newCell = destRow.getCell(j);
            if (oldCell != null) {
                if (newCell == null) {
                    newCell = destRow.createCell(j);
                }
                copyCell(newWorkbook, oldCell, newCell, styleMap);
            }
        }
    }

    private void copyCell(SXSSFWorkbook newWorkbook, XSSFCell oldCell, SXSSFCell newCell, Map<Integer, CellStyle> styleMap) {
        if (styleMap != null) {
            int stHashCode = oldCell.getCellStyle().hashCode();
            CellStyle newCellStyle = styleMap.get(stHashCode);
            if (newCellStyle == null) {
                newCellStyle = newWorkbook.createCellStyle();
                newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
                styleMap.put(stHashCode, newCellStyle);
            }
            newCell.setCellStyle(newCellStyle);
        }
        switch (oldCell.getCellType()) {
            case STRING:
                newCell.setCellValue(oldCell.getRichStringCellValue());
                break;
            case NUMERIC:
                newCell.setCellValue(oldCell.getNumericCellValue());
                break;
            case BLANK:
                newCell.setCellType(BLANK);
                break;
            case BOOLEAN:
                newCell.setCellValue(oldCell.getBooleanCellValue());
                break;
            case ERROR:
                newCell.setCellErrorValue(oldCell.getErrorCellValue());
                break;
            case FORMULA:
                newCell.setCellFormula(oldCell.getCellFormula());
                break;
            default:
                break;
        }
    }

    private String generateRedisKey(OrderFileParameter orderFileParameter) {
        return DownloadRedisLockKey.DOWNLOAD_ORDER.key()
                .replace(DownloadRedisLockKey.ORDER_TYPE_PATTEN.key(), orderFileParameter.getOrderType())
                .replace(DownloadRedisLockKey.USER_ID_PATTEN.key(), orderFileParameter.getUserId().toString());
    }


    @Value("${vctek.downloadPath:/opt/download}")
    public void setFileRootPath(String fileRootPath) {
        this.fileRootPath = fileRootPath;
    }

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${vctek.config.redis.cachedKeyTimeout:4}")
    public void setRedisCachedKeyTimeout(int redisCachedKeyTimeout) {
        this.redisCachedKeyTimeout = redisCachedKeyTimeout;
    }
}
