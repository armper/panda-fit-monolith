package com.pandatech.pandafit.repository;

import com.pandatech.pandafit.domain.Routine;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for the Routine entity.
 */
@Repository
public interface RoutineRepository extends MongoRepository<Routine, String> {
    @Query("{}")
    Page<Routine> findAllWithEagerRelationships(Pageable pageable);

    @Query("{}")
    List<Routine> findAllWithEagerRelationships();

    @Query("{'id': ?0}")
    Optional<Routine> findOneWithEagerRelationships(String id);
}
