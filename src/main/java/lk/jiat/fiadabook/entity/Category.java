package lk.jiat.fiadabook.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category implements java.io.Serializable{

    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private int id;

    @Column(name="name" , nullable = false)
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

