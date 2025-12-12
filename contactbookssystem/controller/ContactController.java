package com.example.contactbookssystem.controller;

import com.example.contactbookssystem.model.Contact;
import com.example.contactbookssystem.model.ContactMethod;
import com.example.contactbookssystem.model.ContactMethodType;
import com.example.contactbookssystem.service.ContactService;
import com.example.contactbookssystem.service.ImportExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private ImportExportService importExportService;

    @GetMapping("/")
    public String home(Model model, HttpServletRequest request) {
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        model.addAttribute("baseUrl", baseUrl);
        return "index";
    }

    @GetMapping("/contacts")
    public String getAllContacts(Model model,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String message,
                                 @RequestParam(required = false) String error,
                                 HttpServletRequest request) {
        List<Contact> contacts;

        if (search != null && !search.trim().isEmpty()) {
            contacts = contactService.searchContacts(search);
            model.addAttribute("searchKeyword", search);
        } else {
            contacts = contactService.getAllContacts();
        }

        model.addAttribute("contacts", contacts);
        model.addAttribute("contactMethodTypes", ContactMethodType.values());
        model.addAttribute("message", message);
        model.addAttribute("error", error);

        // 添加 baseUrl 用于前端链接
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        model.addAttribute("baseUrl", baseUrl);

        return "contacts";
    }

    @GetMapping("/contacts/bookmarked")
    public String getBookmarkedContacts(Model model, HttpServletRequest request) {
        List<Contact> contacts = contactService.getBookmarkedContacts();
        model.addAttribute("contacts", contacts);
        model.addAttribute("isBookmarkedPage", true);
        model.addAttribute("contactMethodTypes", ContactMethodType.values());

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        model.addAttribute("baseUrl", baseUrl);

        return "contacts";
    }

    @GetMapping("/contacts/add")
    public String showAddForm(Model model, HttpServletRequest request) {
        Contact contact = new Contact();
        contact.setContactMethods(new ArrayList<>());
        contact.getContactMethods().add(new ContactMethod());

        model.addAttribute("contact", contact);
        model.addAttribute("contactMethodTypes", ContactMethodType.values());
        model.addAttribute("isEdit", false);

        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        model.addAttribute("baseUrl", baseUrl);

        return "edit-contact";
    }

    @GetMapping("/contacts/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        return contactService.getContactById(id)
                .map(contact -> {
                    model.addAttribute("contact", contact);
                    model.addAttribute("contactMethodTypes", ContactMethodType.values());
                    model.addAttribute("isEdit", true);

                    String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                    model.addAttribute("baseUrl", baseUrl);

                    return "edit-contact";
                })
                .orElse("redirect:/contacts?error=联系人不存在");
    }

    @PostMapping("/contacts/save")
    public String saveContact(@ModelAttribute Contact contact,
                              @RequestParam(value = "methodTypes", required = false) String[] methodTypes,
                              @RequestParam(value = "methodValues", required = false) String[] methodValues,
                              @RequestParam(value = "methodLabels", required = false) String[] methodLabels,
                              @RequestParam(value = "methodPrimaries", required = false) String[] methodPrimaries,
                              RedirectAttributes redirectAttributes) {

        System.out.println("=== 开始保存联系人 ===");
        System.out.println("姓名: " + contact.getName());

        try {
            // 处理联系方式
            List<ContactMethod> contactMethods = new ArrayList<>();
            if (methodTypes != null && methodValues != null) {
                for (int i = 0; i < methodTypes.length; i++) {
                    if (methodValues[i] != null && !methodValues[i].trim().isEmpty()) {
                        ContactMethod method = new ContactMethod();
                        method.setType(ContactMethodType.valueOf(methodTypes[i]));
                        method.setValue(methodValues[i].trim());

                        if (methodLabels != null && i < methodLabels.length && methodLabels[i] != null) {
                            method.setLabel(methodLabels[i]);
                        }

                        if (methodPrimaries != null) {
                            method.setPrimary(Arrays.asList(methodPrimaries).contains(String.valueOf(i)));
                        }

                        contactMethods.add(method);
                        System.out.println("添加联系方式: " + method.getType() + " - " + method.getValue());
                    }
                }
            }

            if (contactMethods.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请至少添加一个联系方式");
                if (contact.getId() != null) {
                    return "redirect:/contacts/edit/" + contact.getId();
                } else {
                    return "redirect:/contacts/add";
                }
            }

            contact.setContactMethods(contactMethods);

            Contact savedContact = contactService.saveContact(contact);
            System.out.println("联系人保存成功，ID: " + savedContact.getId());

            redirectAttributes.addFlashAttribute("message", "联系人 '" + contact.getName() + "' 保存成功！");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "保存失败: " + e.getMessage());
            if (contact.getId() != null) {
                return "redirect:/contacts/edit/" + contact.getId();
            } else {
                return "redirect:/contacts/add";
            }
        }

        return "redirect:/contacts";
    }

    @GetMapping("/contacts/delete/{id}")
    public String deleteContact(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contactService.deleteContact(id);
            redirectAttributes.addFlashAttribute("message", "联系人删除成功！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/toggle-bookmark/{id}")
    public String toggleBookmark(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Contact contact = contactService.toggleBookmark(id);
            if (contact != null) {
                String action = contact.isBookmarked() ? "收藏" : "取消收藏";
                redirectAttributes.addFlashAttribute("message", "联系人 '" + contact.getName() + "' " + action + "成功！");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "操作失败: " + e.getMessage());
        }
        return "redirect:/contacts";
    }

    @GetMapping("/contacts/export/all")
    public ResponseEntity<byte[]> exportAllContacts() {
        try {
            System.out.println("开始导出所有联系人...");
            byte[] excelBytes = importExportService.exportAllContacts();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "all_contacts.xlsx";
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelBytes.length);

            System.out.println("导出成功，文件大小: " + excelBytes.length + " bytes");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("导出失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/contacts/export/bookmarked")
    public ResponseEntity<byte[]> exportBookmarkedContacts() {
        try {
            System.out.println("开始导出收藏联系人...");
            byte[] excelBytes = importExportService.exportBookmarkedContacts();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String fileName = "bookmarked_contacts.xlsx";
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setContentLength(excelBytes.length);

            System.out.println("导出成功，文件大小: " + excelBytes.length + " bytes");
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("导出失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/contacts/import")
    public String importContacts(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes) {
        try {
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "请选择文件");
                return "redirect:/contacts";
            }

            String fileName = file.getOriginalFilename();
            System.out.println("导入文件: " + fileName + ", 大小: " + file.getSize() + " bytes");

            int count = importExportService.importContacts(file);
            redirectAttributes.addFlashAttribute("message", "成功导入 " + count + " 个联系人");

        } catch (Exception e) {
            System.err.println("导入失败: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "导入失败: " + e.getMessage());
        }

        return "redirect:/contacts";
    }

    // 添加一个简单的健康检查接口
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Contact Book System is running!");
    }

    // 添加静态资源映射测试接口
    @GetMapping("/test/css")
    @ResponseBody
    public String testCss() {
        return "CSS path should be /static/css/style.css";
    }
}