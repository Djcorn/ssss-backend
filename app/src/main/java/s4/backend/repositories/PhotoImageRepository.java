package s4.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import s4.backend.PhotoImage;

@Repository
public interface PhotoImageRepository extends JpaRepository<PhotoImage, Long>{}