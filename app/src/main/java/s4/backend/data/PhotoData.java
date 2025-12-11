package s4.backend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

import org.json.JSONObject;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;

import lombok.Data;

@Data //auto adds getters/setters
@Entity
@Table(name = "photo_data")
public class PhotoData {

    public PhotoData(){}

    public PhotoData(JSONObject jsonObject) {        
        this.upload_timestamp = ZonedDateTime.now();

        if(jsonObject.isNull("timestamp")){
            this.taken_timestamp = null;
        }
        else{
            Instant i = Instant.ofEpochMilli(jsonObject.getLong("timestamp"));
            this.taken_timestamp = ZonedDateTime.ofInstant(i, ZoneOffset.UTC);
        }

        this.media_type                = jsonObject.isNull("media_type")                ? null : jsonObject.getString("media_type");
        this.latitude                  = jsonObject.isNull("latitude")                  ? null : jsonObject.getDouble("latitude");
        this.longitude                 = jsonObject.isNull("longitude")                 ? null : jsonObject.getDouble("longitude");
        this.location_accuracy         = jsonObject.isNull("location_accuracy")         ? null : jsonObject.getDouble("location_accuracy");
        this.altitude_above_msl        = jsonObject.isNull("altitude_above_msl")        ? null : jsonObject.getDouble("altitude_above_msl");
        this.height_above_ellipsoid    = jsonObject.isNull("height_above_ellipsoid")    ? null : jsonObject.getDouble("height_above_ellipsoid");
        this.linear_error              = jsonObject.isNull("linear_error")              ? null : jsonObject.getDouble("linear_error");
        this.resolution                = jsonObject.isNull("resolution")                ? null : jsonObject.getString("resolution");
        this.zoom                      = jsonObject.isNull("zoom")                      ? null : jsonObject.getDouble("zoom");
        this.horizontal_field_of_view  = jsonObject.isNull("horizontal_field_of_view")  ? null : jsonObject.getDouble("horizontal_field_of_view");
        this.vertical_field_of_view    = jsonObject.isNull("vertical_field_of_view")    ? null : jsonObject.getDouble("vertical_field_of_view");
        this.azumith                   = jsonObject.isNull("azumith")                   ? null : jsonObject.getDouble("azumith");
        this.pitch                     = jsonObject.isNull("pitch")                     ? null : jsonObject.getDouble("pitch");
        this.roll                      = jsonObject.isNull("roll")                      ? null : jsonObject.getDouble("roll");
        this.lens_type                 = jsonObject.isNull("lens_type")                 ? null : jsonObject.getString("lens_type");
        this.location_provider         = jsonObject.isNull("location_provider")         ? null : jsonObject.getString("location_provider");
    }

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "device_id", referencedColumnName = "id")
    private Device device_id;

    @Column(name = "upload_timestamp")
    public ZonedDateTime upload_timestamp; //TODO: change this back to private and use reflection to force different times for testing

    @Column(name = "taken_timestamp")
    private ZonedDateTime taken_timestamp;

    @Column(name = "media_type") 
    private String media_type;
    
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "location_accuracy")
    private Double location_accuracy;

    @Column(name = "altitude_above_msl")
    private Double altitude_above_msl;

    @Column(name = "height_above_ellipsoid")
    private Double height_above_ellipsoid;

    @Column(name = "linear_error")
    private Double linear_error;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "zoom")
    private Double zoom;

    @Column(name = "horizontal_field_of_view")
    private Double horizontal_field_of_view;

    @Column(name = "vertical_field_of_view")
    private Double vertical_field_of_view;

    @Column(name = "azumith")
    private Double azumith;

    @Column(name = "pitch")
    private Double pitch;

    @Column(name = "roll")
    private Double roll;

    @Column(name = "lens_type")
    private String lens_type;

    @Column(name = "location_provider")
    private String location_provider;
}