module itss.group11 {
    // --- REQUIRES ---
    requires javafx.controls;
    requires javafx.fxml;
    
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core; 
    
    requires spring.boot.starter.data.jpa; // Gộp chung nếu cần thiết
    requires spring.data.jpa;
    requires spring.data.commons; 
    requires spring.context;
    requires spring.beans;
    requires spring.tx;
    requires spring.web;

    requires java.dotenv;
    requires lombok;

    // --- OPENS (CẤU HÌNH ĐỂ SPRING/JAVAFX HOẠT ĐỘNG) ---
    
    // 1. Cho phép Hibernate và Spring truy cập các Entity (Models)
    opens itss.group11.models to org.hibernate.orm.core, spring.core, spring.beans;
    
    // 2. Cho phép Spring quản lý các Repository và Service
    opens itss.group11.repository.allocation to spring.core, spring.beans, spring.context;
    opens itss.group11.services.allocation to spring.core, spring.beans, spring.context;

    // 3. CẤU HÌNH CONTROLLER (ĐÃ SỬA: Thêm đường dẫn đúng tới DashboardController)
    // Mở gói chứa các Controller cho Spring và JavaFX
    opens itss.group11.frontend.screens.dashboard to javafx.fxml, spring.core, spring.beans, spring.context;
    
    // Nếu bạn còn các gói controller khác ở 'controllers.allocation', hãy giữ nguyên:
    opens itss.group11.controllers.allocation to javafx.fxml, spring.core, spring.beans, spring.context;

    // 4. Cho phép JavaFX khởi chạy App và quản lý Stage
    opens itss.group11.frontend to javafx.fxml;
    opens itss.group11.frontend.stage to javafx.fxml;

    // --- EXPORTS ---
    exports itss.group11.frontend;
}