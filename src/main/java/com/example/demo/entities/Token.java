package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.Hibernate;

import java.util.Objects;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "token")
@Accessors(chain = true)
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;

    @Lob
    @Column(name = "public_key")
    private byte[] publicKey;

    @Column(name = "username")
    private String username;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Token token = (Token) o;
        return getToken() != null && Objects.equals(getToken(), token.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getToken());
    }
}
