package com.example.contactbookssystem.service;

import com.example.contactbookssystem.model.Contact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ImportExportService {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ExcelService excelService;

    public byte[] exportAllContacts() throws IOException {
        List<Contact> contacts = contactService.getAllContacts();
        System.out.println("导出联系人数量: " + contacts.size());
        return excelService.exportContactsToExcel(contacts);
    }

    public byte[] exportBookmarkedContacts() throws IOException {
        List<Contact> contacts = contactService.getBookmarkedContacts();
        System.out.println("导出收藏联系人数量: " + contacts.size());
        return excelService.exportContactsToExcel(contacts);
    }

    @Transactional
    public int importContacts(MultipartFile file) throws IOException {
        List<Contact> contacts = excelService.importContactsFromExcel(file);
        System.out.println("从Excel读取到联系人数量: " + contacts.size());

        int count = 0;
        for (Contact contact : contacts) {
            // 检查是否已存在相同姓名的联系人
            List<Contact> existingContacts = contactService.searchContacts(contact.getName());
            if (existingContacts.isEmpty()) {
                contactService.saveContact(contact);
                count++;
                System.out.println("导入联系人: " + contact.getName());
            } else {
                System.out.println("跳过已存在的联系人: " + contact.getName());
            }
        }

        return count;
    }

    public ExcelService getExcelService() {
        return excelService;
    }
}