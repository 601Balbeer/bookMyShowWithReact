package com.bms.bms_core.repositories;

import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.HallRowMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HallRowMappingRepository extends JpaRepository<HallRowMapping, UUID> {
    /** Finds the seat-row configurations registered for a hall. */
    public List<HallRowMapping> findByHall(Hall hall);
}
