package io.github.raphaelmun1z.ChatGepeteco.entities.usuario;

import io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao.Papel;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"papel"})
@EqualsAndHashCode(of = "id")
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "tb_usuario", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public abstract class Usuario implements UserDetails, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "O nome não pode ser vazio ou nulo.")
    @Column(nullable = false)
    private String nome;

    @NotBlank(message = "O e-mail não pode ser vazio ou nulo.")
    @Email(message = "O formato do e-mail é inválido.")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "A senha não pode ser vazia ou nula.")
    @Column(nullable = false)
    private String senha;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @NotNull(message = "O papel do usuário não pode ser nulo.")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "papel_id", nullable = false)
    private Papel papel;

    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
    private boolean enabled = true;

    public Usuario(String nome, String email, String senha, Papel papel) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.papel = papel;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this.papel != null) {
            authorities.add(new SimpleGrantedAuthority(this.papel.getNome()));
            authorities.addAll(this.papel.getPermissoes());
        }
        return authorities;
    }

    public boolean hasRole(String nomeDoPapel) {
        if (this.papel == null || nomeDoPapel == null) {
            return false;
        }
        return this.papel.getNome().equalsIgnoreCase(nomeDoPapel);
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}