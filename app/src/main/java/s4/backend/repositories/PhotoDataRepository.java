package s4.backend.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import s4.backend.data.PhotoData;

@Repository
public interface PhotoDataRepository extends JpaRepository<PhotoData, Long>{


 //CRITIAL NOTE: HOW DO YOU HANDLE WRAP AROUNDS?

    @Query(value = "SELECT * FROM photo_data WHERE latitude >= ?1 AND longitude >= ?2 AND latitude <= ?3 AND longitude <= ?4", nativeQuery = true)
    List<PhotoData> findPhotoDataByLatLonBox(Double latitude_1,
                                             Double longitude_1,
                                             Double latitude_2,
                                             Double longitude_2);

    @Query(value = "SELECT * FROM photo_data WHERE latitude >= ?1 AND longitude >= ?2 AND latitude <= ?3 AND longitude <= ?4", nativeQuery = true)
    List<PhotoData> findPhotoDataByLatLonBoxAfterDate(ZonedDateTime startTime,
                                             Double latitude_1,
                                             Double longitude_1,
                                             Double latitude_2,
                                             Double longitude_2);
    
    
    //@Query(value = "SELECT * FROM photo_data WHERE latitude <= :latitude_1 AND longitude <= :longitude_1" , nativeQuery = true)
    //List<PhotoData> findPhotoDataAfterDate(@Param("upload_timestamp") ZonedDateTime startTime);

    
    
}