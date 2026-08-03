package com.app.ecomisiones.repository;

import com.app.ecomisiones.model.Pool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolRepository extends JpaRepository<Pool, Integer> {
}
