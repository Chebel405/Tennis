package com.dyma.tennis.data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "player", schema = "public")
public class PlayerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "identifier", nullable = false, unique = true)
    private UUID identifier;

    @Column(name = "last_name", length = 50, nullable = false)
    private String lastName;

    @Column(name = "first_name", length = 50, nullable = false)
    private String firstName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "rank", nullable = false)
    private Integer rank;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "player_tournament",
            joinColumns = {@JoinColumn(name = "player_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "tournament_id", referencedColumnName = "id")}
    )
    private Set<TournamentEntity> tournaments = new HashSet<>();

    public PlayerEntity() {
    }

    public PlayerEntity( UUID identifier, String lastName, String firstName, LocalDate birthDate, Integer points, Integer rank) {
        this.identifier = identifier;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.points = points;
        this.rank = rank;
    }

    public PlayerEntity(Long id, UUID identifier, String lastName, String firstName, LocalDate birthDate, Integer points, Integer rank) {
        this.id = id;
        this.identifier = identifier;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.points = points;
        this.rank = rank;
    }

    public Long getId() {
        return id;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Set<TournamentEntity> getTournaments() {
        return tournaments;
    }

    public void addTournament(TournamentEntity tournament){
        this.tournaments.add(tournament);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerEntity that = (PlayerEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
