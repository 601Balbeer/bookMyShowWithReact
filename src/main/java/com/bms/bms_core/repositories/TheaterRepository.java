package com.bms.bms_core.repositories;

import com.bms.bms_core.models.Theater;
import com.bms.bms_core.models.TheatreOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, UUID> {
    
    @Query(
            value = """
                    SELECT\s
                        t.sys_id,
                        gr.id,
                        t.theater_name,
                        t.location_sys_id,
                        t.owner_sys_id,
                        gr.created_at,
                        gr.updated_at,
                        gr.created_by,
                        gr.updated_by
                    FROM theaters t
                    INNER JOIN global_records gr ON t.sys_id = gr.sys_id
                    INNER JOIN locations l ON t.location_sys_id = l.sys_id
                    INNER JOIN users u ON t.owner_sys_id = u.sys_id
                    WHERE LOWER(l.city) = LOWER(:city)
                    """, nativeQuery = true
    )
    /** Finds theaters in the supplied city using the declared query. */
    List<Theater> findAllTheatersByCity(@Param("city") String city);


    /** Finds theaters in a city without case-sensitive matching. */
    @Query("SELECT t FROM Theater t WHERE LOWER(t.location.city) = LOWER(:city)")
    List<Theater> findByLocation_CityIgnoreCase(@Param("city") String city);
    
    @Query(value = """
            SELECT t.sys_id, t.theater_name, t.location_sys_id, t.owner_id,
                   gr.id, gr.created_at, gr.updated_at, gr.created_by, gr.updated_by
            FROM theaters t
            INNER JOIN global_records gr ON t.sys_id = gr.sys_id
            INNER JOIN locations l ON t.location_sys_id = l.sys_id
            WHERE LOWER(l.city) = LOWER(:city)
            """, nativeQuery = true)
    /** Finds theaters in a city using the native SQL query. */
    List<Theater> findTheatersByCityNative(@Param("city") String city);

    /** Finds all theaters owned by the specified theater owner. */
    public List<Theater> findByOwner(TheatreOwner theatreOwner);

}
