package com.example.appstore.repository;

import com.example.appstore.models.AppEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppEntryRepository extends JpaRepository<AppEntry, Long> {
}
