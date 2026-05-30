module itss.group11 {

    // Java core
    requires java.net.http;
    requires java.sql;

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Jackson
    requires com.fasterxml.jackson.databind;

    // JPA / Hibernate
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    // PostgreSQL JDBC Driver
    requires org.postgresql.jdbc;

    // Spring
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.beans;
    requires spring.core;
    requires spring.tx;
    requires spring.web;
    requires spring.aop;
    requires spring.data.jpa;
    requires spring.data.commons;

    // Other
    requires java.dotenv;
    requires static lombok;

    // Spring Boot entry point
    opens itss.group11 to spring.core, spring.beans, spring.context;

    // Models cho Hibernate / JPA / Spring reflection
    opens itss.group11.models to
            org.hibernate.orm.core,
            spring.core,
            spring.beans,
            spring.context,
            com.fasterxml.jackson.databind;

    // DTO cho Jackson / Spring
    opens itss.group11.dto.allocation to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.dto.transport to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.dto.orderExecution to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.dto.requestManage to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.dto.siteSync to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.dto.warehouse to
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;

    exports itss.group11.dto.allocation to
            org.hibernate.orm.core;

    // Repository packages
    opens itss.group11.repository.allocation to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.repository.orderExecution to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.repository.requestManage to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.repository.siteSync to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.repository.transport to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.repository.warehouse to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    // Service packages
    opens itss.group11.services.allocation to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    opens itss.group11.services.transport to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    opens itss.group11.services.orderExecution to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    opens itss.group11.services.requestManage to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    opens itss.group11.services.siteSync to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    opens itss.group11.services.warehouse to
            spring.core,
            spring.beans,
            spring.context,
            spring.aop;

    // Spring REST Controller
    opens itss.group11.controllers.allocation to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    opens itss.group11.controllers.transport to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    opens itss.group11.controllers.orderExecution to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    opens itss.group11.controllers.requestManage to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    opens itss.group11.controllers.siteSync to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    opens itss.group11.controllers.warehouse to
            spring.core,
            spring.beans,
            spring.context,
            spring.web;

    // JavaFX App / Controllers
    opens itss.group11.frontend to
            javafx.fxml,
            spring.core,
            spring.beans,
            spring.context;

    opens itss.group11.frontend.stage to javafx.fxml;

    opens itss.group11.frontend.screens.login to javafx.fxml;
    opens itss.group11.frontend.screens.dashboard to javafx.fxml;
    opens itss.group11.frontend.screens.allocationList to javafx.fxml;
    opens itss.group11.frontend.screens.siteClassification to javafx.fxml;
    opens itss.group11.frontend.screens.orderReconciliation to javafx.fxml;
    opens itss.group11.frontend.screens.orderRequestCreate to javafx.fxml;
    opens itss.group11.frontend.screens.siteInventoryManage to javafx.fxml;
    opens itss.group11.frontend.screens.siteShippingManage to javafx.fxml;

    // Export App
    exports itss.group11.frontend;
}
