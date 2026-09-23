package lk.jiat.fiadabook.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@NamedQuery(name="Role.findByValue",
        query = "FROM Role s WHERE s.value=:value")
@Table(name = "role")
public class Role implements Serializable {

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
        ADMIN,
        USER,
    }


}
