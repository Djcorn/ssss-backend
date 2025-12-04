package s4.backend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import lombok.Data;

@Data //auto adds getters/setters
@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    @Column(name = "manufacturer") 
    private String manufacturer;

    @Column(name = "version") 
    private String version;

    @Column(name = "model") 
    private String model;
}