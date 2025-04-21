// src/main/java/com/example/appstore/repository/WarningRepository.java
package com.example.appstore.repository;

import com.example.appstore.models.Warning;
import com.example.appstore.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarningRepository extends JpaRepository<Warning, Long> {
    long countByDeveloper(User developer);

    void deleteByDeveloper(User developer);
}
