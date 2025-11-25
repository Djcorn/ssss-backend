package s4.backend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import lombok.Data;

@Data //auto adds getters/setters
@Entity
@Table(name = "photo_data")
public class PhotoData {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    // need to link https://stackoverflow.com/questions/64634166/how-to-link-foreign-key-between-entity-in-spring-boot-data-jpa
    //@Column(name = "user_id", nullable=false) // ID of user table
    //private long user_id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "device_id", referencedColumnName = "id")

    @Column(name = "timestamp")
    private double timestamp;

    @Column(name = "media_type") 
    private String media_type;
    
    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "location_accuracy")
    private double location_accuracy;

    @Column(name = "altitude_above_msl")
    private double altitude_above_msl;

    @Column(name = "height_above_ellipsoid")
    private double height_above_ellipsoid;

    @Column(name = "linear_error")
    private double linear_error;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "zoom")
    private double zoom;

    @Column(name = "horizontal_field_of_view")
    private double horizontal_field_of_view;

    @Column(name = "vertical_field_of_view")
    private double vertical_field_of_view;

    @Column(name = "azumith")
    private double azumith;

    @Column(name = "pitch")
    private double pitch;

    @Column(name = "roll")
    private double roll;

    @Column(name = "lens_type")
    private String lens_type;

    @Column(name = "location_provider")
    private String location_provider;
}