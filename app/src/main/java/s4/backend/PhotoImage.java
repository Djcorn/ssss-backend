package s4.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "photo_image")
public class PhotoImage {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    @Column(name = "photo_data_id", nullable=false)
    private long photo_data_id;

    @Column(name = "user_id", nullable=false) // ID of user table
    private double user_id;

}