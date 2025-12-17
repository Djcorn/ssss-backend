package s4.backend.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import s4.backend.data.PhotoData;

@Repository
public interface PhotoDataRepository extends JpaRepository<PhotoData, Long>{


    //TODO: HOW DO YOU HANDLE WRAP AROUNDS? THAT'S GOING TO BE AN UNCOMMON CASE BUT IMPORTANT TO CONSIDER
    @Query(value = "SELECT * FROM photo_data WHERE latitude >= ?1 AND longitude >= ?2 AND latitude <= ?3 AND longitude <= ?4", nativeQuery = true)
    List<PhotoData> findPhotoDataByLatLonBox(Double latitude_1,
                                             Double longitude_1,
                                             Double latitude_2,
                                             Double longitude_2);

    @Query(value = "SELECT * FROM photo_data WHERE upload_timestamp_in_milli_epoch >= ?1", nativeQuery = true)
    List<PhotoData> findPhotoDataAfterDate(Long startTimeInMilliSinceEpoch);
    
    @Query(value = "SELECT * FROM photo_data WHERE upload_timestamp_in_milli_epoch >= ?5 AND latitude >= ?1 AND longitude >= ?2 AND latitude <= ?3 AND longitude <= ?4", nativeQuery = true)
    List<PhotoData> findPhotoDataByLatLonBoxAfterDate(Double latitude_1,
                                                      Double longitude_1,
                                                      Double latitude_2,
                                                      Double longitude_2,
                                                      Long startTimeInMilliSinceEpoch);    
    
    //TODO: Add stopTimeInMilliSinceEpoch as an optional argument 
}