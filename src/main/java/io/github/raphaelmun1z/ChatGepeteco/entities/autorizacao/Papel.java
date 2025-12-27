package io.github.raphaelmun1z.ChatGepeteco.entities.autorizacao;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "tb_papel")
public class Papel implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank(message = "O nome do papel é obrigatório.")
    @Column(nullable = false, unique = true)
    private String nome;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "papel_permissao",
            joinColumns = @JoinColumn(name = "papel_id"),
            inverseJoinColumns = @JoinColumn(name = "permissao_id"))
    @ToString.Exclude
    private Set<Permissao> permissoes;
}