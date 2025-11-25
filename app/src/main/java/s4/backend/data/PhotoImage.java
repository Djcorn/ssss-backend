package s4.backend.data;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import lombok.Data;

@Data //auto adds getters/setters
@Entity
@Table(name = "photo_image")
public class PhotoImage {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private long id;

    // need to link properly
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_data_id", referencedColumnName = "id")
    private long photo_data_id;

    @Column(name = "photo_data", nullable=false) 
    private byte[] photo_data;

}