module itss.group11 {

    requires java.net.http;
    requires java.sql;

    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;

    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires org.postgresql.jdbc;

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

    requires java.dotenv;
    requires static lombok;

    opens itss.group11 to spring.core, spring.beans, spring.context;

    opens itss.group11.entity.chung to
            org.hibernate.orm.core,
            spring.core,
            spring.beans,
            spring.context,
            com.fasterxml.jackson.databind;

    opens itss.group11.entity.uc1 to com.fasterxml.jackson.databind, spring.core, spring.beans, spring.context;
    opens itss.group11.entity.uc2 to com.fasterxml.jackson.databind, spring.core, spring.beans, spring.context;
    opens itss.group11.entity.uc3 to com.fasterxml.jackson.databind, spring.core, spring.beans, spring.context;
    opens itss.group11.entity.uc4 to com.fasterxml.jackson.databind, spring.core, spring.beans, spring.context;
    opens itss.group11.entity.uc5 to
            org.hibernate.orm.core,
            com.fasterxml.jackson.databind,
            spring.core,
            spring.beans,
            spring.context;
    opens itss.group11.entity.uc6 to com.fasterxml.jackson.databind, spring.core, spring.beans, spring.context;

    opens itss.group11.subsystem.chung to
            spring.core,
            spring.beans,
            spring.context,
            spring.data.commons,
            spring.data.jpa;

    opens itss.group11.subsystem.uc1 to spring.core, spring.beans, spring.context, spring.aop;
    opens itss.group11.subsystem.uc2 to spring.core, spring.beans, spring.context, spring.aop;
    opens itss.group11.subsystem.uc3 to spring.core, spring.beans, spring.context, spring.aop;
    opens itss.group11.subsystem.uc4 to spring.core, spring.beans, spring.context, spring.aop;
    opens itss.group11.subsystem.uc5 to spring.core, spring.beans, spring.context, spring.aop;
    opens itss.group11.subsystem.uc6 to spring.core, spring.beans, spring.context, spring.aop;

    opens itss.group11.controller.chung to javafx.fxml, spring.core, spring.beans, spring.context;
    opens itss.group11.controller.uc1 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;
    opens itss.group11.controller.uc2 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;
    opens itss.group11.controller.uc3 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;
    opens itss.group11.controller.uc4 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;
    opens itss.group11.controller.uc5 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;
    opens itss.group11.controller.uc6 to javafx.fxml, spring.core, spring.beans, spring.context, spring.web;

    exports itss.group11.controller.chung;
}
