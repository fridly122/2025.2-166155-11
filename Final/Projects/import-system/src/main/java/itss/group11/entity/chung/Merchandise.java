package itss.group11.entity.chung;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "merchandise")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Merchandise {

    @Id
    @Column(name = "code", length = 50)
    private String code;           // Mã hàng (VD: "SP001")

    @Column(name = "name", nullable = false, length = 200)
    private String name;           // Tên mặt hàng

    @Column(name = "unit", length = 50)
    private String unit;           // Đơn vị (cái, kg, thùng...)
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
}
