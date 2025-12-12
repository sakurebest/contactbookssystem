package com.example.contactbookssystem.service;

import com.example.contactbookssystem.model.Contact;
import com.example.contactbookssystem.model.ContactMethod;
import com.example.contactbookssystem.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    public List<Contact> getAllContacts() {
        List<Contact> contacts = contactRepository.findAll();
        System.out.println("获取所有联系人数量: " + contacts.size());
        return contacts;
    }

    public List<Contact> getBookmarkedContacts() {
        List<Contact> contacts = contactRepository.findByBookmarkedTrue();
        System.out.println("获取收藏联系人数量: " + contacts.size());
        return contacts;
    }

    public List<Contact> searchContacts(String keyword) {
        List<Contact> contacts = contactRepository.findByNameContainingIgnoreCase(keyword);
        System.out.println("搜索 '" + keyword + "' 的结果数量: " + contacts.size());
        return contacts;
    }

    public Optional<Contact> getContactById(Long id) {
        return contactRepository.findById(id);
    }

    @Transactional
    public Contact saveContact(Contact contact) {
        System.out.println("=== 开始保存联系人 ===");
        System.out.println("姓名: " + contact.getName());
        System.out.println("公司: " + contact.getCompany());
        System.out.println("职位: " + contact.getPosition());
        System.out.println("备注: " + contact.getNotes());
        System.out.println("收藏: " + contact.isBookmarked());

        // 确保联系方式不为null
        if (contact.getContactMethods() == null) {
            contact.setContactMethods(new java.util.ArrayList<>());
        }

        System.out.println("联系方式数量: " + contact.getContactMethods().size());

        // 确保至少有一个主要联系方式
        if (!contact.getContactMethods().isEmpty()) {
            boolean hasPrimary = contact.getContactMethods().stream()
                    .anyMatch(method -> method != null && method.isPrimary());

            System.out.println("是否有主要联系方式: " + hasPrimary);

            if (!hasPrimary) {
                for (ContactMethod method : contact.getContactMethods()) {
                    if (method != null && method.getValue() != null && !method.getValue().trim().isEmpty()) {
                        method.setPrimary(true);
                        System.out.println("设置为主要联系方式: " + method.getType() + " - " + method.getValue());
                        break;
                    }
                }
            }

            // 打印所有联系方式
            int i = 1;
            for (ContactMethod method : contact.getContactMethods()) {
                if (method != null) {
                    System.out.println("联系方式 " + i++ + ": " +
                            method.getType() + " - " +
                            method.getValue() + " - " +
                            method.getLabel() + " - 主要: " + method.isPrimary());
                }
            }
        }

        Contact savedContact = contactRepository.save(contact);
        System.out.println("=== 联系人保存完成，ID: " + savedContact.getId() + " ===");
        return savedContact;
    }

    @Transactional
    public void deleteContact(Long id) {
        System.out.println("删除联系人 ID: " + id);
        contactRepository.deleteById(id);
    }

    @Transactional
    public Contact toggleBookmark(Long id) {
        System.out.println("切换收藏状态 ID: " + id);
        Optional<Contact> contactOpt = contactRepository.findById(id);
        if (contactOpt.isPresent()) {
            Contact contact = contactOpt.get();
            contact.setBookmarked(!contact.isBookmarked());
            System.out.println("新收藏状态: " + contact.isBookmarked());
            return contactRepository.save(contact);
        }
        return null;
    }
}