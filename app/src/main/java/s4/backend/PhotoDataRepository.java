package s4.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import s4.backend.PhotoData;

@Repository
public interface PhotoDataRepository extends JpaRepository<PhotoData, Long>{}