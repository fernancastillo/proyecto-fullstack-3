package com.waitinglistservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; 

    @Column(nullable = false)
    private Long medicoId; 

    @Column(nullable = false, length = 100)
    private String specialty;

    @Column(nullable = false, length = 20)
    private String priority; 

    @Column(nullable = false, length = 50)
    private String status; 

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
        if (status == null) status = "EN_ESPERA";
        if (priority == null) priority = "MEDIA";
    }
}