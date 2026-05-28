module itss.group11 {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    
    // Database & ORM
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core; 
    
    // Spring Boot Core & JPA
    requires spring.data.jpa;
    requires spring.data.commons; // <-- BẮT BUỘC THÊM DÒNG NÀY ĐỂ HẾT LỖI @Param
    requires spring.context;
    requires spring.beans;
    requires spring.tx;

    // Spring Web
    requires spring.web;

    // Tiện ích
    requires java.dotenv;
    requires lombok;

    // Reflection config
    opens itss.group11.models to org.hibernate.orm.core, spring.core, spring.beans;
    opens itss.group11.controllers.allocation to spring.core, spring.beans, spring.context;
    
    // BẮT BUỘC THÊM: Cho phép Spring Data tạo proxy cho các Repository nằm trong package allocation
    opens itss.group11.repository.allocation to spring.core, spring.beans, spring.context;

    opens itss.group11 to javafx.fxml;
    opens itss.group11.controllers to javafx.fxml;

    exports itss.group11;
}