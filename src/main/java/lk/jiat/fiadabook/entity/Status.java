package lk.jiat.fiadabook.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@NamedQuery(name="Status.findByValue",
        query = "FROM Status s WHERE s.value=:value")
@Table(name = "status")
public class Status implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 45 , nullable = false , unique = true)
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum Type{
        ACTIVE,
        INACTIVE,
        DELETED,
        PENDING,
        DELIVERED,
        PACKING,
        DECLINE,
        APPROVED,
        REJECTED,
        RECEIVED,
        COMPLETED,
        CANCELLED,
        VERIFIED
    }


}
