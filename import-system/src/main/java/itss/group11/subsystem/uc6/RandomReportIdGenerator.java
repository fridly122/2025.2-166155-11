package itss.group11.subsystem.uc6;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class RandomReportIdGenerator implements ReportIdGenerator {

    @Override
    public String nextId() {
        return "BB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
