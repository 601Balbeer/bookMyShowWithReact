package com.bms.bms_core.repositories;

import com.bms.bms_core.models.Hall;
import com.bms.bms_core.models.Show;
import com.bms.bms_core.models.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShowRepository extends JpaRepository<Show, UUID> {

    /** Finds all shows scheduled in a hall. */
    public List<Show> findByHall(Hall hall);

    /** Finds all shows scheduled in theaters matching the supplied theater. */
    public List<Show> findByHall_Theater(Theater theater);
}
