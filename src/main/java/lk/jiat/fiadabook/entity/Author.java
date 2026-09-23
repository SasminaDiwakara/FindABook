package lk.jiat.fiadabook.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "author")
@NamedQuery(name = "Author.grtPrimary",query = "FROM Author a")

public class Author implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name="name",nullable = false)
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
