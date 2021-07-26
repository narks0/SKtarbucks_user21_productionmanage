package local;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProductionRepository extends PagingAndSortingRepository<Production, Long>{
    List<Production> findByMenuId(Long MenuId);
    Production findByOrderId(Long OrderId);
}