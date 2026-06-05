package itss.group11.subsystem.chung;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import itss.group11.entity.chung.Merchandise;

@Repository
public interface MerchandiseRepository extends JpaRepository<Merchandise, String> {

    Optional<Merchandise> findByCode(String code);
    List<Merchandise> findAllByOrderByCodeAsc();
}

