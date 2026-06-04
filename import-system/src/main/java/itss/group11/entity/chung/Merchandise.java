package itss.group11.entity.chung;

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
    private String code;           // MÃ£ hÃ ng (VD: "SP001")

    @Column(name = "name", nullable = false, length = 200)
    private String name;           // TÃªn máº·t hÃ ng

    @Column(name = "unit", length = 50)
    private String unit;           // ÄÆ¡n vá»‹ (cÃ¡i, kg, thÃ¹ng...)
}
