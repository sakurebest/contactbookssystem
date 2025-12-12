package com.example.contactbookssystem.service;

import com.example.contactbookssystem.model.Contact;
import com.example.contactbookssystem.model.ContactMethod;
import com.example.contactbookssystem.model.ContactMethodType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelService {

    public byte[] exportContactsToExcel(List<Contact> contacts) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("联系人");

        // 创建表头
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "姓名", "公司", "职位", "备注", "是否收藏", "电话", "邮箱", "微信", "QQ", "地址"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(createHeaderStyle(workbook));
        }

        // 填充数据
        int rowNum = 1;
        for (Contact contact : contacts) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(contact.getId() != null ? contact.getId() : 0);
            row.createCell(1).setCellValue(contact.getName() != null ? contact.getName() : "");
            row.createCell(2).setCellValue(contact.getCompany() != null ? contact.getCompany() : "");
            row.createCell(3).setCellValue(contact.getPosition() != null ? contact.getPosition() : "");
            row.createCell(4).setCellValue(contact.getNotes() != null ? contact.getNotes() : "");
            row.createCell(5).setCellValue(contact.isBookmarked() ? "是" : "否");

            // 处理联系方式
            String phone = "";
            String email = "";
            String wechat = "";
            String qq = "";
            String address = "";

            if (contact.getContactMethods() != null) {
                for (ContactMethod method : contact.getContactMethods()) {
                    if (method != null && method.getType() != null && method.getValue() != null) {
                        switch (method.getType()) {
                            case PHONE:
                                phone = method.getValue();
                                break;
                            case EMAIL:
                                email = method.getValue();
                                break;
                            case WECHAT:
                                wechat = method.getValue();
                                break;
                            case QQ:
                                qq = method.getValue();
                                break;
                            case ADDRESS:
                                address = method.getValue();
                                break;
                        }
                    }
                }
            }

            row.createCell(6).setCellValue(phone);
            row.createCell(7).setCellValue(email);
            row.createCell(8).setCellValue(wechat);
            row.createCell(9).setCellValue(qq);
            row.createCell(10).setCellValue(address);
        }

        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 写入字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();

        byte[] result = baos.toByteArray();
        System.out.println("生成的Excel文件大小: " + result.length + " bytes");
        return result;
    }

    public List<Contact> importContactsFromExcel(MultipartFile file) throws IOException {
        List<Contact> contacts = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(is);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // 跳过表头
            if (rows.hasNext()) {
                rows.next();
            }

            int rowCount = 0;
            while (rows.hasNext()) {
                Row row = rows.next();
                rowCount++;

                // 跳过空行
                if (isRowEmpty(row)) {
                    continue;
                }

                Contact contact = new Contact();

                // 读取基本数据
                String name = getCellValue(row.getCell(1));
                if (name == null || name.trim().isEmpty()) {
                    System.out.println("第" + rowCount + "行: 姓名为空，跳过");
                    continue;
                }

                contact.setName(name.trim());
                contact.setCompany(getCellValue(row.getCell(2)));
                contact.setPosition(getCellValue(row.getCell(3)));
                contact.setNotes(getCellValue(row.getCell(4)));

                String bookmarked = getCellValue(row.getCell(5));
                contact.setBookmarked("是".equals(bookmarked) || "true".equalsIgnoreCase(bookmarked) || "1".equals(bookmarked));

                // 处理联系方式
                List<ContactMethod> methods = new ArrayList<>();
                boolean hasPrimary = false;

                String phone = getCellValue(row.getCell(6));
                if (phone != null && !phone.trim().isEmpty()) {
                    ContactMethod method = createContactMethod(ContactMethodType.PHONE, phone.trim(), "电话", !hasPrimary);
                    if (!hasPrimary) hasPrimary = true;
                    methods.add(method);
                }

                String email = getCellValue(row.getCell(7));
                if (email != null && !email.trim().isEmpty()) {
                    ContactMethod method = createContactMethod(ContactMethodType.EMAIL, email.trim(), "邮箱", !hasPrimary);
                    if (!hasPrimary) hasPrimary = true;
                    methods.add(method);
                }

                String wechat = getCellValue(row.getCell(8));
                if (wechat != null && !wechat.trim().isEmpty()) {
                    ContactMethod method = createContactMethod(ContactMethodType.WECHAT, wechat.trim(), "微信", !hasPrimary);
                    if (!hasPrimary) hasPrimary = true;
                    methods.add(method);
                }

                String qq = getCellValue(row.getCell(9));
                if (qq != null && !qq.trim().isEmpty()) {
                    ContactMethod method = createContactMethod(ContactMethodType.QQ, qq.trim(), "QQ", !hasPrimary);
                    if (!hasPrimary) hasPrimary = true;
                    methods.add(method);
                }

                String address = getCellValue(row.getCell(10));
                if (address != null && !address.trim().isEmpty()) {
                    ContactMethod method = createContactMethod(ContactMethodType.ADDRESS, address.trim(), "地址", !hasPrimary);
                    if (!hasPrimary) hasPrimary = true;
                    methods.add(method);
                }

                contact.setContactMethods(methods);
                contacts.add(contact);

                System.out.println("导入行" + rowCount + ": " + contact.getName());
            }

            workbook.close();
            System.out.println("成功解析 " + contacts.size() + " 个联系人");
        }

        return contacts;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        if (row.getLastCellNum() <= 0) {
            return true;
        }
        for (int cellNum = 0; cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValue(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private ContactMethod createContactMethod(ContactMethodType type, String value, String label, boolean primary) {
        ContactMethod method = new ContactMethod();
        method.setType(type);
        method.setValue(value);
        method.setLabel(label);
        method.setPrimary(primary);
        return method;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 如果是整数，返回整数；否则返回小数
                    double num = cell.getNumericCellValue();
                    if (num == Math.floor(num)) {
                        return String.valueOf((long) num);
                    } else {
                        return String.valueOf(num);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return cell.getCellFormula();
                    }
                }
            default:
                return "";
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}