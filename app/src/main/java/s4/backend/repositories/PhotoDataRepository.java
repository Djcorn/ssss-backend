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

    @Query(value = "SELECT * FROM photo_data WHERE latitude <= :latitude_1 AND longitude <= :longitude_1 AND latitude >= :latitude_2 AND longitude >= :longitude_2" , nativeQuery = true)
    List<PhotoData> findPhotoDataByLatLonBox(@Param("latitude") Double latitude_1,
                                             @Param("longitude") Double longitude_1,
                                             @Param("latitude") Double latitude_2,
                                             @Param("longitude") Double longitude_2);

        @Query(value = "SELECT * FROM photo_data WHERE latitude <= :latitude_1 AND longitude <= :longitude_1 AND latitude >= :latitude_2 AND longitude >= :longitude_2" , nativeQuery = true)    
        List<PhotoData> findPhotoDataByLatLonBoxAfterDate(@Param("upload_timestamp") ZonedDateTime startTime,
                                                 @Param("latitude") Double latitude_1,
                                                 @Param("longitude") Double longitude_1,
                                                 @Param("latitude") Double latitude_2,
                                                 @Param("longitude") Double longitude_2);

    
    
    //@Query(value = "SELECT * FROM photo_data WHERE latitude <= :latitude_1 AND longitude <= :longitude_1" , nativeQuery = true)
    //List<PhotoData> findPhotoDataAfterDate(@Param("upload_timestamp") ZonedDateTime startTime);

    
    
    }