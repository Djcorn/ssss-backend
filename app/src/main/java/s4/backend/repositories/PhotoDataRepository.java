package s4.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import s4.backend.data.PhotoData;

@Repository
public interface PhotoDataRepository extends JpaRepository<PhotoData, Long>{}