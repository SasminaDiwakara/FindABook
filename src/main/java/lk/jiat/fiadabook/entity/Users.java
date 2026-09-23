package lk.jiat.fiadabook.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@NamedQuery(name = "Users.getByEmail",query = "FROM Users u WHERE u.email=:email")
public class Users extends lk.jiat.fiadabook.entity.BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "first_name", length = 45, nullable = false)
    private String firstName;
    @Column(name = "last_name", length = 45, nullable = false)
    private String lastName;
    @Column(name = "email", length = 150, nullable = false)
    private String email;
    @Column(name = "password", length = 20, nullable = false)
    private String password;
    @Column(name = "verification_code", length = 15 , nullable = false)
    private String verificationCode;

    @ManyToOne(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "role_id" ,  nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY , cascade = CascadeType.ALL)
    @JoinColumn(name = "status_id" ,  nullable = false)
    private Status status;

    @OneToMany(mappedBy = "users")
    private Set<Address> addresses = new HashSet<>();

    @OneToMany(mappedBy = "users")
    private Set<Orders> orders = new HashSet<>();

    @OneToMany(mappedBy = "users")
    private Set<Cart> cart = new HashSet<>();

    public Set<Cart> getCart() {return cart;}

    public Set<Orders> getOrders() {
        return orders;
    }

    public Set<Address> getAddresses() {
        return addresses;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
