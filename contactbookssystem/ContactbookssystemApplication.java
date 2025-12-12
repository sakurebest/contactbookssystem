package com.example.contactbookssystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;

@SpringBootApplication
public class ContactbookssystemApplication {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        System.setProperty("spring.devtools.restart.enabled", "true");
        SpringApplication.run(ContactbookssystemApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printApplicationInfo() {
        try {
            // 获取实际端口（Spring Boot 启动后会设置 server.port 属性）
            String port = environment.getProperty("local.server.port");
            if (port == null) {
                // 尝试从其他属性获取
                port = environment.getProperty("server.port", "8080");
                System.out.println("⚠️ 使用默认端口: " + port);
            }

            String host = InetAddress.getLocalHost().getHostAddress();

            String line = "=".repeat(70);
            System.out.println("\n" + line);
            System.out.println("🚀 联系簿管理系统启动成功!");
            System.out.println(line);

            System.out.println("🌐 访问地址:");
            System.out.println("   主页:        http://localhost:" + port + "/");
            System.out.println("   主页:        http://" + host + ":" + port + "/");
            System.out.println("   联系人列表:   http://localhost:" + port + "/contacts");
            System.out.println("   收藏联系人:   http://localhost:" + port + "/contacts/bookmarked");
            System.out.println("   添加联系人:   http://localhost:" + port + "/contacts/add");

            System.out.println("\n📊 H2 数据库控制台:");
            System.out.println("   地址:        http://localhost:" + port + "/h2-console");
            System.out.println("   JDBC URL:    jdbc:h2:mem:contactdb");
            System.out.println("   用户名:      sa");
            System.out.println("   密码:        (空)");

            System.out.println("\n📁 静态资源:");
            System.out.println("   CSS 文件:    http://localhost:" + port + "/static/css/style.css");

            System.out.println("\n🔍 调试信息:");
            System.out.println("   端口:        " + port);
            System.out.println("   Context Path: " + environment.getProperty("server.servlet.context-path", "/"));
            System.out.println("   激活 Profiles: " + String.join(", ", environment.getActiveProfiles()));

            System.out.println(line + "\n");

        } catch (Exception e) {
            System.err.println("❌ 无法获取服务器信息: " + e.getMessage());
            e.printStackTrace();
        }
    }
}