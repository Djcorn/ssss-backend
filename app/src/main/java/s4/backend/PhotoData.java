package s4.backend;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "photo_data")
public class PhotoData {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    @Column(name = "timestamp")
    private double timestamp;

    @Column(name = "user_id", nullable=false) // ID of user table
    private long user_id;

    @Column(name = "device_id", nullable=false) // ID of device table
    private long device_id;

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
    private double resolution;

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

    private String name;

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}